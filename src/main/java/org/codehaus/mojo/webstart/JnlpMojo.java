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

import com.sun.tools.apache.ant.pack200.Pack200Task;
import com.sun.tools.apache.ant.pack200.Unpack200Task;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.ArtifactUtils;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.artifact.versioning.InvalidVersionSpecificationException;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.InvalidPluginException;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.PluginManager;
import org.apache.maven.plugin.PluginManagerException;
import org.apache.maven.plugin.PluginNotFoundException;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.plugin.jar.JarSignMojo;
import org.apache.maven.plugin.version.PluginVersionNotFoundException;
import org.apache.maven.plugin.version.PluginVersionResolutionException;
import org.apache.maven.project.DefaultMavenProjectHelper;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.apache.maven.settings.Settings;
import org.apache.tools.ant.Project;
import org.codehaus.mojo.keytool.GenkeyMojo;
import org.codehaus.mojo.webstart.generator.Generator;
import org.codehaus.plexus.archiver.zip.ZipArchiver;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.io.FileFilter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Packages a jnlp application.
 *
 * @author <a href="jerome@coffeebreaks.org">Jerome Lacoste</a>
 * @version $Id: $
 * @goal jnlp
 * @phase package
 * @requiresDependencyResolution runtime
 * @requiresProject
 * @inheritedByDefault true
 * @todo refactor the common code with javadoc plugin
 * @todo how to propagate the -X argument to enable verbose?
 * @todo initialize the jnlp alias and dname.o from pom.artifactId and pom.organization.name
 */
