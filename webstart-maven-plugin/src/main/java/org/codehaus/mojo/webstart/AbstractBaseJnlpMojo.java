package org.codehaus.mojo.webstart;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.SystemUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.jar.JarSignVerifyMojo;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.archiver.manager.ArchiverManager;
import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.FileUtils;

/**
 * The superclass for all JNLP generating MOJOs.
 *
 * @author Kevin Stembridge
 * @author $LastChangedBy$
 * @since 28 May 2007
 * @version $Revision$
 *
 */
public abstract class AbstractBaseJnlpMojo extends AbstractMojo
{

    private static final String DEFAULT_RESOURCES_DIR = "src/main/jnlp/resources";

    /** unprocessed files (that will be signed) are prefixed with this */
    private static final String UNPROCESSED_PREFIX = "unprocessed_";

    /**
     * Artifact resolver, needed to download source jars for inclusion in classpath.
     *
     * @component
     * @required
     * @readonly
     */
    private ArtifactResolver artifactResolver;

    /**
     * Artifact factory, needed to download source jars for inclusion in classpath.
     *
     * @component
     * @required
     * @readonly
     */
    private ArtifactFactory artifactFactory;

    /**
     * @parameter default-value="${localRepository}"
     * @required
     * @readonly
     */
    private ArtifactRepository localRepository;

    /**
     * The collection of remote artifact repositories.
     *
     * @parameter default-value="${project.remoteArtifactRepositories}"
     * @readonly
     * @required
     */
    private List remoteRepositories;

    /**
     * The directory in which files will be stored prior to processing.
     *
     * @parameter default-value="${project.build.directory}/jnlp"
     * @required
     */
    private File workDirectory;

    /**
     * The path where the libraries are placed within the jnlp structure.
     *
     * @parameter default-value=""
     */
    protected String libPath;

    /**
     * The location of the directory (relative or absolute) containing non-jar resources that
     * are to be included in the JNLP bundle.
     *
     * @parameter
     */
    private File resourcesDirectory;

    /**
     * The location where the JNLP Velocity template files are stored.
     *
     * @parameter default-value="${project.basedir}/src/main/jnlp"
     * @required
     */
    private File templateDirectory;

    /**
     * Indicates whether or not jar resources should be compressed
     * using pack200. Setting this value to true requires SDK 5.0 or greater.
     *
     * @parameter default-value="false"
     */
    private boolean pack200;

    /**
     * The Sign Config
     *
     * @parameter implementation="org.codehaus.mojo.webstart.JarSignMojoConfig"
     */
    private SignConfig sign;

    /**
     * Indicates whether or not jar files should be verified after signing.
     *
     * @parameter default-value="true"
     */
    private boolean verifyjar;

    /**
     * Indicates whether or not gzip archives will be created for each of the jar
     * files included in the webstart bundle.
     *
     * @parameter default-value="false"
     */
    private boolean gzip;

    /**
     * Enable verbose output.
     *
     * @parameter expression="${verbose}" default-value="false"
     */
    private boolean verbose;
    
    /**
     * Set to true to exclude all transitive dependencies.
     * 
     * @parameter
     */
    private boolean excludeTransitive;

    private final List modifiedJnlpArtifacts = new ArrayList();

    // the jars to sign and pack are selected if they are prefixed by UNPROCESSED_PREFIX.
    // as the plugin copies the new versions locally before signing/packing them
    // we just need to see if the plugin copied a new version
    // We achieve that by only filtering files modified after the plugin was started
    // Note: if other files (the pom, the keystore config) have changed, one needs to clean
    private final FileFilter unprocessedJarFileFilter;
    private final FileFilter processedJarFileFilter;

    private final FileFilter unprocessedPack200FileFilter;

    /**
     * Define whether to remove existing signatures.
     * 
     * @parameter alias="unsign" default-value="false"
     */
    private boolean unsignAlreadySignedJars;

    /**
     * To look up Archiver/UnArchiver implementations
     *
     * @component
     * @required
     */
    protected ArchiverManager archiverManager;

