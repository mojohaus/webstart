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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.filter.AndArtifactFilter;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.artifact.resolver.filter.ExcludesArtifactFilter;
import org.apache.maven.artifact.resolver.filter.IncludesArtifactFilter;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.PluginManager;
import org.apache.maven.project.MavenProjectHelper;
import org.apache.maven.settings.Settings;
import org.codehaus.mojo.webstart.generator.Generator;
import org.codehaus.mojo.webstart.generator.GeneratorExtraConfig;
import org.codehaus.plexus.archiver.zip.ZipArchiver;

/**
 * @author <a href="jerome@coffeebreaks.org">Jerome Lacoste</a>
 * @version $Id$
 *
 * @todo refactor the common code with javadoc plugin
 * @todo how to propagate the -X argument to enable verbose?
 * @todo initialize the jnlp alias and dname.o from pom.artifactId and pom.organization.name
 */
public abstract class AbstractJnlpMojo
    extends AbstractBaseJnlpMojo
{

    private static final String DEFAULT_TEMPLATE_LOCATION = "src/main/jnlp/template.vm";

    /**
     * The Zip archiver.
     *
     * @component role="org.codehaus.plexus.archiver.Archiver" roleHint="zip"
     * @required
     */
    private ZipArchiver zipArchiver;

    /**
     * The jnlp configuration element.
     *
     * @parameter
     */
    private JnlpConfig jnlp;

    /**
     * [optional] transitive dependencies filter - if omitted, the plugin will include all transitive dependencies. 
     * Provided and test scope dependencies are always excluded.
     *
     * @parameter
     */
    private Dependencies dependencies;

    /**
     * Represents the configuration element that specifies which of the current 
     * project's dependencies will be included or excluded from the resources element
     * in the generated JNLP file.
     */
    public static class Dependencies
    {
        
        private boolean outputJarVersions;
        
        private List includes;

        private List excludes;
        
        /**
         * Returns the value of the flag that determines whether or not 
         * the version attribute will be output in each jar resource element
         * in the generated JNLP file.
         *
         * @return The default output version flag.
         */
        public boolean getOutputJarVersions()
        {
            return outputJarVersions;
        }

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
     * @parameter default-value="${basedir}"
     * @required
     * @readonly
     */
    private File basedir;

    /**
     * The project helper used to attach the artifact produced by this plugin to the project.
     * @component
     */
    private MavenProjectHelper projectHelper;

    /**
     * The current user system settings for use in Maven. This is used for
     * <br/>
     * plugin manager API calls.
     *
     * @parameter default-value="${settings}"
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

    /**
     * the artifacts packaged in the webstart app
     */
    private List packagedJnlpArtifacts = new ArrayList();

    private Artifact artifactWithMainClass;

    /**
     * When set to true, this flag indicates that a version attribute should
     * be output in each of the jar resource elements in the generated 
     * JNLP file. 
     *  
     * @parameter default-value="false"
     */
    private boolean outputJarVersions;

    public void execute()
        throws MojoExecutionException
    {

        checkInput();

        findDefaultJnlpTemplateURL();

        getLog().debug( "using work directory " + getWorkDirectory() );
        getLog().debug( "using library directory " + getLibDirectory() );
        //
        // prepare layout
        //
        makeWorkingDirIfNecessary();
        
        try
        {
            copyResources( getResourcesDirectory(), getLibDirectory() );

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
            if ( ( isPack200() || getSign() != null ) && getLog().isDebugEnabled() )
            {
                logCollection(
                    "Some dependencies may be skipped. Here's the list of the artifacts that should be signed/packed: ",
                    getModifiedJnlpArtifacts() );
            }
            
            signOrRenameJars();
            packJars();
            generateJnlpFile( getWorkDirectory() );

            // package the zip. Note this is very simple. Look at the JarMojo which does more things.
            // we should perhaps package as a war when inside a project with war packaging ?
            File toFile = new File( getProject().getBuild().getDirectory(), 
                                    getProject().getBuild().getFinalName() + ".zip" );
            if ( toFile.exists() )
            {
                getLog().debug( "deleting file " + toFile );
                toFile.delete();
            }
            zipArchiver.addDirectory( getWorkDirectory() );
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

    /**
     * Detects improper includes/excludes configuration.
     * @throws MojoExecutionException if at least one of the specified includes or excludes matches no artifact, 
     *              false otherwise
     */
    void checkDependencies()
        throws MojoExecutionException
    {
        if ( dependencies == null )
        {
            return;
        }

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
            throw new MojoExecutionException( "At least one specified dependency is incorrect. " 
                                              + "Review your project configuration." );
        }
    }

    /**
     * @return true if at least one of the pattern in the list matches no artifact, false otherwise
     */
    private boolean checkDependencies( List patterns, Collection artifacts )
    {
        if ( dependencies == null )
        {
            return false;
        }

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

        Collection artifacts = isExcludeTransitive() 
            ? getProject().getDependencyArtifacts() : getProject().getArtifacts();

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
        if ( !Artifact.SCOPE_SYSTEM.equals( artifact.getScope() ) 
                        && !Artifact.SCOPE_PROVIDED.equals( artifact.getScope() ) 
                        && !Artifact.SCOPE_TEST.equals( artifact.getScope() ) )
        {
            String type = artifact.getType();
            if ( "jar".equals( type ) || "ejb-client".equals( type ) )
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

                boolean copied = copyJarAsUnprocessedToDirectoryIfNecessary( toCopy, getLibDirectory() );

                if ( copied )
                {

                    String name = toCopy.getName();
                    getModifiedJnlpArtifacts().add( name.substring( 0, name.lastIndexOf( '.' ) ) );

                }

                packagedJnlpArtifacts.add( artifact );

                if ( jnlp != null && artifactContainsClass( artifact, jnlp.getMainClass() ) )
                {
                    if ( artifactWithMainClass == null )
                    {
                        artifactWithMainClass = artifact;
                        getLog().debug( "Found main jar. Artifact " + artifactWithMainClass 
                                        + " contains the main class: " + jnlp.getMainClass() );
                    }
                    else
                    {
                        getLog().warn( "artifact " + artifact + " also contains the main class: " 
                                       + jnlp.getMainClass() + ". IGNORED." );
                    }
                }
            }
            else
            // FIXME how do we deal with native libs?
            // we should probably identify them and package inside jars that we timestamp like the native lib
            // to avoid repackaging every time. What are the types of the native libs?
            {
                verboseLog( "Skipping artifact of type " + type + " for " + getLibDirectory().getName() );
            }
            // END COPY
        }
        else
        {
            verboseLog( "Skipping artifact of scope " + artifact.getScope() + " for " + getLibDirectory().getName() );
        }
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

        File templateDirectory = getProject().getBasedir();

        if ( jnlp.getInputTemplateResourcePath() != null && jnlp.getInputTemplateResourcePath().length() > 0 )
        {
            templateDirectory = new File( jnlp.getInputTemplateResourcePath() );
        }

        if ( jnlp.getInputTemplate() == null || jnlp.getInputTemplate().length() == 0 )
        {
          getLog().debug(
                "Jnlp template file name not specified. Checking if default output file name exists: "
                + DEFAULT_TEMPLATE_LOCATION );

          File templateFile = new File( templateDirectory, DEFAULT_TEMPLATE_LOCATION );
        
          if ( templateFile.isFile() )
          {
              jnlp.setInputTemplate( DEFAULT_TEMPLATE_LOCATION );
          }
          else 
          {
              getLog().debug( "Jnlp template file not found in default location. Using inbuilt one." );
          }
        }
        else 
        {
          File templateFile = new File( templateDirectory, jnlp.getInputTemplate() );
        
          if ( !templateFile.isFile() )
          {
              throw new MojoExecutionException( "The specified JNLP template does not exist: [" + templateFile + "]" );
          }
        }
        String templateFileName = jnlp.getInputTemplate();

        Generator jnlpGenerator = new Generator( this.getProject(),
                                                 this, 
                                                 "default-jnlp-template.vm",
                                                 templateDirectory, 
                                                 jnlpOutputFile, 
                                                 templateFileName, 
                                                 this.getJnlp().getMainClass(),
                                                 getWebstartJarURLForVelocity() );

        jnlpGenerator.setExtraConfig( getGeneratorExtraConfig() );
        
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

    private GeneratorExtraConfig getGeneratorExtraConfig()
    {
        return new GeneratorExtraConfig()
        {
            public String getJnlpSpec()
            {
                // shouldn't we automatically identify the spec based on the features used in the spec?
                // also detect conflicts. If user specified 1.0 but uses a 1.5 feature we should fail in checkInput().
                if ( jnlp.getSpec() != null )
                {
                    return jnlp.getSpec();
                }
                return "1.0+";
            }
            public String getOfflineAllowed()
            {
                if ( jnlp.getOfflineAllowed() != null )
                {
                    return jnlp.getOfflineAllowed();
                }
                return "false";
            }
            public String getAllPermissions()
            {
                if ( jnlp.getAllPermissions() != null )
                {
                    return jnlp.getAllPermissions();
                }
                return "true";
            }
            public String getJ2seVersion()
            {
                if ( jnlp.getJ2seVersion() != null )
                {
                    return jnlp.getJ2seVersion();
                }
                return "1.5+";
            }
        };
    }

    private void checkInput()
        throws MojoExecutionException
    {

        getLog().debug( "a fact " + getArtifactFactory() );
        getLog().debug( "a resol " + getArtifactResolver() );
        getLog().debug( "basedir " + this.basedir );
        getLog().debug( "gzip " + isGzip() );
        getLog().debug( "pack200 " + isPack200() );
        getLog().debug( "project " + this.getProject() );
        getLog().debug( "zipArchiver " + this.zipArchiver );
        // getLog().debug( "usejnlpservlet " + this.usejnlpservlet );
        getLog().debug( "verifyjar " + isVerifyjar() );
        getLog().debug( "verbose " + isVerbose() );

        checkPack200();
        checkDependencies();

        if ( jnlp != null && jnlp.getResources() != null )
        {
            throw new MojoExecutionException(
               "The <jnlp><resources> configuration element is obsolete. Use <resourcesDirectory> instead." );
        }

        // FIXME
        /*
        if ( !"pom".equals( getProject().getPackaging() ) ) {
           throw new MojoExecutionException( "'" + getProject().getPackaging() + "' packaging unsupported. Use 'pom'" );
        }
        */
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

    // helper methods

    /**
     * Returns the flag that indicates whether or not a version attribute 
     * should be output in each jar resource element in the generated 
     * JNLP file. The default is false.
     * @return Returns the value of the {@code outputJarVersions} property.
     */
    public boolean isOutputJarVersions() 
    {
        return this.outputJarVersions;
    }

    /**
     * Sets the flag that indicates whether or not a version attribute
     * should be output in each jar resource element in the generated
     * JNLP file. The default is false.
     * @param outputJarVersions
     */
    public void setOutputJarVersions( boolean outputJarVersions )
    {
        this.outputJarVersions = outputJarVersions;
    }
    
}