public class JnlpMojo
    extends AbstractMojo
{
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
     * Project.
     *
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    /**
     * JNLP dependencies.
     *
     * @parameter
     */
    private List dependencies;

    /**
     * Xxx
     *
     * @parameter
     */
    private JnlpConfig jnlp;

    /**
     * Xxx
     *
     * @parameter
     */
    private SignConfig sign;

    public static class KeystoreConfig
    {
        private boolean delete;

        private boolean gen;

        public boolean isDelete()
        {
            return delete;
        }

        public void setDelete( boolean delete )
        {
            this.delete = delete;
        }

        public boolean isGen()
        {
            return gen;
        }

        public void setGen( boolean gen )
        {
            this.gen = gen;
        }
    }

    /**
     * Xxx
     *
     * @parameter
     */
    private KeystoreConfig keystore;

    /**
     * Xxx
     *
     * @parameter default-value="false"
     */
    private boolean usejnlpservlet;

    /**
     * Xxx
     *
     * @parameter default-value="true"
     */
    private boolean verifyjar;

    /**
     * Xxx
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

    private FileFilter jarFileFilter = new FileFilter()
    {
        public boolean accept( File pathname )
        {
            return pathname.isFile() && pathname.getName().endsWith( ".jar" );
        }
    };

    private FileFilter pack200FileFilter = new FileFilter()
    {
        public boolean accept( File pathname )
        {
            return pathname.isFile() &&
                ( pathname.getName().endsWith( ".jar.pack.gz" ) || pathname.getName().endsWith( ".jar.pack" ) );
        }
    };

    private List copiedArtifacts;

    private Artifact artifactWithMainClass;

    public void execute()
        throws MojoExecutionException
    {

        checkInput();

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
            File applicationDirectory = workDirectory;
            File iconFolder = new File( applicationDirectory, "images" );
            iconFolder.mkdirs();

            //
            //copy icons, jars etc.. to the relevant folders
            //
            for ( int i = 0; i < jnlp.getInformations().length; i++ )
            {
                JnlpConfig.Information information = jnlp.getInformations()[i];
                if ( information.getIcons() != null )
                {
                    for ( int j = 0; j < information.getIcons().length; j++ )
                    {
                        //icons
                        JnlpConfig.Icon icon = information.getIcons()[j];
                        File iconFile = getIconFile( icon );
                        icon.setFileName( iconFile.getName() );
                        FileUtils.copyFileToDirectory( iconFile, iconFolder );
                    }
                }
            }

            /*
            // jnlp servlet -> WEB-INF/lib folder
            if ( this.usejnlpservlet ) {
                // we need to retrieve the version of the jnlpServlet.
                String jnlpServletGroupId = "com.sun.java.jnlp";
                String jnlpServletArtifactId = "jnlp-servlet";
                String jnlpServletVersion = findThisPluginDependencyVersion( jnlpServletGroupId, jnlpServletArtifactId );

                // getLog().debug( "****************************************************************************" );
                Artifact jnlpServletArtifact = resolveJarArtifact( jnlpServletGroupId, jnlpServletArtifactId, jnlpServletVersion );
                getLog().debug( "jnlpServletArtifact : " + jnlpServletArtifact.getFile() );

                copyFileToDirectory( jnlpServletArtifact.getFile(), webinflibFolder );
            }
            */

            artifactWithMainClass = null;

            copiedArtifacts = new ArrayList();
            Collection artifacts = getProject().getArtifacts();
            for ( Iterator it = dependencies.iterator(); it.hasNext(); )
            {
                String dependency = (String) it.next();
                getLog().debug( "handling dependency " + dependency );

                boolean found = false;
                // identify artifact
                //jars -> application folder
                // similar to what war plugin does
                // FIXME we must make our list based on the specified dependencies
                for ( Iterator it2 = artifacts.iterator(); it2.hasNext() && !found; )
                {
                    Artifact artifact = (Artifact) it2.next();

                    // should we use depedencyset and filters like in assembly plugin?
                    if ( !matches( artifact, dependency ) )
                    {
                        continue;
                    }
                    found = true;

                    // copied from war plugin... then modified
                    // BEGIN COPY
                    // TODO: utilise appropriate methods from project builder
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
                            FileUtils.copyFileToDirectory( artifact.getFile(), applicationDirectory );
                            copiedArtifacts.add( artifact );

                            // JarArchiver.grabFilesAndDirs()
                            ClassLoader cl = new java.net.URLClassLoader( new URL[]{artifact.getFile().toURL()} );
                            try
                            {
                                Class.forName( jnlp.getMainClass(), false, cl );
                                if ( artifactWithMainClass == null )
                                {
                                    artifactWithMainClass = artifact;
                                    getLog().debug(
                                        "Found main jar. Artifact " + artifactWithMainClass +
                                        " contains the main class: " +
                                        jnlp.getMainClass() );
                                }
                                else
                                {
                                    getLog().warn(
                                        "artifact " + artifact + " also contains the main class: " +
                                        jnlp.getMainClass() +
                                        ". IGNORED." );
                                }
                            }
                            catch ( ClassNotFoundException e )
                            {
                                getLog().debug(
                                    "artifact " + artifact + " doesn't contain the main class: " + jnlp.getMainClass() );
                            }
                        }
                        else
                        // FIXME how do we deal with native libs?
                        // we should probably identify them and package inside jars that we timestamp like the native lib
                        // to avoid repackaging every time. What are the types of the native libs?
                        {
                            getLog().debug(
                                "Skipping artifact of type " + type + " for " + applicationDirectory.getName() );
                        }
                        // END COPY
                    }
                }
                if ( !found )
                {
                    throw new MojoExecutionException( "didn't find dependency " + dependency + " in dependency list." );
                }
            }
            if ( artifactWithMainClass == null )
            {
                throw new MojoExecutionException( "didn't find artifact with main class: " + jnlp.getMainClass() + ". Did you specify it? " );
            }

            // native libsi
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
            if ( sign != null )
            {

                if ( keystore != null && keystore.isGen() )
                {
                    if ( keystore.isDelete() )
                    {
                        deleteKeyStore();
                    }
                    genKeyStore();
                }

                if ( pack200 )
                {
                    // http://java.sun.com/j2se/1.5.0/docs/guide/deployment/deployment-guide/pack200.html
                    // we need to pack then unpack the files before signing them
                    packJars( applicationDirectory );
                    unpackJars( applicationDirectory );
                    // specs says that one should do it twice when there are unsigned jars??
                    // unpackJars(applicationDirectory);
                }
                signJars( applicationDirectory );
            }
            if ( pack200 )
            {
                getLog().debug( "packing jars" );
                packJars( applicationDirectory );
            }

            //
            // template generation
            //
            // generate the JNLP deployment file
            File jnlpOutputFile = new File( applicationDirectory, "launch.jnlp" );
            Generator jnlpGenerator = new Generator( this, jnlpOutputFile,
                                                     "org/codehaus/mojo/webstart/template/jnlp.vm" );
            try
            {
                jnlpGenerator.generate();
            }
            catch ( Exception e )
            {
                getLog().debug( e.toString() );
                throw new MojoExecutionException( "Could not generate the JNLP deployment descriptor", e );
            }

            // package the zip. Note this is very simple. Look at the JarMojo which does more things.
            // we should perhaps package as a war when inside a project with war packaging ?
            File toFile = new File( project.getBuild().getDirectory(), project.getBuild().getFinalName() + ".zip" );
            if ( toFile.exists() )
            {
                getLog().debug( "deleting file " + toFile );
                toFile.delete();
            }
            zipArchiver.addDirectory( workDirectory );
            zipArchiver.setDestFile( toFile );
            getLog().debug( "about to call createArchive" );
            zipArchiver.createArchive();

            // project.attachArtifact( "pom", null, toFile );

            getLog().debug( "**** attach new way **** " + projectHelper.getClass().getName() );
            // depends on MNG-1251
            projectHelper.attachArtifact( project, "zip", toFile );

        }
        catch ( Exception e )
        {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter( sw, true );
            e.printStackTrace( pw );
            pw.flush();
            sw.flush();

            getLog().debug( "An error occurred during the task: " + sw.toString() );
        }
    }

    /**
     * Return the version of the specified plugin dependency.
     * <p/>
     * The plugin version is found at runtime, its description resolved and its artifacts are searched to find the
     * specified dependency.
     *
     * @param pluginDependencyGroupId
     * @param pluginDependencyArtifactId
     * @return
     * @throws ArtifactResolutionException
     * @throws PluginVersionResolutionException
     *
     * @throws ArtifactNotFoundException
     * @throws InvalidVersionSpecificationException
     *
     * @throws InvalidPluginException
     * @throws PluginManagerException
     * @throws PluginNotFoundException
     * @throws PluginVersionNotFoundException
     * @throws MojoExecutionException
     */
    // I believe this hack could be useful. Let me store it somewhere.
    /*
    private String findThisPluginDependencyVersion( String pluginDependencyGroupId, String pluginDependencyArtifactId )
        throws ArtifactResolutionException, PluginVersionResolutionException, ArtifactNotFoundException,
        InvalidVersionSpecificationException, InvalidPluginException, PluginManagerException, PluginNotFoundException,
        PluginVersionNotFoundException, MojoExecutionException
    {
        // see DescribeMojo for a way to reduce the number of exceptiosn...

        Plugin thisPlugin = findThisPlugin();
        return findPluginDependencyVersion( thisPlugin, pluginDependencyGroupId, pluginDependencyArtifactId );
    }
    */
    /*
    private Plugin findThisPlugin()
        throws MojoExecutionException
    {
        // first we identify this plugin. Is this correct?
        final Artifact pluginArtifact = (Artifact) getProject().getPluginArtifacts().iterator().next();
        String thisPluginGroupId = pluginArtifact.getGroupId();
        String thisPluginArtifactId = pluginArtifact.getArtifactId();

        for ( Iterator it2 = getProject().getBuildPlugins().iterator(); it2.hasNext(); )
        {
            final org.apache.maven.model.Plugin plugin = ( (org.apache.maven.model.Plugin) it2.next() );
            // getLog().debug( "project build plugin: " + plugin.getGroupId()
            //        + ":" + plugin.getArtifactId() + ":" + plugin.getVersion());

            if ( plugin.getGroupId().equals( thisPluginGroupId ) &&
                plugin.getArtifactId().equals( thisPluginArtifactId ) )
            {
                getLog().debug( "found thisPluginVersion : " + plugin.getVersion() );
                return plugin;
            }
        }

        throw new MojoExecutionException( "couldn't identify this plugin version for " + thisPluginGroupId + ":" + thisPluginArtifactId );
    }

    private String findPluginDependencyVersion( final Plugin plugin, String pluginDependencyGroupId,
                                                String pluginDependencyArtifactId )
        throws ArtifactResolutionException, PluginVersionResolutionException, ArtifactNotFoundException,
        InvalidVersionSpecificationException, InvalidPluginException, PluginManagerException, PluginNotFoundException,
        PluginVersionNotFoundException, MojoExecutionException
    {

        // logCollection( "plugin dependencies: ", plugin.getDependencies() );

        // now that we've found the plugin
        PluginDescriptor descriptor = pluginManager.verifyPlugin( plugin, getProject(), settings, localRepository );

        final Iterator iterator = descriptor.getArtifacts().iterator();
        String pluginDependencyVersion;
        while ( iterator.hasNext() )
        {
            Artifact artifact = (Artifact) iterator.next();
            if ( pluginDependencyGroupId.equals( artifact.getGroupId() ) &&
                pluginDependencyArtifactId.equals( artifact.getArtifactId() ) )
            {
                pluginDependencyVersion = artifact.getVersion();
                getLog().debug( "found jnlpServlet version : " + pluginDependencyVersion );
                return pluginDependencyVersion;
            }
        }
        throw new MojoExecutionException( "couldn't identify the jnlpServlet dependency version for plugin " +
                                          plugin.getGroupId() + ":" + plugin.getArtifactId() + ":" + plugin.getVersion() );
    }
    */

    private Artifact resolveJarArtifact( String groupId, String artifactId, String version )
        throws MojoExecutionException
    {
        return resolveArtifact( groupId, artifactId, version, "jar" );
    }

    private Artifact resolveArtifact( String groupId, String artifactId, String version, final String type )
        throws MojoExecutionException
    {
        Artifact artifact = artifactFactory.createArtifact( groupId, artifactId, version, null, type );

        try
        {
            artifactResolver.resolve( artifact, getProject().getRemoteArtifactRepositories(), localRepository );
        }
        catch ( ArtifactNotFoundException e )
        {
            // ignore, the jar has not been found
            if ( getLog().isDebugEnabled() )
            {
                getLog().debug( "Cannot resolve source artifact", e );
            }
        }
        catch ( ArtifactResolutionException e )
        {
            throw new MojoExecutionException( "Error getting source artifact", e );
        }

        return artifact;
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

    private void deleteKeyStore()
    {
        File keyStore = null;
        if ( sign.getKeystore() != null )
        {
            keyStore = new File( sign.getKeystore() );
        }
        else
        {
            // FIXME decide if we really want this.
            // keyStore = new File( System.getProperty( "user.home") + File.separator + ".keystore" );
        }

        if ( keyStore == null )
        {
            return;
        }
        if ( keyStore.exists() )
        {
            if ( keyStore.delete() )
            {
                getLog().debug( "deleted keystore from: " + keyStore.getAbsolutePath() );
            }
            else
            {
                getLog().warn( "Couldn't delete keystore from: " + keyStore.getAbsolutePath() );
            }
        }
        else
        {
            getLog().debug( "Skipping deletion of non existing keystore: " + keyStore.getAbsolutePath() );
        }
    }

    private void genKeyStore()
        throws MojoExecutionException
    {
        GenkeyMojo genKeystore = new GenkeyMojo();
        genKeystore.setAlias( sign.getAlias() );
        genKeystore.setDname( sign.getDname() );
        genKeystore.setKeyalg( sign.getKeyalg() );
        genKeystore.setKeypass( sign.getKeypass() );
        genKeystore.setKeysize( sign.getKeysize() );
        genKeystore.setKeystore( sign.getKeystore() );
        genKeystore.setSigalg( sign.getSigalg() );
        genKeystore.setStorepass( sign.getStorepass() );
        genKeystore.setStoretype( sign.getStoretype() );
        genKeystore.setValidity( sign.getValidity() );
        genKeystore.setVerbose( this.verbose );
        genKeystore.setWorkingDir( getWorkDirectory() );

        genKeystore.execute();
    }

    private File getWorkDirectory()
    {
        return workDirectory;
    }

    private boolean matches( Artifact artifact, String dependency )
    {
        final String stringRepresentation = ArtifactUtils.versionlessKey( artifact );
        final boolean b = dependency.equals( stringRepresentation );
        getLog().debug( "checking match of <" + dependency + "> with <" + stringRepresentation + ">: " + b );
        return b;
    }

    private File getIconFile( JnlpConfig.Icon icon )
        throws MojoExecutionException
    {
        // FIXME we could have a different priority search order. IN particular the src/jnlp/icons could be first.
        File iconFile = new File( icon.getHref() );
        if ( !iconFile.exists() )
        {
            getLog().debug( "icon " + icon.getHref() + " not found at " + iconFile.getAbsolutePath() );
            iconFile = new File( basedir, icon.getHref() );
            if ( !iconFile.exists() )
            {
                getLog().debug( "icon " + icon.getHref() + " not found at " + iconFile.getAbsolutePath() );
                iconFile = new File( basedir + "/src/jnlp/icons", icon.getHref() );
                if ( !iconFile.exists() )
                {
                    getLog().debug( "icon " + icon.getHref() + " not found at " + iconFile.getAbsolutePath() );
                    throw new MojoExecutionException( "icon: " + icon.getHref() + " doesn't exist." );
                }
            }
        }
        getLog().debug( "icon " + icon.getHref() + " found at " + iconFile.getAbsolutePath() );
        return iconFile;
    }

    private void packJars( File directory )
    {
        getLog().debug( "packJars for " + directory );
        Pack200Task packTask;
        File[] jarFiles = directory.listFiles( jarFileFilter );
        for ( int i = 0; i < jarFiles.length; i++ )
        {
            getLog().debug( "packJars: " + jarFiles[i] );

            final String extension = this.gzip ? ".pack.gz" : ".pack";

            File pack200Jar = new File( jarFiles[i].getParentFile(), jarFiles[i].getName() + extension );

            if ( pack200Jar.exists() )
            {
                pack200Jar.delete();
            }

            packTask = new Pack200Task();
            packTask.setProject( new Project() );
            packTask.setDestfile( pack200Jar );
            packTask.setSrc( jarFiles[i] );
            packTask.setGZIPOutput( this.gzip );
            packTask.execute();
            pack200Jar.setLastModified( jarFiles[i].lastModified() );
        }
    }

    private void unpackJars( File directory )
    {
        getLog().debug( "unpackJars for " + directory );
        Unpack200Task unpackTask;
        File[] packFiles = directory.listFiles( pack200FileFilter );
        for ( int i = 0; i < packFiles.length; i++ )
        {
            final String packedJarPath = packFiles[i].getAbsolutePath();
            int extensionLength = packedJarPath.endsWith( ".jar.pack.gz" ) ? 8 : 5;
            String jarFileName = packedJarPath.substring( 0, packedJarPath.length() - extensionLength );
            File jarFile = new File( jarFileName );

            if ( jarFile.exists() )
            {
                jarFile.delete();
            }
            unpackTask = new Unpack200Task();
            unpackTask.setProject( new Project() );
            unpackTask.setDest( jarFile );
            unpackTask.setSrc( packFiles[i] );
            unpackTask.execute();
            jarFile.setLastModified( packFiles[i].lastModified() );
        }
    }

    private void signJars( File directory )
        throws MojoExecutionException
    {

        getLog().debug( "signJars in " + directory );

        File[] jarFiles = directory.listFiles( jarFileFilter );

        JarSignMojo signJar = new JarSignMojo();
        signJar.setAlias( sign.getAlias() );
        signJar.setBasedir( basedir );
        signJar.setKeypass( sign.getKeypass() );
        signJar.setKeystore( sign.getKeystore() );
        signJar.setSigFile( sign.getSigfile() );
        signJar.setStorepass( sign.getStorepass() );
        signJar.setType( sign.getStoretype() );
        signJar.setVerbose( this.verbose );
        signJar.setWorkingDir( getWorkDirectory() );
        signJar.setVerify( sign.getVerify() );

        for ( int i = 0; i < jarFiles.length; i++ )
        {
            signJar.setJarPath( jarFiles[i] );
            // we don't change the jar name
            // signJar.setSignedJar( xx );
            long lastModified = jarFiles[i].lastModified();
            signJar.execute();
            jarFiles[i].setLastModified( lastModified );
        }
    }

    private void checkInput()
        throws MojoExecutionException
    {

        getLog().debug( "a fact " + this.artifactFactory );
        getLog().debug( "a resol " + this.artifactResolver );
        getLog().debug( "basedir " + this.basedir );
        getLog().debug( "depend " + this.dependencies );
        getLog().debug( "gzip " + this.gzip );
        getLog().debug( "pack200 " + this.pack200 );
        getLog().debug( "project " + this.getProject() );
        getLog().debug( "zipArchiver " + this.zipArchiver );
        getLog().debug( "usejnlpservlet " + this.usejnlpservlet );
        getLog().debug( "verifyjar " + this.verifyjar );
        getLog().debug( "verbose " + this.verbose );

        // FIXME
        /*
        if ( !"pom".equals( getProject().getPackaging() ) ) {
            throw new MojoExecutionException( "'" + getProject().getPackaging() + "' packaging unsupported. Use 'pom'" );
        }
        */
    }

    /**
     * @return
     */
    public MavenProject getProject()
    {
        return project;
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

    public List getCopiedArtifacts()
    {
        return copiedArtifacts;
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
}