    /**
     * Creates a new {@code AbstractBaseJnlpMojo}.
     */
    public AbstractBaseJnlpMojo()
    {

        processedJarFileFilter = new FileFilter()
        {
            public boolean accept( File pathname )
            {
                return pathname.isFile() && pathname.getName().endsWith( ".jar" )
                      && ! pathname.getName().startsWith( UNPROCESSED_PREFIX );
            }

        };

        unprocessedJarFileFilter = new FileFilter() 
        {
            public boolean accept( File pathname )
            {
                return pathname.isFile() && pathname.getName().startsWith( UNPROCESSED_PREFIX )
                       && pathname.getName().endsWith( ".jar" );
            }

        };

        unprocessedPack200FileFilter = new UnprocessedPack200FileFilter();
    }

    protected void makeWorkingDirIfNecessary() throws MojoExecutionException
    {

        if ( !getWorkDirectory().exists() && !getWorkDirectory().mkdirs() )
        {
            throw new MojoExecutionException( "Failed to create: " + getWorkDirectory().getAbsolutePath() );
        }

        // check and create the library path
        if ( !getLibDirectory().exists() && !getLibDirectory().mkdirs() )
        {
            throw new MojoExecutionException( "Failed to create: " + getLibDirectory().getAbsolutePath() );
        }

    }

    public abstract MavenProject getProject();

    /**
     * Returns the working directory. This is the directory in which files and resources
     * will be placed in order to be processed prior to packaging.
     * @return Returns the value of the workDirectory field.
     */
    protected File getWorkDirectory()
    {
        return workDirectory;
    }

    /**
     * Returns the library directory. If not libPath is configured, the working directory is returned.
     * @return Returns the value of the libraryDirectory field.
     */
    protected File getLibDirectory() 
    {
        if ( getLibPath() != null ) 
        {
            return new File( getWorkDirectory(), getLibPath() );
        }
        return getWorkDirectory();
    }

    /**
     * Returns the library path. This is ths subpath within the working directory, where the libraries are placed.
     * If the path is not configured it is <code>null</code>.
     * @return the library path or <code>null</code> if not configured.
     */
    public String getLibPath() 
    {
        if ( ( libPath == null ) || ( libPath.trim().length() == 0 ) )
        {
            return null;
        }
        return libPath;
    }

    /**
     * Returns the location of the directory containing
     * non-jar resources that are to be included in the JNLP bundle.
     *
     * @return Returns the value of the resourcesDirectory field, never null.
     */
    protected File getResourcesDirectory()
    {

        if ( resourcesDirectory == null )
        {
            resourcesDirectory = new File( getProject().getBasedir(), DEFAULT_RESOURCES_DIR );
        }

        return resourcesDirectory;

    }

    /**
     * Returns the file handle to the directory containing the Velocity templates for the JNLP
     * files to be generated.
     * @return Returns the value of the templateDirectory field.
     */
    protected File getTemplateDirectory()
    {
        return templateDirectory;
    }

    /**
     * Returns the ArtifactFactory that can be used to create artifacts that
     * need to be retrieved from maven artifact repositories.
     * @return Returns the value of the artifactFactory field.
     */
    protected ArtifactFactory getArtifactFactory()
    {
        return artifactFactory;
    }

    /**
     * Returns the ArtifactResolver that can be used to retrieve artifacts
     * from maven artifact repositories.
     * @return Returns the value of the artifactResolver field.
     */
    protected ArtifactResolver getArtifactResolver()
    {
        return artifactResolver;
    }

    /**
     * Returns the local artifact repository.
     * @return Returns the value of the localRepository field.
     */
    protected ArtifactRepository getLocalRepository()
    {
        return localRepository;
    }

    /**
     * Returns the collection of remote artifact repositories for the current
     * Maven project.
     * @return Returns the value of the remoteRepositories field.
     */
    protected List getRemoteRepositories()
    {
        return remoteRepositories;
    }

    /**
     * Returns the flag that indicates whether or not jar resources
     * will be compressed using pack200.
     *
     * @return Returns the value of the pack200 field.
     */
    public boolean isPack200()
    {
        return pack200;
    }

    /**
     * Returns jar signing configuration element.
     * @return Returns the value of the sign field.
     */
    protected SignConfig getSign()
    {
        return sign;
    }

