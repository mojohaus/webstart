package org.codehaus.mojo.webstart;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License" );
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.artifact.resolver.filter.AndArtifactFilter;
import org.apache.maven.artifact.resolver.filter.IncludesArtifactFilter;
import org.apache.maven.artifact.resolver.filter.ExcludesArtifactFilter;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.PluginManager;
import org.apache.maven.plugin.jar.JarSignMojo;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.apache.maven.settings.Settings;

import org.codehaus.mojo.keytool.GenkeyMojo;
import org.codehaus.mojo.webstart.generator.Generator;
import org.codehaus.plexus.archiver.manager.ArchiverManager;
import org.codehaus.plexus.archiver.zip.ZipArchiver;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.DirectoryScanner;

import org.apache.commons.lang.SystemUtils;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * @author <a href="jerome@coffeebreaks.org">Jerome Lacoste</a>
 * @version $Id: JnlpMojo.java 2897 2007-01-02 12:55:00Z lacostej $
 *
 * @todo refactor the common code with javadoc plugin
 * @todo how to propagate the -X argument to enable verbose?
 * @todo initialize the jnlp alias and dname.o from pom.artifactId and pom.organization.name
 */
public abstract class AbstractJnlpMojo
    extends AbstractMojo
{

    public abstract MavenProject getProject();

    /**
     * Directory to create the resulting artifacts
     *
     * @parameter expression="${project.build.directory}/jnlp"
     * @required
     */
    protected File workDirectory;

    /**
     * The Zip archiver.
     *
     * @parameter expression="${component.org.codehaus.plexus.archiver.Archiver#zip}"
     * @required
     */
    private ZipArchiver zipArchiver;

    /**
     * To look up Archiver/UnArchiver implementations
     *
     * @parameter expression="${component.org.codehaus.plexus.archiver.manager.ArchiverManager}"
     * @required
     */
    protected ArchiverManager archiverManager;    

    /**
     * Xxx
     *
     * @parameter
     */
    private JnlpConfig jnlp;

    /**
     * [optional] transitive dependencies filter - if omitted, the plugin will include all transitive dependencies. Provided and test scope dependencies are always excluded.
     *
     * @parameter
     */
    private Dependencies dependencies;

    public static class Dependencies
    {
        private List includes;

        private List excludes;

        public List getIncludes()
        {
            return includes;
        }

        public void setIncludes( List includes )
        {
            this.includes = includes;
        }

        public List getExcludes()
        {
            return excludes;
        }

        public void setExcludes( List excludes )
        {
            this.excludes = excludes;
        }
    }

    /**
     * The Sign Config
     *
     * @parameter implementation="org.codehaus.mojo.webstart.JarSignMojoConfig"
     */
    private SignConfig sign;

    /**
     * A placeholder for an obsoleted configuration element.
     *
     * This dummy parameter is here to force the plugin configuration to fail in case one
     * didn't properly migrate from 1.0-alpha-1 to 1.0-alpha-2 configuration.
     *
     * It will be removed before 1.0.
     * @parameter
     **/
    private String keystore;

    /**
     * Xxx
     *
     * @parameter default-value="false"
     */
    // private boolean usejnlpservlet;

    /**
     * Xxx
     *
     * @parameter default-value="true"
     */
    private boolean verifyjar;

    /**
     * Enables pack200. Requires SDK 5.0.
     *
     * @parameter default-value="false"
     */
    private boolean pack200;

    /**
     * Xxx
     *
     * @parameter default-value="false"
     */
    private boolean gzip;

    /**
     * Enable verbose.
     *
     * @parameter expression="${verbose}" default-value="false"
     */
    private boolean verbose;

    /**
     * @parameter expression="${basedir}"
     * @required
     * @readonly
     */
    private File basedir;

    /**
     * Artifact resolver, needed to download source jars for inclusion in classpath.
     *
     * @parameter expression="${component.org.apache.maven.artifact.resolver.ArtifactResolver}"
     * @required
     * @readonly
     * @todo waiting for the component tag
     */
    private ArtifactResolver artifactResolver;

    /**
     * Artifact factory, needed to download source jars for inclusion in classpath.
     *
     * @parameter expression="${component.org.apache.maven.artifact.factory.ArtifactFactory}"
     * @required
     * @readonly
     * @todo waiting for the component tag
     */
    private ArtifactFactory artifactFactory;

    /**
     * @parameter expression="${localRepository}"
     * @required
     * @readonly
     */
    protected ArtifactRepository localRepository;

    /**
     * @parameter expression="${component.org.apache.maven.project.MavenProjectHelper}
     */
    private MavenProjectHelper projectHelper;

    /**
     * The current user system settings for use in Maven. This is used for
     * <br/>
     * plugin manager API calls.
     *
     * @parameter expression="${settings}"
     * @required
     * @readonly
     */
    private Settings settings;

    /**
     * The plugin manager instance used to resolve plugin descriptors.
     *
     * @component role="org.apache.maven.plugin.PluginManager"
     */
    private PluginManager pluginManager;

    private class CompositeFileFilter
        implements FileFilter
    {
        private List fileFilters = new ArrayList();

        CompositeFileFilter( FileFilter filter1, FileFilter filter2 )
        {
            fileFilters.add( filter1 );
            fileFilters.add( filter2 );
        }

        public boolean accept( File pathname )
        {
            for ( int i = 0; i < fileFilters.size(); i++ )
            {
                if ( ! ( (FileFilter) fileFilters.get( i ) ).accept( pathname ) )
                {
                    return false;
                }
            }
            return true;
        }
    }

    class ModifiedFileFilter implements FileFilter
    {
        public boolean accept( File pathname )
        {
            boolean modified = pathname.lastModified() > getStartTime();
            getLog().debug( "File: " + pathname.getName() + " modified: " + modified );
            getLog().debug( "lastModified: " + pathname.lastModified() + " plugin start time " + getStartTime() );
            return modified;
        }
    };
    private FileFilter modifiedFileFilter = new ModifiedFileFilter();

    private FileFilter jarFileFilter = new FileFilter()
    {
        public boolean accept( File pathname )
        {
            return pathname.isFile() && pathname.getName().endsWith( ".jar" );
        }
    };
    
    private FileFilter pack200FileFilter = new Pack200FileFilter();

    // anonymous to inner to work-around qdox 1.6.1 bug (MPLUGIN-26)
    static class Pack200FileFilter implements FileFilter {
        public boolean accept( File pathname )
        {
            return pathname.isFile() &&
                ( pathname.getName().endsWith( ".jar.pack.gz" ) || pathname.getName().endsWith( ".jar.pack" ) );
        }
    };
    
    // the jars to sign and pack are selected if they are newer than the plugin start.
    // as the plugin copies the new versions locally before signing/packing them
    // we just need to see if the plugin copied a new version
    // We achieve that by only filtering files modified after the plugin was started
    // Note: if other files (the pom, the keystore config) have changed, one needs to clean
    private FileFilter updatedJarFileFilter = new CompositeFileFilter( jarFileFilter, modifiedFileFilter );

    private FileFilter updatedPack200FileFilter = new CompositeFileFilter( pack200FileFilter, modifiedFileFilter );

    /**
     * the artifacts packaged in the webstart app
     */
    private List packagedJnlpArtifacts = new ArrayList();

    private List modifiedJnlpArtifacts = new ArrayList();

    private Artifact artifactWithMainClass;

    // initialized by execute
    private long startTime;

    private long getStartTime()
    {
        if ( startTime == 0 )
        {
            throw new IllegalStateException( "startTime not initialized" );
        }
        return startTime;
    }

    public void execute()
        throws MojoExecutionException
    {

        checkInput();

        // interesting: copied files lastModified time stamp will be rounded.
        // We have to be sure that the startTime is before that time...
        // rounding to the second - 1 millis should be sufficient..
        startTime = System.currentTimeMillis() - 1000;

        File workDirectory = getWorkDirectory();
        getLog().debug( "using work directory " + workDirectory );
        //
        // prepare layout
        //
        if ( !workDirectory.exists() && !workDirectory.mkdirs() )
        {
            throw new MojoExecutionException( "Failed to create: " + workDirectory.getAbsolutePath() );
        }

        try
        {
            File resourcesDir = getJnlp().getResources();
            if ( resourcesDir == null )
            {
                resourcesDir = new File( getProject().getBasedir(), "src/main/jnlp/resources" );
            }
            copyResources( resourcesDir, workDirectory );

            artifactWithMainClass = null;

            processDependencies();

            if ( artifactWithMainClass == null )
            {
                throw new MojoExecutionException(
                    "didn't find artifact with main class: " + jnlp.getMainClass() + ". Did you specify it? " );
            }

            // native libs
            // FIXME

            /*
            for( Iterator it = getNativeLibs().iterator(); it.hasNext(); ) {
                Artifact artifact = ;
                Artifact copiedArtifact = 

                // similar to what we do for jars, except that we must pack them into jar instead of copying.
                // them
                    File nativeLib = artifact.getFile()
                    if(! nativeLib.endsWith( ".jar" ) ){
                        getLog().debug("Wrapping native library " + artifact + " into jar." );
                        File nativeLibJar = new File( applicationFolder, xxx + ".jar");
                        Jar jarTask = new Jar();
                        jarTask.setDestFile( nativeLib );
                        jarTask.setBasedir( basedir );
                        jarTask.setIncludes( nativeLib );
                        jarTask.execute();

                        nativeLibJar.setLastModified( nativeLib.lastModified() );
              
                        copiedArtifact = new ....
                    } else {
                        getLog().debug( "Copying native lib " + artifact );
                        copyFileToDirectory( artifact.getFile(), applicationFolder );
  
                        copiedArtifact = artifact;
                    }
                    copiedNativeArtifacts.add( copiedArtifact );
                }
            }
            */

            //
            // pack200 and jar signing
            //
            if ( ( pack200 || sign != null ) && getLog().isDebugEnabled() )
            {
                logCollection(
                    "Some dependencies may be skipped. Here's the list of the artifacts that should be signed/packed: ",
                    modifiedJnlpArtifacts );
            }

            if ( sign != null )
            {
                sign.init(getLog(), getWorkDirectory(), verbose);
                
                // not yet enabled
                // removeExistingSignatures(workDirectory, updatedJarFileFilter);

                if ( pack200 )
                {
                    // http://java.sun.com/j2se/1.5.0/docs/guide/deployment/deployment-guide/pack200.html
                    // we need to pack then unpack the files before signing them
                    Pack200.packJars( workDirectory, updatedJarFileFilter, this.gzip );
                    Pack200.unpackJars( workDirectory, updatedPack200FileFilter );
                    // specs says that one should do it twice when there are unsigned jars??
                    // Pack200.unpackJars( applicationDirectory, updatedPack200FileFilter );
                }

                int signedJars = signJars( workDirectory, updatedJarFileFilter );
                if ( signedJars != modifiedJnlpArtifacts.size() )
                {
                    throw new IllegalStateException(
                        "the number of signed artifacts differ from the number of modified artifacts. Implementation error" );
                }
            }
            if ( pack200 )
            {
                getLog().debug( "packing jars" );
                Pack200.packJars( workDirectory, updatedJarFileFilter, this.gzip );
            }

            generateJnlpFile( workDirectory );

            // package the zip. Note this is very simple. Look at the JarMojo which does more things.
            // we should perhaps package as a war when inside a project with war packaging ?
            File toFile = new File( getProject().getBuild().getDirectory(), getProject().getBuild().getFinalName() + ".zip" );
            if ( toFile.exists() )
            {
                getLog().debug( "deleting file " + toFile );
                toFile.delete();
            }
            zipArchiver.addDirectory( workDirectory );
            zipArchiver.setDestFile( toFile );
            getLog().debug( "about to call createArchive" );
            zipArchiver.createArchive();

            // maven 2 version 2.0.1 method
            projectHelper.attachArtifact( getProject(), "zip", toFile );

        }
        catch ( MojoExecutionException e )
        {
            throw e;
        }
        catch ( Exception e )
        {
            throw new MojoExecutionException( "Failure to run the plugin: ", e );
            /*
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter( sw, true );
            e.printStackTrace( pw );
            pw.flush();
            sw.flush();

            getLog().debug( "An error occurred during the task: " + sw.toString() );
            */
        }

    }

    private void copyResources( File resourcesDir, File workDirectory )
        throws IOException
    {
        if ( ! resourcesDir.exists() )
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
                // FileUtils.copyDirectoryStructure( resourcesDir , workDirectory );

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
        if ( ! sourceDirectory.exists() )
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

    void checkDependencies()
        throws MojoExecutionException
    {
        if ( dependencies == null )
            return;

        boolean failed = false;

        Collection artifacts = getProject().getArtifacts();

        getLog().debug( "artifacts: " + artifacts.size() );

        if ( dependencies.getIncludes() != null && !dependencies.getIncludes().isEmpty() )
        {
            failed = checkDependencies( dependencies.getIncludes(), artifacts ) || failed;
        }
        if ( dependencies.getExcludes() != null && !dependencies.getExcludes().isEmpty() )
        {
            failed = checkDependencies( dependencies.getExcludes(), artifacts ) || failed;
        }

        if ( failed )
        {
            throw new MojoExecutionException( "At least one specified dependency is incorrect. Review your project configuration." );
        }
    }

    private boolean checkDependencies( List patterns, Collection artifacts )
    {
        if ( dependencies == null )
            return false;

        boolean failed = false;
        for ( Iterator it = patterns.iterator(); it.hasNext(); )
        {
            failed = ensurePatternMatchesAtLeastOneArtifact( it.next().toString(), artifacts ) || failed;
        }
        return failed;
    }

    /** @return true if filter matches no artifact, false otherwise **/
    private boolean ensurePatternMatchesAtLeastOneArtifact( String pattern, Collection artifacts )
    {
        List onePatternList = new ArrayList();
        onePatternList.add( pattern );
        ArtifactFilter filter = new IncludesArtifactFilter( onePatternList );

        boolean noMatch = true;
        for ( Iterator it = artifacts.iterator(); it.hasNext(); )
        {
            Artifact artifact = (Artifact) it.next();

            getLog().debug( "checking pattern: " + pattern + " against " + artifact );

            if ( filter.include( artifact ) )
            {
                noMatch = false;
                break;
            }
        }
        if ( noMatch )
        {
            getLog().error( "pattern: " + pattern + " doesn't match any artifact." );
        }
        return noMatch;
    }

    /**
     * Iterate through all the top level and transitive dependencies declared in the project and
     * collect all the runtime scope dependencies for inclusion in the .zip and signing.
     *
     * @throws IOException
     */
    private void processDependencies()
        throws IOException
    {

        processDependency( getProject().getArtifact() );

        AndArtifactFilter filter = new AndArtifactFilter();
        // filter.add( new ScopeArtifactFilter( dependencySet.getScope() ) );

        if ( dependencies != null && dependencies.getIncludes() != null && !dependencies.getIncludes().isEmpty() )
        {
            filter.add( new IncludesArtifactFilter( dependencies.getIncludes() ) );
        }
        if ( dependencies != null && dependencies.getExcludes() != null && !dependencies.getExcludes().isEmpty() )
        {
            filter.add( new ExcludesArtifactFilter( dependencies.getExcludes() ) );
        }

        Collection artifacts = getProject().getArtifacts();

        for ( Iterator it = artifacts.iterator(); it.hasNext(); )
        {
            Artifact artifact = (Artifact) it.next();
            if ( filter.include( artifact ) )
            {
                processDependency( artifact );
            }
        }
    }

    private void processDependency( Artifact artifact )
        throws IOException
    {
        // TODO: scope handler
        // Include runtime and compile time libraries
        if ( !Artifact.SCOPE_PROVIDED.equals( artifact.getScope() ) &&
            !Artifact.SCOPE_TEST.equals( artifact.getScope() ) )
        {
            String type = artifact.getType();
            if ( "jar".equals( type ) )
            {

                // FIXME when signed, we should update the manifest.
                // see http://www.mail-archive.com/turbine-maven-dev@jakarta.apache.org/msg08081.html
                // and maven1: maven-plugins/jnlp/src/main/org/apache/maven/jnlp/UpdateManifest.java
                // or shouldn't we?  See MOJO-7 comment end of October.
                final File toCopy = artifact.getFile();

                if ( toCopy == null )
                {
                    getLog().error( "artifact with no file: " + artifact );
                    getLog().error( "artifact download url: " + artifact.getDownloadUrl() );
                    getLog().error( "artifact repository: " + artifact.getRepository() );
                    getLog().error( "artifact repository: " + artifact.getVersion() );
                    throw new IllegalStateException(
                        "artifact " + artifact + " has no matching file, why? Check the logs..." );
                }

                boolean copied = copyFileToDirectoryIfNecessary( toCopy, getWorkDirectory() );

                if ( copied )
                {

                    String name = toCopy.getName();
                    this.modifiedJnlpArtifacts.add( name.substring( 0, name.lastIndexOf( '.' ) ) );

                }

                packagedJnlpArtifacts.add( artifact );

                if ( artifactContainsClass( artifact, jnlp.getMainClass() ) )
                {
                    if ( artifactWithMainClass == null )
                    {
                        artifactWithMainClass = artifact;
                        getLog().debug( "Found main jar. Artifact " + artifactWithMainClass +
                            " contains the main class: " + jnlp.getMainClass() );
                    }
                    else
                    {
                        getLog().warn( "artifact " + artifact + " also contains the main class: " +
                            jnlp.getMainClass() + ". IGNORED." );
                    }
                }
            }
            else
            // FIXME how do we deal with native libs?
            // we should probably identify them and package inside jars that we timestamp like the native lib
            // to avoid repackaging every time. What are the types of the native libs?
            {
                verboseLog( "Skipping artifact of type " + type + " for " + getWorkDirectory().getName() );
            }
            // END COPY
        }
        else
        {
            verboseLog( "Skipping artifact of scope " + artifact.getScope() + " for " + getWorkDirectory().getName() );
        }
    }

    private boolean artifactContainsClass( Artifact artifact, final String mainClass )
        throws MalformedURLException
    {
        boolean containsClass = true;

        // JarArchiver.grabFilesAndDirs()
        ClassLoader cl = new java.net.URLClassLoader( new URL[]{artifact.getFile().toURL()} );
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
                c.getMethod( "main", new Class[]{String[].class} );
            }
            catch ( NoSuchMethodException e )
            {
                getLog().warn( "The specified main class (" + mainClass +
                    ") doesn't seem to contain a main method... Please check your configuration." + e.getMessage() );
            }
            catch ( NoClassDefFoundError e )
            {
                // undocumented in SDK 5.0. is this due to the ClassLoader lazy loading the Method thus making this a case tackled by the JVM Spec (Ref 5.3.5)!
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

    void generateJnlpFile( File outputDirectory )
        throws MojoExecutionException
    {
        if ( jnlp.getOutputFile() == null || jnlp.getOutputFile().length() == 0 )
        {
            getLog().debug( "Jnlp output file name not specified. Using default output file name: launch.jnlp." );
            jnlp.setOutputFile( "launch.jnlp" );
        }
        File jnlpOutputFile = new File( outputDirectory, jnlp.getOutputFile() );

        if ( jnlp.getInputTemplate() == null || jnlp.getInputTemplate().length() == 0 )
        {
            getLog().debug(
                "Jnlp template file name not specified. Using default output file name: src/main/jnlp/template.vm." );
            jnlp.setInputTemplate( "src/main/jnlp/template.vm" );
        }
        String templateFileName = jnlp.getInputTemplate();

        File resourceLoaderPath = getProject().getBasedir();

        if ( jnlp.getInputTemplateResourcePath() != null && jnlp.getInputTemplateResourcePath().length() > 0 )
        {
            resourceLoaderPath = new File( jnlp.getInputTemplateResourcePath() );
        }

        Generator jnlpGenerator = new Generator( this, resourceLoaderPath, jnlpOutputFile, templateFileName );
        try
        {
            jnlpGenerator.generate();
        }
        catch ( Exception e )
        {
            getLog().debug( e .toString() );
            throw new MojoExecutionException( "Could not generate the JNLP deployment descriptor", e );
        }
    }

    private void logCollection( final String prefix, final Collection collection )
    {
        getLog().debug( prefix + " " + collection );
        if ( collection == null )
        {
            return;
        }
        for ( Iterator it3 = collection.iterator(); it3.hasNext(); )
        {
            getLog().debug( prefix + it3.next() );
        }
    }

    private File getWorkDirectory()
    {
        return workDirectory;
    }

    /**
     * Conditionaly copy the file into the target directory.
     * The operation is not performed when the target file exists is up to date.
     * The target file name is taken from the <code>sourceFile</code> name.
     *
     * @return <code>true</code> when the file was copied, <code>false</code> otherwise.
     * @throws NullPointerException is sourceFile is <code>null</code> or
     *                              <code>sourceFile.getName()</code> is <code>null</code>
     * @throws IOException          if the copy operation is tempted but failed.
     */
    private boolean copyFileToDirectoryIfNecessary( File sourceFile, File targetDirectory )
        throws IOException
    {

        if ( sourceFile == null )
        {
            throw new NullPointerException( "sourceFile is null" );
        }

        File targetFile = new File( targetDirectory, sourceFile.getName() );

        boolean shouldCopy = ! targetFile.exists() || targetFile.lastModified() < sourceFile.lastModified();

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
     * return the number of signed jars *
     */
    private int signJars( File directory, FileFilter fileFilter )
        throws MojoExecutionException, MojoFailureException
    {

        File[] jarFiles = directory.listFiles( fileFilter );

        getLog().debug( "signJars in " + directory + " found " + jarFiles.length + " jar(s) to sign" );

        if ( jarFiles.length == 0 )
        {
            return 0;
        }

        JarSignerMojo signJar = sign.getJarSignerMojo();

        for ( int i = 0; i < jarFiles.length; i++ )
        {
            signJar.setJarPath( jarFiles[i] );
            // for some reason, it appears that the signed jar field is not null ?
            signJar.setSignedJar( null );
            long lastModified = jarFiles[i].lastModified();
            signJar.execute();
            setLastModified( jarFiles[i], lastModified );
        }

        return jarFiles.length;
    }

    private void checkInput()
        throws MojoExecutionException
    {

        getLog().debug( "a fact " + this.artifactFactory );
        getLog().debug( "a resol " + this.artifactResolver );
        getLog().debug( "basedir " + this.basedir );
        getLog().debug( "gzip " + this.gzip );
        getLog().debug( "pack200 " + this.pack200 );
        getLog().debug( "project " + this.getProject() );
        getLog().debug( "zipArchiver " + this.zipArchiver );
        // getLog().debug( "usejnlpservlet " + this.usejnlpservlet );
        getLog().debug( "verifyjar " + this.verifyjar );
        getLog().debug( "verbose " + this.verbose );

        if ( jnlp == null )
        {
            throw new MojoExecutionException( "<jnlp> configuration element missing." );
        }

        if ( SystemUtils.JAVA_VERSION_FLOAT < 1.5f )
        {
            if ( pack200 )
            {
                throw new MojoExecutionException( "SDK 5.0 minimum when using pack200." );
            }
        }

        checkDependencies();

        // FIXME
        /*
        if ( !"pom".equals( getProject().getPackaging() ) ) {
            throw new MojoExecutionException( "'" + getProject().getPackaging() + "' packaging unsupported. Use 'pom'" );
        }
        */
    }

    /** this to try to workaround an issue with setting setLastModifier. See MWEBSTART-28. May be removed later on if that doesn't help. */
    private boolean setLastModified( File file, long timestamp )
    {
        boolean result;
        int nbretries = 3;
        int sleep = 4000;
        while ( ! (result = file.setLastModified( timestamp )) && nbretries-- > 0 )
        {
            getLog().warn("failure to change last modified timestamp... retrying ...");
            try { Thread.currentThread().sleep( sleep ); } catch (InterruptedException ignore) {}
        }
        return result;
    }

    void setWorkDirectory( File workDirectory )
    {
        this.workDirectory = workDirectory;
    }

    void setVerbose( boolean verbose )
    {
        this.verbose = verbose;
    }

    public JnlpConfig getJnlp()
    {
        return jnlp;
    }

    public List getPackagedJnlpArtifacts()
    {
        return packagedJnlpArtifacts;
    }

    Dependencies getDependencies()
    {
        return this.dependencies;
    }

    /*
    public Artifact getArtifactWithMainClass() {
        return artifactWithMainClass;
    }
    */

    public boolean isArtifactWithMainClass( Artifact artifact )
    {
        final boolean b = artifactWithMainClass.equals( artifact );
        getLog().debug( "compare " + artifactWithMainClass + " with " + artifact + ": " + b );
        return b;
    }

    public String getSpec()
    {
        // shouldn't we automatically identify the spec based on the features used in the spec?
        // also detect conflicts. If user specified 1.0 but uses a 1.5 feature we should fail in checkInput().
        if ( jnlp.getSpec() != null )
        {
            return jnlp.getSpec();
        }
        return "1.0+";
    }
    
    private int removeExistingSignatures(File workDirectory, FileFilter updatedJarFileFilter) 
        throws MojoExecutionException 
    {   
        // cleanup tempDir if exists
        File tempDir = new File( this.workDirectory, "temp_extracted_jars" );
        removeDirectory(tempDir);
        
        // recreate temp dir
        if ( !tempDir.mkdirs() ) {
            throw new MojoExecutionException( "Error creating temporary directory: " + tempDir );
        }        
        
        // process jars
        File[] jarFiles = workDirectory.listFiles( updatedJarFileFilter );

        JarUnsignMojo unsignJar = new JarUnsignMojo();
//        unsignJar.setBasedir( basedir );
        unsignJar.setTempDir( tempDir );
        unsignJar.setVerbose( this.verbose );
//        unsignJar.setWorkingDir( getWorkDirectory() );

        unsignJar.setArchiverManager( archiverManager );

        for ( int i = 0; i < jarFiles.length; i++ )
        {
            unsignJar.setJarPath( jarFiles[i] );
            // long lastModified = jarFiles[i].lastModified();
            unsignJar.execute();
            // jarFiles[i].setLastModified( lastModified );
        }

        // cleanup tempDir
        removeDirectory(tempDir);

        return jarFiles.length;        
    }

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

    // helper methods

    /**
     * Log as info when verbose or info is enabled, as debug otherwise.
     */
    private void verboseLog( String msg )
    {
        infoOrDebug( this.verbose || getLog().isInfoEnabled(), msg );
    }

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

