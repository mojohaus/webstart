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

import org.apache.commons.lang.SystemUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * The superclass for all JNLP generating MOJOs.
 *
 * @author Kevin Stembridge
 * @author $LastChangedBy$
 * @version $Revision$
 * @since 28 May 2007
 */
public abstract class AbstractBaseJnlpMojo
    extends AbstractMojo
{

    private static final String DEFAULT_RESOURCES_DIR = "src/main/jnlp/resources";

    /**
     * unprocessed files (that will be signed) are prefixed with this
     */
    private static final String UNPROCESSED_PREFIX = "unprocessed_";

    public static final String JAR_SUFFIX = ".jar";

    /**
     * Artifact resolver, needed to download source jars for inclusion in classpath.
     *
     * @component
     * @required
     * @readonly
     */
    private ArtifactResolver artifactResolver;

    /**
     * JarSigner tool.
     *
     * @component role="org.codehaus.mojo.webstart.SignTool"
     * @required
     * @readonly
     */
    private SignTool signTool;

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

    /**
     * Flag to create the archive or not.
     *
     * @parameter default-value="true"
     * @since 1.0-beta-2
     */
    private boolean makeArchive;

    /**
     * Flag to attach the archive or not to the project's build.
     *
     * @parameter default-value="true"
     * @since 1.0-beta-2
     */
    private boolean attachArchive;

    /**
     * The code base to use on the generated jnlp files.
     *
     * @parameter expression="${jnlp.codebase}" default-value="${project.url}/jnlp"
     * @since 1.0-beta-2
     */
    private String codebase;

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
     * To authorize or not to unsign some already signed jar.
     * <p/>
     * If set to false and the {@code unsign} parameter is set to {@code true} then the build will fail if there is
     * a jar to unsign, to avoid this use then the extension jnlp component.
     *
     * @parameter default-value="true"
     * @since 1.0-beta-2
     */
    private boolean canUnsign;

//    /**
//     * To look up Archiver/UnArchiver implementations
//     *
//     * @component
//     * @required
//     */
//    protected ArchiverManager archiverManager;

    /**
     * All available pack200 tools.
     * <p/>
     * We use a plexus list injection instead of a direct component injection since for a jre 1.4, we will at the
     * moment have no implementation of this tool.
     * <p/>
     * Later in the execute of mojo, we will check if at least one implementation is available if required.
     *
     * @component role="org.codehaus.mojo.webstart.Pack200Tool"
     * @since 1.0-beta-2
     */
    private List pack200Tools;

    /**
     * Creates a new {@code AbstractBaseJnlpMojo}.
     */
    public AbstractBaseJnlpMojo()
    {

        processedJarFileFilter = new FileFilter()
        {
            public boolean accept( File pathname )
            {
                return pathname.isFile() && pathname.getName().endsWith( JAR_SUFFIX ) &&
                    !pathname.getName().startsWith( UNPROCESSED_PREFIX );
            }
        };

        unprocessedJarFileFilter = new FileFilter()
        {
            public boolean accept( File pathname )
            {
                return pathname.isFile() && pathname.getName().startsWith( UNPROCESSED_PREFIX ) &&
                    pathname.getName().endsWith( JAR_SUFFIX );
            }
        };

        unprocessedPack200FileFilter = new UnprocessedPack200FileFilter();
    }

    public abstract MavenProject getProject();

    /**
     * Returns the library path. This is ths subpath within the working directory, where the libraries are placed.
     * If the path is not configured it is <code>null</code>.
     *
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
     * Returns the flag that indicates whether or not jar resources
     * will be compressed using pack200.
     *
     * @return Returns the value of the pack200 field.
     */
    public boolean isPack200()
    {
        return pack200;
    }

    protected void makeWorkingDirIfNecessary()
        throws MojoExecutionException
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

    /**
     * Returns the working directory. This is the directory in which files and resources
     * will be placed in order to be processed prior to packaging.
     *
     * @return Returns the value of the workDirectory field.
     */
    protected File getWorkDirectory()
    {
        return workDirectory;
    }

    /**
     * Returns the library directory. If not libPath is configured, the working directory is returned.
     *
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
     *
     * @return Returns the value of the templateDirectory field.
     */
    protected File getTemplateDirectory()
    {
        return templateDirectory;
    }

    /**
     * Returns the ArtifactFactory that can be used to create artifacts that
     * need to be retrieved from maven artifact repositories.
     *
     * @return Returns the value of the artifactFactory field.
     */
    protected ArtifactFactory getArtifactFactory()
    {
        return artifactFactory;
    }

    /**
     * Returns the ArtifactResolver that can be used to retrieve artifacts
     * from maven artifact repositories.
     *
     * @return Returns the value of the artifactResolver field.
     */
    protected ArtifactResolver getArtifactResolver()
    {
        return artifactResolver;
    }

    /**
     * Returns the local artifact repository.
     *
     * @return Returns the value of the localRepository field.
     */
    protected ArtifactRepository getLocalRepository()
    {
        return localRepository;
    }

    /**
     * Returns the collection of remote artifact repositories for the current
     * Maven project.
     *
     * @return Returns the value of the remoteRepositories field.
     */
    protected List getRemoteRepositories()
    {
        return remoteRepositories;
    }

    /**
     * Returns jar signing configuration element.
     *
     * @return Returns the value of the sign field.
     */
    protected SignConfig getSign()
    {
        return sign;
    }

    /**
     * Returns the code base to inject in the generated jnlp.
     *
     * @return Returns the value of codebase field.
     */
    protected String getCodebase()
    {
        return codebase;
    }

    /**
     * Returns the flag that indicates whether or not a gzip should be
     * created for each jar resource.
     *
     * @return Returns the value of the gzip field.
     */
    protected boolean isGzip()
    {
        return gzip;
    }

    /**
     * Returns the flag that indicates whether or not to provide verbose output.
     *
     * @return Returns the value of the verbose field.
     */
    protected boolean isVerbose()
    {
        return verbose;
    }

    /**
     * Returns the flag that indicates whether or not jars should be verified after signing.
     *
     * @return Returns the value of the verifyjar field.
     */
    protected boolean isVerifyjar()
    {
        return verifyjar;
    }

    /**
     * Returns the flag that indicates whether or not all transitive dependencies will be excluded
     * from the generated JNLP bundle.
     *
     * @return Returns the value of the excludeTransitive field.
     */
    protected boolean isExcludeTransitive()
    {
        return this.excludeTransitive;
    }

    /**
     * Returns the flag indicates whether or not archive must be build.
     *
     * @return Returns the value of the makeArchive field.
     */
    protected boolean isMakeArchive()
    {
        return makeArchive;
    }

    /**
     * Returns the flag indicates whether or not archive must be deployed.
     *
     * @return Returns the value of the attachArchive field.
     */
    protected boolean isAttachArchive()
    {
        return attachArchive;
    }

    protected boolean isCanUnsign()
    {
        return canUnsign;
    }

    /**
     * Returns the collection of artifacts that have been modified
     * since the last time this mojo was run.
     *
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
    protected void checkPack200()
        throws MojoExecutionException
    {
        if ( !isPack200() )
        {

            // pack 200 is not required, so no check to do
            return;
        }

        final float javaVersion5 = 1.5f;
        if ( SystemUtils.JAVA_VERSION_FLOAT < javaVersion5 )
        {
            throw new MojoExecutionException(
                "Configuration error: Pack200 compression is only available on SDK 5.0 or above." );
        }

        // check the pack200Tool exists
        if ( pack200Tools.isEmpty() )
        {
            throw new MojoExecutionException( "Configuration error: No Pack200Tool found." );
        }
    }

    protected void copyResources( File resourcesDir, File workDirectory )
        throws IOException
    {
        if ( !resourcesDir.exists() && getLog().isInfoEnabled() )
        {
            getLog().info( "No resources found in " + resourcesDir.getAbsolutePath() );
        }
        else
        {
            if ( !resourcesDir.isDirectory() )
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

    /**
     * Conditionally copy the file into the target directory.
     * The operation is not performed when the target file exists and is up to date.
     * The target file name is taken from the <code>sourceFile</code> name.
     *
     * @param sourceFile
     * @param targetDirectory
     * @return <code>true</code> when the file was copied, <code>false</code> otherwise.
     * @throws IllegalArgumentException if sourceFile is <code>null</code> or
     *                                  <code>sourceFile.getName()</code> is <code>null</code>
     * @throws IOException              if an error occurs attempting to copy the file.
     */
    protected boolean copyFileToDirectoryIfNecessary( File sourceFile, File targetDirectory )
        throws IOException
    {

        if ( sourceFile == null )
        {
            throw new IllegalArgumentException( "sourceFile is null" );
        }

        File targetFile = new File( targetDirectory, sourceFile.getName() );

        boolean shouldCopy = !targetFile.exists() || ( targetFile.lastModified() < sourceFile.lastModified() );

        if ( shouldCopy )
        {
            FileUtils.copyFileToDirectory( sourceFile, targetDirectory );
        }
        else
        {
            getLog().debug(
                "Source file hasn't changed. Do not overwrite " + targetFile + " with " + sourceFile + "." );

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
     * @param sourceFile
     * @param targetDirectory
     * @return <code>true</code> when the file was copied, <code>false</code> otherwise.
     * @throws IllegalArgumentException if sourceFile is <code>null</code> or
     *                                  <code>sourceFile.getName()</code> is <code>null</code>
     * @throws IOException              if an error occurs attempting to copy the file.
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

        boolean shouldCopy =
            !signedTargetFile.exists() || ( signedTargetFile.lastModified() < sourceFile.lastModified() );

        shouldCopy = shouldCopy &&
            ( !unsignedTargetFile.exists() || ( unsignedTargetFile.lastModified() < sourceFile.lastModified() ) );

        if ( shouldCopy )
        {
            FileUtils.copyFile( sourceFile, unsignedTargetFile );
        }
        else
        {
            getLog().debug(
                "Source file hasn't changed. Do not reprocess " + signedTargetFile + " with " + sourceFile + "." );
        }

        return shouldCopy;
    }


    /**
     * If sign is enabled, sign the jars, otherwise rename them into final jars
     *
     * @throws org.apache.maven.plugin.MojoExecutionException
     *
     * @throws org.apache.maven.plugin.MojoFailureException
     *
     */
    protected void signOrRenameJars()
        throws MojoExecutionException, MojoFailureException
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
                getPack200Tool().packJars( getLibDirectory(), unprocessedJarFileFilter, isGzip() );
                getPack200Tool().unpackJars( getLibDirectory(), unprocessedPack200FileFilter );
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
                    "The number of signed artifacts (" + signedJars + ") differ from the number of modified " +
                        "artifacts (" + getModifiedJnlpArtifacts().size() + "). Implementation error" );
            }

        }
        else
        {
            makeUnprocessedFilesFinal( getLibDirectory(), unprocessedJarFileFilter );
        }
    }

    protected URL findDefaultJnlpTemplateURL()
    {
        return getClass().getClassLoader().getResource( "default-jnlp-template.vm" );
    }

    protected URL findDefaultJnlpExtensionTemplateURL()
    {
        return getClass().getClassLoader().getResource( "default-jnlp-extension-template.vm" );
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
            IllegalStateException iae =
                new IllegalStateException( "Failure to find webstart Jar URL: " + e.getMessage() );
            iae.initCause( e );
            throw iae;
        }
    }

    /**
     * @return something of the form jar:file:..../webstart-maven-plugin-.....jar!/
     */
    protected String getWebstartJarURLForVelocity()
    {
        String url = findDefaultJnlpTemplateURL().toString();
        return url.substring( 0, url.indexOf( "!" ) + 2 );
    }

    protected boolean isJarSigned( File jarFile )
        throws MojoExecutionException
    {

        return signTool.isJarSigned( getSign(), jarFile );
    }

    protected void packJars()
    {

        if ( isPack200() )
        {
            getLog().debug( "packing jars" );
            getPack200Tool().packJars( getLibDirectory(), processedJarFileFilter, isGzip() );
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
    protected boolean artifactContainsClass( Artifact artifact, final String mainClass )
        throws MalformedURLException
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
            getLog().info( "artifact " + artifact + " seems to contain the main class: " + mainClass +
                               " but the jar doesn't seem to contain all dependencies " + t.getMessage() );
        }

        if ( c != null )
        {
            getLog().debug( "Checking if the loaded class contains a main method." );

            try
            {
                c.getMethod( "main", new Class[]{ String[].class } );
            }
            catch ( NoSuchMethodException e )
            {
                getLog().warn(
                    "The specified main class (" + mainClass + ") doesn't seem to contain a main method... " +
                        "Please check your configuration." + e.getMessage() );
            }
            catch ( NoClassDefFoundError e )
            {
                // undocumented in SDK 5.0. is this due to the ClassLoader lazy loading the Method
                // thus making this a case tackled by the JVM Spec (Ref 5.3.5)!
                // Reported as Incident 633981 to Sun just in case ...
                getLog().warn( "Something failed while checking if the main class contains the main() method. " +
                                   "This is probably due to the limited classpath we have provided to the class loader. " +
                                   "The specified main class (" + mainClass +
                                   ") found in the jar is *assumed* to contain a main method... " + e.getMessage() );
            }
            catch ( Throwable t )
            {
                getLog().error( "Unknown error: Couldn't check if the main class has a main method. " +
                                    "The specified main class (" + mainClass +
                                    ") found in the jar is *assumed* to contain a main method...", t );
            }
        }

        return containsClass;
    }

    /**
     * @return true if already signed jars should be unsigned prior to signing
     *         with own key.
     */
    protected boolean unsignAlreadySignedJars()
    {
        return unsignAlreadySignedJars;
    }

    protected Pack200Tool getPack200Tool()
    {
        return (Pack200Tool) pack200Tools.get( 0 );
    }

    /**
     * Log as info when verbose or info is enabled, as debug otherwise.
     *
     * @param msg the message to display
     */
    protected void verboseLog( String msg )
    {
        infoOrDebug( isVerbose() || getLog().isInfoEnabled(), msg );
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

    private int makeUnprocessedFilesFinal( File directory, FileFilter fileFilter )
        throws MojoExecutionException
    {
        File[] jarFiles = directory.listFiles( fileFilter );

        if ( getLog().isDebugEnabled() )
        {
            getLog().debug(
                "makeUnprocessedFilesFinal in " + directory + " found " + jarFiles.length + " file(s) to rename" );
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
                throw new IllegalStateException(
                    "We are about to sign an non " + UNPROCESSED_PREFIX + " file with path: " +
                        jarFiles[i].getAbsolutePath() );
            }
            File finalJar =
                new File( jarFiles[i].getParent(), unprocessedJarFileName.substring( UNPROCESSED_PREFIX.length() ) );
            if ( finalJar.exists() )
            {
                boolean deleted = finalJar.delete();
                if ( !deleted )
                {
                    throw new IllegalStateException(
                        "Couldn't delete obsolete final jar: " + finalJar.getAbsolutePath() );
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
     * @param directory
     * @param fileFilter
     * @return the number of deleted files
     * @throws org.apache.maven.plugin.MojoExecutionException
     *
     */
    private int deleteFiles( File directory, FileFilter fileFilter )
        throws MojoExecutionException
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
     * @param directory
     * @param fileFilter
     * @return the number of signed jars
     * @throws org.apache.maven.plugin.MojoExecutionException
     *
     * @throws org.apache.maven.plugin.MojoFailureException
     *
     */
    private int signJars( File directory, FileFilter fileFilter )
        throws MojoExecutionException, MojoFailureException
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

        for ( int i = 0; i < jarFiles.length; i++ )
        {
            String unprocessedJarFileName = jarFiles[i].getName();
            if ( !unprocessedJarFileName.startsWith( UNPROCESSED_PREFIX ) )
            {
                throw new IllegalStateException(
                    "We are about to sign an non " + UNPROCESSED_PREFIX + " file with path: " +
                        jarFiles[i].getAbsolutePath() );
            }

            File signedJar =
                new File( jarFiles[i].getParent(), unprocessedJarFileName.substring( UNPROCESSED_PREFIX.length() ) );

            if ( signedJar.exists() )
            {
                if ( !signedJar.delete() )
                {
                    throw new IllegalStateException(
                        "Couldn't delete obsolete signed jar: " + signedJar.getAbsolutePath() );
                }
            }

            signTool.sign( getSign(), jarFiles[i], signedJar );

            getLog().debug( "lastModified signedJar:" + signedJar.lastModified() + " unprocessed signed Jar:" +
                                jarFiles[i].lastModified() );

            // remove unprocessed files
            // TODO wouldn't have to do that if we copied the unprocessed jar files in a temporary area
            if ( !jarFiles[i].delete() )
            {
                throw new IllegalStateException(
                    "Couldn't delete obsolete unprocessed jar: " + jarFiles[i].getAbsolutePath() );
            }
        }

        return jarFiles.length;
    }

    /**
     * Removes the signature of the files in the specified directory which satisfy the
     * specified filter.
     *
     * @param workDirectory
     * @param updatedJarFileFilter
     * @return the number of unsigned jars
     * @throws org.apache.maven.plugin.MojoExecutionException
     *
     */
    private int removeExistingSignatures( File workDirectory, FileFilter updatedJarFileFilter )
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

        boolean canUnsign = isCanUnsign();

        for ( int i = 0; i < jarFiles.length; i++ )
        {
            if ( isJarSigned( jarFiles[i] ) )
            {
                if ( !canUnsign )
                {
                    throw new MojoExecutionException(
                        "neverUnsignAlreadySignedJar is set to true and a jar file [" + jarFiles[i] +
                            " was asked to be unsign,\n please prefer use in this case an extension for " +
                            "signed jars or not set to true the neverUnsignAlreadySignedJar parameter, Make " +
                            "your choice:)" );
                }
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

    private void unsignJarFile( File jarFile, File tempDir )
        throws MojoExecutionException
    {
        signTool.unsign( jarFile, tempDir, isVerbose() );
//        JarUnsignMojo unsignJar = new JarUnsignMojo();
//        unsignJar.setTempDir( tempDir );
//        unsignJar.setVerbose( isVerbose() );
//        unsignJar.setArchiverManager( archiverManager );
//        unsignJar.setJarPath( jarFile );
//        unsignJar.execute();
    }

    /**
     * anonymous to inner to work-around qdox 1.6.1 bug (MPLUGIN-26)
     */
    private static class UnprocessedPack200FileFilter
        implements FileFilter
    {

        public boolean accept( File pathname )
        {
            return pathname.isFile() && pathname.getName().startsWith( UNPROCESSED_PREFIX ) &&
                ( pathname.getName().endsWith( ".jar.pack.gz" ) || pathname.getName().endsWith( ".jar.pack" ) );
        }

    }

    /**
     * Delete the specified directory.
     *
     * @param dir the directory to delete
     * @throws MojoExecutionException
     */
    private void removeDirectory( File dir )
        throws MojoExecutionException
    {
        if ( dir != null )
        {
            if ( dir.exists() && dir.isDirectory() )
            {
                getLog().info( "Deleting directory " + dir.getAbsolutePath() );
                try
                {
                    FileUtils.deleteDirectory( dir );
                }
                catch ( IOException e )
                {
                    throw new MojoExecutionException( "Could not delete directory " + dir, e );
                }
            }
        }
    }

    /**
     * if info is true, log as info(), otherwise as debug()
     *
     * @param info flag to display log as info if setted to {@code true}.
     * @param msg  the message to display
     */
    private void infoOrDebug( boolean info, String msg )
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