    /**
     * Returns the flag that indicates whether or not a gzip should be
     * created for each jar resource.
     * @return Returns the value of the gzip field.
     */
    protected boolean isGzip()
    {
        return gzip;
    }

    /**
     * Returns the flag that indicates whether or not to provide verbose output.
     * @return Returns the value of the verbose field.
     */
    protected boolean isVerbose()
    {
        return verbose;
    }

    /**
     * Returns the flag that indicates whether or not jars should be verified after signing.
     * @return Returns the value of the verifyjar field.
     */
    protected boolean isVerifyjar()
    {
        return verifyjar;
    }

    /**
     * Returns the flag that indicates whether or not all transitive dependencies will be excluded
     * from the generated JNLP bundle.
     * @return Returns the value of the excludeTransitive field.
     */
    protected boolean isExcludeTransitive()
    {
        return this.excludeTransitive;
    }

    /**
     * Returns the collection of artifacts that have been modified
     * since the last time this mojo was run.
     * @return Returns the value of the modifiedJnlpArtifacts field.
     */
    protected List getModifiedJnlpArtifacts()
    {
        return modifiedJnlpArtifacts;
    }

    /**
     * Confirms that if Pack200 is enabled, the MOJO is being executed in at least a Java 1.5 JVM.
     *
     * @throws MojoExecutionException
     */
    protected void checkPack200() throws MojoExecutionException
    {
        final float javaVersion5 = 1.5f;
        if ( isPack200() && ( SystemUtils.JAVA_VERSION_FLOAT < javaVersion5 ) )
        {
            throw new MojoExecutionException(
                    "Configuration error: Pack200 compression is only available on SDK 5.0 or above." );
        }

    }

    protected void copyResources( File resourcesDir, File workDirectory ) throws IOException
    {
        if ( !resourcesDir.exists() && getLog().isInfoEnabled() )
        {
            getLog().info( "No resources found in " + resourcesDir.getAbsolutePath() );
        }
        else
        {
            if ( ! resourcesDir.isDirectory() )
            {
                getLog().debug( "Not a directory: " + resourcesDir.getAbsolutePath() );
            }
            else
            {
                getLog().debug( "Copying resources from " + resourcesDir.getAbsolutePath() );

                // hopefully available from FileUtils 1.0.5-SNAPSHOT
                //FileUtils.copyDirectoryStructure( resourcesDir , workDirectory );

                // this may needs to be parametrized somehow
                String excludes = concat( DirectoryScanner.DEFAULTEXCLUDES, ", " );
                copyDirectoryStructure( resourcesDir, workDirectory, "**", excludes );
            }

        }
    }

    private static String concat( String[] array, String delim )
    {
        StringBuffer buffer = new StringBuffer();
        for ( int i = 0; i < array.length; i++ )
        {
            if ( i > 0 )
            {
                buffer.append( delim );
            }
            String s = array[i];
            buffer.append( s ).append( delim );
        }
        return buffer.toString();
    }

    private void copyDirectoryStructure( File sourceDirectory, File destinationDirectory, String includes,
                                         String excludes )
        throws IOException
    {
        if ( !sourceDirectory.exists() )
        {
            return;
        }

        List files = FileUtils.getFiles( sourceDirectory, includes, excludes );

        for ( Iterator i = files.iterator(); i.hasNext(); )
        {
            File file = (File) i.next();

            getLog().debug( "Copying " + file + " to " + destinationDirectory );

            String path = file.getAbsolutePath().substring( sourceDirectory.getAbsolutePath().length() + 1 );

            File destDir = new File( destinationDirectory, path );

            getLog().debug( "Copying " + file + " to " + destDir );

            if ( file.isDirectory() )
            {
                destDir.mkdirs();
            }
            else
            {
                FileUtils.copyFileToDirectory( file, destDir.getParentFile() );
            }
        }
    }

    /**
     * Conditionally copy the file into the target directory.
     * The operation is not performed when the target file exists and is up to date.
     * The target file name is taken from the <code>sourceFile</code> name.
     *
     * @return <code>true</code> when the file was copied, <code>false</code> otherwise.
     * @throws IllegalArgumentException if sourceFile is <code>null</code> or
     * <code>sourceFile.getName()</code> is <code>null</code>
     * @throws IOException if an error occurs attempting to copy the file.
     */
    protected boolean copyFileToDirectoryIfNecessary( File sourceFile, File targetDirectory ) throws IOException
    {

        if ( sourceFile == null )
        {
            throw new IllegalArgumentException( "sourceFile is null" );
        }

        File targetFile = new File( targetDirectory, sourceFile.getName() );

        boolean shouldCopy = ! targetFile.exists() || ( targetFile.lastModified() < sourceFile.lastModified() );

        if ( shouldCopy )
        {
            FileUtils.copyFileToDirectory( sourceFile, targetDirectory );
        }
        else
        {
            getLog().debug( "Source file hasn't changed. Do not overwrite "
                            + targetFile + " with " + sourceFile + "." );

        }

        return shouldCopy;

    }


    /**
     * Conditionally copy the jar file into the target directory.
     * The operation is not performed when a signed target file exists and is up to date.
     * The signed target file name is taken from the <code>sourceFile</code> name.E
     * The unsigned target file name is taken from the <code>sourceFile</code> name prefixed with UNPROCESSED_PREFIX.
     * TODO this is confusing if the sourceFile is already signed. By unsigned we really mean 'unsignedbyus'
     *
     * @return <code>true</code> when the file was copied, <code>false</code> otherwise.
     * @throws IllegalArgumentException if sourceFile is <code>null</code> or
     * <code>sourceFile.getName()</code> is <code>null</code>
     * @throws IOException if an error occurs attempting to copy the file.
     */
    protected boolean copyJarAsUnprocessedToDirectoryIfNecessary( File sourceFile, File targetDirectory )
        throws IOException
    {

        if ( sourceFile == null )
        {
            throw new IllegalArgumentException( "sourceFile is null" );
        }

        File signedTargetFile = new File( targetDirectory, sourceFile.getName() );

        File unsignedTargetFile = new File( targetDirectory, UNPROCESSED_PREFIX + sourceFile.getName() );

        boolean shouldCopy = !signedTargetFile.exists()
            || ( signedTargetFile.lastModified() < sourceFile.lastModified() );

        shouldCopy = shouldCopy && ( !unsignedTargetFile.exists() 
            || ( unsignedTargetFile.lastModified() < sourceFile.lastModified() ) );

        if ( shouldCopy )
        {
            FileUtils.copyFile( sourceFile, unsignedTargetFile );
        }
        else
        {
            getLog().debug( "Source file hasn't changed. Do not reprocess "
                            + signedTargetFile + " with " + sourceFile + "." );
        }

        return shouldCopy;

    }

    /**
     * If sign is enabled, sign the jars, otherwise rename them into final jars
     */
    protected void signOrRenameJars() throws MojoExecutionException, MojoFailureException
    {

        if ( getSign() != null )
        {
            getSign().init( getLog(), getWorkDirectory(), isVerbose() );

            if ( unsignAlreadySignedJars() )
            {
                removeExistingSignatures( getLibDirectory(), unprocessedJarFileFilter );
            }

            if ( isPack200() )
            {
                // http://java.sun.com/j2se/1.5.0/docs/guide/deployment/deployment-guide/pack200.html
                // we need to pack then unpack the files before signing them
                Pack200.packJars( getLibDirectory(), unprocessedJarFileFilter, isGzip() );
                Pack200.unpackJars( getLibDirectory(), unprocessedPack200FileFilter );
                // As out current Pack200 ant tasks don't give us the ability to use a temporary area for
                // creating those temporary packing, we have to delete the temporary files.
                deleteFiles( getLibDirectory(), unprocessedPack200FileFilter );
                // specs says that one should do it twice when there are unsigned jars??
                // Pack200.unpackJars( applicationDirectory, updatedPack200FileFilter );
            }

            int signedJars = signJars( getLibDirectory(), unprocessedJarFileFilter );

            if ( signedJars != getModifiedJnlpArtifacts().size() )
            {
                throw new IllegalStateException(
                        "The number of signed artifacts (" + signedJars + ") differ from the number of modified "
                        + "artifacts (" + getModifiedJnlpArtifacts().size() + "). Implementation error" );
            }

        } 
        else 
        {
            makeUnprocessedFilesFinal( getLibDirectory(), unprocessedJarFileFilter );   
        }
    }

    private int makeUnprocessedFilesFinal( File directory, FileFilter fileFilter ) throws MojoExecutionException
    {
        File[] jarFiles = directory.listFiles( fileFilter );

        if ( getLog().isDebugEnabled() )
        {
            getLog().debug( "makeUnprocessedFilesFinal in " + directory + " found " 
                            + jarFiles.length + " file(s) to rename" );
        }

        if ( jarFiles.length == 0 )
        {
            return 0;
        }

        for ( int i = 0; i < jarFiles.length; i++ )
        {
            String unprocessedJarFileName = jarFiles[i].getName();
            if ( !unprocessedJarFileName.startsWith( UNPROCESSED_PREFIX ) )
            {
                throw new IllegalStateException( "We are about to sign an non " + UNPROCESSED_PREFIX
                                                 + " file with path: " + jarFiles[i].getAbsolutePath() );
            }
            File finalJar = new File( jarFiles[i].getParent(), 
                                      unprocessedJarFileName.substring( UNPROCESSED_PREFIX.length() ) );
            if ( finalJar.exists() )
            {
                boolean deleted = finalJar.delete();
                if ( !deleted )
                {
                    throw new IllegalStateException( "Couldn't delete obsolete final jar: " 
                                                     + finalJar.getAbsolutePath() );
                }
            }
            boolean renamed = jarFiles[i].renameTo( finalJar );
            if ( !renamed )
            {
                throw new IllegalStateException( "Couldn't rename into final jar: " + finalJar.getAbsolutePath() );
            }
        }
        return jarFiles.length;
    } 

    /**
     * @return the number of deleted files
     */
    private int deleteFiles( File directory, FileFilter fileFilter ) throws MojoExecutionException
    {
        File[] files = directory.listFiles( fileFilter );

        if ( getLog().isDebugEnabled() )
        {
            getLog().debug( "deleteFiles in " + directory + " found " + files.length + " file(s) to delete" );
        }

        if ( files.length == 0 )
        {
            return 0;
        }

        for ( int i = 0; i < files.length; i++ )
        {
            boolean deleted = files[i].delete();
            if ( !deleted )
            {
                throw new IllegalStateException( "Couldn't delete file: " + files[i].getAbsolutePath() );
            } 
        }
        return files.length;
    }

    /**
     * @return the number of signed jars
     */
    private int signJars( File directory, FileFilter fileFilter ) throws MojoExecutionException, MojoFailureException
    {

        File[] jarFiles = directory.listFiles( fileFilter );

        if ( getLog().isDebugEnabled() )
        {
            getLog().debug( "signJars in " + directory + " found " + jarFiles.length + " jar(s) to sign" );
        }

        if ( jarFiles.length == 0 )
        {
            return 0;
        }

        JarSignerMojo jarSigner = getSign().getJarSignerMojo();

        for ( int i = 0; i < jarFiles.length; i++ )
        {
            String unprocessedJarFileName = jarFiles[i].getName();
            if ( !unprocessedJarFileName.startsWith( UNPROCESSED_PREFIX ) )
            {
                throw new IllegalStateException( "We are about to sign an non " + UNPROCESSED_PREFIX
                                                 + " file with path: " + jarFiles[i].getAbsolutePath() );
            }
            jarSigner.setJarPath( jarFiles[i] );

            File signedJar = new File( jarFiles[i].getParent(), 
                                       unprocessedJarFileName.substring( UNPROCESSED_PREFIX.length() ) );

            jarSigner.setSignedJar( signedJar );

            if ( signedJar.exists() )
            {
                if ( !signedJar.delete() )
                {
                    throw new IllegalStateException( "Couldn't delete obsolete signed jar: " 
                                                     + signedJar.getAbsolutePath() );
                } 
            }
            jarSigner.execute();
            getLog().debug( "lastModified signedJar:" + signedJar.lastModified() + " unprocessed signed Jar:" 
                            + jarFiles[i].lastModified() );

            // remove unprocessed files
            // TODO wouldn't have to do that if we copied the unprocessed jar files in a temporary area
            if ( !jarFiles[i].delete() )
            {
                throw new IllegalStateException( "Couldn't delete obsolete unprocessed jar: " 
                                                 + jarFiles[i].getAbsolutePath() );
            } 
        }

        return jarFiles.length;
    }

    protected URL findDefaultJnlpTemplateURL()
    {
        URL url = this.getClass().getClassLoader().getResource( "default-jnlp-template.vm" );
        return url;
    }

    protected URL getWebstartJarURL()
    {
        String url = findDefaultJnlpTemplateURL().toString();
        try
        {
            return new URL( url.substring( "jar:".length(), url.indexOf( "!" ) ) );
        } 
        catch ( Exception e )
        {
            IllegalStateException iae = new IllegalStateException( "Failure to find webstart Jar URL: " 
                                                                   + e.getMessage() );
            iae.initCause( e );
            throw iae;
        }
    }

    /** @return something of the form jar:file:..../webstart-maven-plugin-.....jar!/ */
    protected String getWebstartJarURLForVelocity()
    {
        String url = findDefaultJnlpTemplateURL().toString();
        return  url.substring( 0, url.indexOf( "!" ) + 2 );
    }

    /**
     * Removes the signature of the files in the specified directory which satisfy the 
     * specified filter.
     *  
     * @return the number of unsigned jars
     */
    protected int removeExistingSignatures( File workDirectory, FileFilter updatedJarFileFilter ) 
        throws MojoExecutionException
    {
        verboseLog( "Start removing existing signatures" );
        // cleanup tempDir if exists
        File tempDir = new File( workDirectory, "temp_extracted_jars" );
        removeDirectory( tempDir );
        
        // recreate temp dir
        if ( !tempDir.mkdirs() ) 
        {
            throw new MojoExecutionException( "Error creating temporary directory: " + tempDir );
        }        
        
        // process jars
        File[] jarFiles = workDirectory.listFiles( updatedJarFileFilter );

        for ( int i = 0; i < jarFiles.length; i++ )
        {
            if ( isJarSigned( jarFiles[i] ) )
            {
                verboseLog( "remove signature from : " + jarFiles[i] );
                unsignJarFile( jarFiles[i], tempDir );
            } 
            else
            {
                verboseLog( "not signed : " + jarFiles[i] );
            }
        }

        // cleanup tempDir
        removeDirectory( tempDir );

        return jarFiles.length; // FIXME this is wrong. Not all jars are signed.
    }
    
    private boolean isJarSigned( File jarFile )
    {
        JarSignVerifyMojo verifyMojo = setupVerifyMojo();
        verifyMojo.setJarPath( jarFile );
        try
        {
            verifyMojo.execute();
            return true;
        } 
        catch ( MojoExecutionException e ) 
        {
            return false;
        }
    }
    
    private void unsignJarFile( File jarFile, File tempDir ) throws MojoExecutionException
    {
        JarUnsignMojo unsignJar = new JarUnsignMojo();
        unsignJar.setTempDir( tempDir );
        unsignJar.setVerbose( isVerbose() );
        unsignJar.setArchiverManager( archiverManager );
        unsignJar.setJarPath( jarFile );
        unsignJar.execute();
    }
    
    /**
     * Returns a configured instance of the JarSignVerifyMojo to test whether a
     * jar is already signed. The Mojo throws an exception to indicate that a
     * jar is not signed yet.
     * 
     * @return a configured instance of the JarSignVerifyMojo.
     */
    private JarSignVerifyMojo setupVerifyMojo()
    {
        JarSignVerifyMojo verifyMojo = new JarSignVerifyMojo();
        verifyMojo.setErrorWhenNotSigned( true );
        verifyMojo.setWorkingDir( getWorkDirectory() );
        return verifyMojo;
    }

    /**
     * This is to try to workaround an issue with setting setLastModified.
     * See MWEBSTART-28. May be removed later on if that doesn't help.
     */
    /*
    private boolean setLastModified( File file, long timestamp )
    {
        boolean result;
        int nbretries = 3;

        while ( ! (result = file.setLastModified( timestamp )) && ( nbretries-- > 0 ) )
        {
            getLog().warn("failure to change last modified timestamp... retrying ... " 
                    + "See MWEBSTART-28. (especially if you're on NFS).");

            try
            {
                Thread.sleep( 4000 );
            }
            catch (InterruptedException ignore) {
                //TODO should not be ignoring, because this class doesn't control the Thread policy
            }

        }

        return result;

    }
    */

    protected void packJars()
    {

        if ( isPack200() )
        {
            getLog().debug( "packing jars" );
            Pack200.packJars( getWorkDirectory(), processedJarFileFilter, isGzip() );
        }

    }

    /**
     * TODO finish comment
     *
     * @param artifact
     * @param mainClass
     * @return
     * @throws MalformedURLException
     */
    protected boolean artifactContainsClass( Artifact artifact, final String mainClass ) throws MalformedURLException
    {
        boolean containsClass = true;

        // JarArchiver.grabFilesAndDirs()
        ClassLoader cl = new java.net.URLClassLoader( new URL[]{ artifact.getFile().toURI().toURL() } );
        Class c = null;
        try
        {
            c = Class.forName( mainClass, false, cl );
        }
        catch ( ClassNotFoundException e )
        {
            getLog().debug( "artifact " + artifact + " doesn't contain the main class: " + mainClass );
            containsClass = false;
        }
        catch ( Throwable t )
        {
            getLog().info( "artifact " + artifact + " seems to contain the main class: " + mainClass 
                           + " but the jar doesn't seem to contain all dependencies " + t.getMessage() );
        }

        if ( c != null )
        {
            getLog().debug( "Checking if the loaded class contains a main method." );

            try
            {
                c.getMethod( "main", new Class[]{String[].class} );
            }
            catch ( NoSuchMethodException e )
            {
                getLog().warn( "The specified main class (" + mainClass 
                               + ") doesn't seem to contain a main method... " 
                               + "Please check your configuration." + e.getMessage() );
            }
            catch ( NoClassDefFoundError e )
            {
                // undocumented in SDK 5.0. is this due to the ClassLoader lazy loading the Method 
                // thus making this a case tackled by the JVM Spec (Ref 5.3.5)!
                // Reported as Incident 633981 to Sun just in case ...
                getLog().warn( "Something failed while checking if the main class contains the main() method. " 
                               + "This is probably due to the limited classpath we have provided to the class loader. " 
                               + "The specified main class (" + mainClass 
                               + ") found in the jar is *assumed* to contain a main method... " + e.getMessage() );
            }
            catch ( Throwable t )
            {
                getLog().error( "Unknown error: Couldn't check if the main class has a main method. " 
                                + "The specified main class (" + mainClass 
                                + ") found in the jar is *assumed* to contain a main method...", t );
            }
        }

        return containsClass;
    }
    
    /**
     * anonymous to inner to work-around qdox 1.6.1 bug (MPLUGIN-26)
     */
    private static class UnprocessedPack200FileFilter implements FileFilter 
    {
        public boolean accept( File pathname )
        {
            return pathname.isFile() && pathname.getName().startsWith( UNPROCESSED_PREFIX ) 
                && ( pathname.getName().endsWith( ".jar.pack.gz" ) || pathname.getName().endsWith( ".jar.pack" ) );
        }

    };

    /**
     * 
     * @return true if already signed jars should be unsigned prior to signing
     *         with own key.
     */
    protected boolean unsignAlreadySignedJars()
    {
        return unsignAlreadySignedJars;
    }

    /**
     * Delete the specified directory.
     * 
     * @param dir
     *            the directory to delete
     * @throws MojoExecutionException
     */
    private void removeDirectory( File dir ) throws MojoExecutionException
    {
        if ( dir != null )
        {
            if ( dir.exists() && dir.isDirectory() )
            {
                getLog().info( "Deleting directory " + dir.getAbsolutePath() );
                Utils.removeDir( dir );
            }
        }
    }

    /**
     * Log as info when verbose or info is enabled, as debug otherwise.
     */
    protected void verboseLog( String msg )
    {
        infoOrDebug( isVerbose() || getLog().isInfoEnabled(), msg );
    }

    /** if info is true, log as info(), otherwise as debug() */
    private void infoOrDebug( boolean info , String msg )
    {
        if ( info )
        {
            getLog().info( msg );
        }
        else
        {
            getLog().debug( msg );
        }
    }

}
