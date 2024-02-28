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

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.filter.AndArtifactFilter;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.artifact.resolver.filter.ExcludesArtifactFilter;
import org.apache.maven.artifact.resolver.filter.IncludesArtifactFilter;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProjectHelper;
import org.apache.maven.shared.utils.io.FileUtils;
import org.codehaus.mojo.webstart.generator.ExtensionGenerator;
import org.codehaus.mojo.webstart.generator.ExtensionGeneratorConfig;
import org.codehaus.mojo.webstart.generator.Generator;
import org.codehaus.mojo.webstart.generator.GeneratorConfig;
import org.codehaus.mojo.webstart.generator.GeneratorTechnicalConfig;
import org.codehaus.mojo.webstart.util.IOUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

/**
 * @author <a href="jerome@coffeebreaks.org">Jerome Lacoste</a>
 * @version $Id$
 *          TODO how to propagate the -X argument to enable verbose?
 *          TODO initialize the jnlp alias and dname.o from pom.artifactId and pom.organization.name
 */
public abstract class AbstractJnlpMojo
        extends AbstractBaseJnlpMojo
{
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    /**
     * Name of the built in jnlp template to use if none given.
     */
    private static final String BUILT_IN_JNLP_TEMPLATE_FILENAME = "default-jnlp-template.vm";

    /**
     * Name of the default jnlp template to use if user define it in the default template directory.
     */
    private static final String JNLP_TEMPLATE_FILENAME = "template.vm";

    /**
     * Name of the built in extension template to use if none is given.
     */
    private static final String BUILT_IN_EXTENSION_TEMPLATE_FILENAME = "default-jnlp-extension-template.vm";

    /**
     * Name of the default jnlp extension template to use if user define it in the default template directory.
     */
    private static final String EXTENSION_TEMPLATE_FILENAME = "extension-template.vm";
    
    private static final String JNLP_INF_APPLICATION_JNLP = "JNLP-INF/APPLICATION.JNLP";

    // ----------------------------------------------------------------------
    // Mojo Parameters
    // ----------------------------------------------------------------------

    /**
     * Represents the configuration element that specifies which of the current
     * project's dependencies will be included or excluded from the resources element
     * in the generated JNLP file.
     */
    public static class Dependencies
    {

        private List<String> includes;

        private List<String> excludes;

        public List<String> getIncludes()
        {
            return includes;
        }

        public void setIncludes( List<String> includes )
        {
            this.includes = includes;
        }

        public List<String> getExcludes()
        {
            return excludes;
        }

        public void setExcludes( List<String> excludes )
        {
            this.excludes = excludes;
        }
    }

    /**
     * Flag to create the archive or not.
     *
     * @since 1.0-beta-2
     */
    @Parameter( property = "jnlp.makeArchive", defaultValue = "true" )
    private boolean makeArchive;

    /**
     * Flag to attach the archive or not to the project's build.
     *
     * @since 1.0-beta-2
     */
    @Parameter( property = "jnlp.attachArchive", defaultValue = "true" )
    private boolean attachArchive;

    /**
     * The path of the archive to generate if {@link #makeArchive} flag is on.
     *
     * @since 1.0-beta-4
     */
    @Parameter( property = "jnlp.archive", defaultValue = "${project.build.directory}/${project.build.finalName}.zip" )
    private File archive;

    /**
     * The jnlp configuration element.
     */
    @Parameter
    private JnlpConfig jnlp;

    /**
     * [optional] extensions configuration.
     *
     * @since 1.0-beta-2
     */
    @Parameter
    private List<JnlpExtension> jnlpExtensions;

    /**
     * [optional] transitive dependencies filter - if omitted, the plugin will include all transitive dependencies.
     * Provided and test scope dependencies are always excluded.
     */
    @Parameter
    private Dependencies dependencies;

    /**
     * A placeholder for an obsoleted configuration element.
     * <p>
     * This dummy parameter is here to force the plugin configuration to fail in case one
     * didn't properly migrate from 1.0-alpha-1 to 1.0-alpha-2 configuration.
     * <p>
     * It will be removed before 1.0.
     */
    @Parameter
    private String keystore;

    /**
     */
    @Parameter( defaultValue = "${basedir}", readonly = true, required = true )
    private File basedir;

    /**
     * When set to true, this flag indicates that a version attribute should
     * be output in each of the jar resource elements in the generated
     * JNLP file.
     * <p>
     * <strong>Note: </strong> since version 1.0-beta-5 we use the version download protocol optimization (see
     * http://docs.oracle.com/javase/tutorial/deployment/deploymentInDepth/avoidingUnnecessaryUpdateChecks.html).
     */
    @Parameter( property = "jnlp.outputJarVersions", defaultValue = "false" )
    private boolean outputJarVersions;

    /**
     * Flag to skip dependencies (this can be usefull if you use a shaded jar).
     *
     * @since 1.0.0
     */
    @Parameter( property = "jnlp.skipDependencies", defaultValue = "false" )
    private boolean skipDependencies;

    /**
     * Flag to add the jnlp file inside to main jar at {@code JNLP-INF/APPLICATION.JNLP} location.
     *
     * @since 1.0.0
     */
    @Parameter( defaultValue = "true" )
    private boolean addApplicationFile;
    
    // ----------------------------------------------------------------------
    // Components
    // ----------------------------------------------------------------------

    /**
     * The project helper used to attach the artifact produced by this plugin to the project.
     */
    @Component
    private MavenProjectHelper projectHelper;

    // ----------------------------------------------------------------------
    // Fields
    // ----------------------------------------------------------------------

    /**
     * the artifacts packaged in the webstart app
     */
    private List<Artifact> packagedJnlpArtifacts = new ArrayList<>();

    /**
     * the artifacts associated to each jnlp extension
     */
    private Map<JnlpExtension, List<Artifact>> extensionsJnlpArtifacts = new HashMap<>();

    private Artifact artifactWithMainClass;

    // ----------------------------------------------------------------------
    // Mojo Implementation
    // ----------------------------------------------------------------------

    @Override
    public void execute()
            throws MojoExecutionException
    {

        boolean withExtensions = CollectionUtils.isNotEmpty( jnlpExtensions );

        if ( withExtensions )
        {
            prepareExtensions();
            findDefaultJnlpExtensionTemplateURL();
        }

        checkInput();

        getLog().debug( "using work directory " + getWorkDirectory() );
        getLog().debug( "using library directory " + getLibDirectory() );

        IOUtil ioUtil = getIoUtil();

        // ---
        // prepare layout
        // ---

        ioUtil.makeDirectoryIfNecessary( getWorkDirectory() );
        ioUtil.makeDirectoryIfNecessary( getLibDirectory() );

        try
        {
            ioUtil.copyResources( getResourcesDirectory(), getWorkDirectory() );

            artifactWithMainClass = null;

            processDependencies();

            if ( jnlp.isRequireMainClass() && artifactWithMainClass == null )
            {
                throw new MojoExecutionException(
                        "didn't find artifact with main class: " + jnlp.getMainClass() + ". Did you specify it? " );
            }

            if ( withExtensions )
            {
                processExtensionsDependencies();
            }

            // ---
            // Process native libs (FIXME)
            // ---

            processNativeLibs();

            if ( ( isPack200() || getSign() != null ) && getLog().isDebugEnabled() )
            {
                logCollection(
                        "Some dependencies may be skipped. Here's the list of the artifacts that should be signed/packed: ",
                        getModifiedJnlpArtifacts() );
            }

            // ---
            // Process collected jars
            // ---

            signOrRenameJars();

            // ---
            // Generate jnlp file
            // ---

            generateJnlpFile( getWorkDirectory() );

            // ---
            // Generate jnlp extension files
            // ---

            if ( withExtensions )
            {
                generateJnlpExtensionsFile( getWorkDirectory() );
            }

            // ---
            // Generate archive file if required
            // ---

            if ( makeArchive )
            {
                // package the zip. Note this is very simple. Look at the JarMojo which does more things.
                // we should perhaps package as a war when inside a project with war packaging ?

                ioUtil.makeDirectoryIfNecessary( archive.getParentFile() );

                ioUtil.deleteFile( archive );

                verboseLog( "Will create archive at location: " + archive );

                ioUtil.createArchive( getWorkDirectory(), archive );

                if ( attachArchive )
                {
                    // maven 2 version 2.0.1 method
                    projectHelper.attachArtifact( getProject(), "zip", archive );
                }
            }
        }
        catch ( MojoExecutionException e )
        {
            throw e;
        }
        catch ( Exception e )
        {
            throw new MojoExecutionException( "Failure to run the plugin: ", e );
        }
    }

    // ----------------------------------------------------------------------
    // Protected Methods
    // ----------------------------------------------------------------------

    protected JnlpConfig getJnlp()
    {
        return jnlp;
    }

    protected Dependencies getDependencies()
    {
        return this.dependencies;
    }

    // ----------------------------------------------------------------------
    // Private Methods
    // ----------------------------------------------------------------------

    void checkJnlpConfig()
            throws MojoExecutionException
    {
        if ( jnlp == null )
        {
            throw new MojoExecutionException( "jnlp must be set to generate config!" );
        }
        JnlpFileType type = jnlp.getType();
        if ( type == null )
        {
            throw new MojoExecutionException( "jnlp must define a default jnlp type file to generate (among " +
                                                      Arrays.toString( JnlpFileType.values() ) + " )." );
        }
        if ( !type.isRequireMainClass() && StringUtils.isNotBlank( jnlp.getMainClass() ) )
        {
            getLog().warn( "Jnlp file of type '" + type +
                                   "' does not support mainClass, value will not be accessible in template." );
            jnlp.setMainClass( null );
            
            addApplicationFile = false;
        }
    }

    /**
     * Detects improper includes/excludes configuration.
     *
     * @throws MojoExecutionException if at least one of the specified includes or excludes matches no artifact,
     *                                false otherwise
     */
    void checkDependencies()
            throws MojoExecutionException
    {
        if ( dependencies == null )
        {
            return;
        }

        boolean failed = false;

        Collection<Artifact> artifacts = getProject().getArtifacts();

        getLog().debug( "artifacts: " + artifacts.size() );

        if ( dependencies.getIncludes() != null && !dependencies.getIncludes().isEmpty() )
        {
            failed = checkDependencies( dependencies.getIncludes(), artifacts );
        }
        if ( dependencies.getExcludes() != null && !dependencies.getExcludes().isEmpty() )
        {
            failed = checkDependencies( dependencies.getExcludes(), artifacts ) || failed;
        }

        if ( failed )
        {
            throw new MojoExecutionException(
                    "At least one specified dependency is incorrect. Review your project configuration." );
        }
    }

    /**
     * @param patterns  list of patterns to test over artifacts
     * @param artifacts collection of artifacts to check
     * @return true if at least one of the pattern in the list matches no artifact, false otherwise
     */
    private boolean checkDependencies( List<String> patterns, Collection<Artifact> artifacts )
    {
        if ( dependencies == null )
        {
            return false;
        }

        boolean failed = false;
        for ( String pattern : patterns )
        {
            failed = ensurePatternMatchesAtLeastOneArtifact( pattern, artifacts ) || failed;
        }
        return failed;
    }

    /**
     * @param pattern   pattern to test over artifacts
     * @param artifacts collection of artifacts to check
     * @return true if filter matches no artifact, false otherwise *
     */
    private boolean ensurePatternMatchesAtLeastOneArtifact( String pattern, Collection<Artifact> artifacts )
    {
        List<String> onePatternList = new ArrayList<>();
        onePatternList.add( pattern );
        ArtifactFilter filter = new IncludesArtifactFilter( onePatternList );

        boolean noMatch = true;
        for ( Artifact artifact : artifacts )
        {
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
     * @throws MojoExecutionException if could not process dependencies
     */
    private void processDependencies()
            throws MojoExecutionException
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

        Collection<Artifact> artifacts =
                isExcludeTransitive() ? getProject().getDependencyArtifacts() : getProject().getArtifacts();

        for ( Artifact artifact : artifacts )
        {
            if ( filter.include( artifact ) )
            {
                processDependency( artifact );
            }
        }
    }

    private void processDependency( Artifact artifact )
            throws MojoExecutionException
    {
        // TODO: scope handler
        // Include runtime and compile time libraries
        if ( !Artifact.SCOPE_SYSTEM.equals( artifact.getScope() ) &&
                !Artifact.SCOPE_PROVIDED.equals( artifact.getScope() ) &&
                !Artifact.SCOPE_TEST.equals( artifact.getScope() ) )
        {
            String type = artifact.getType();
            if ( "jar".equals( type ) || "ejb-client".equals( type ) )
            {

                boolean mainArtifact = false;
                if ( jnlp.isRequireMainClass() )
                {

                    // try to find if this dependency contains the main class
                    boolean containsMainClass =
                            getArtifactUtil().artifactContainsClass( artifact, jnlp.getMainClass() );

                    if ( containsMainClass )
                    {
                        if ( artifactWithMainClass == null )
                        {
                            mainArtifact = true;
                            artifactWithMainClass = artifact;
                            getLog().debug(
                                    "Found main jar. Artifact " + artifactWithMainClass + " contains the main class: " +
                                            jnlp.getMainClass() );
                        }
                        else
                        {
                            getLog().warn(
                                    "artifact " + artifact + " also contains the main class: " + jnlp.getMainClass() +
                                            ". IGNORED." );
                        }
                    }
                }

                if ( skipDependencies && !mainArtifact )
                {
                    return;
                }

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

                String name =
                        getDependencyFilenameStrategy().getDependencyFilename( artifact, outputJarVersions, isUseUniqueVersions() );

                boolean copied = copyJarAsUnprocessedToDirectoryIfNecessary( toCopy, getLibDirectory(), name );

                if ( copied )
                {

                    getModifiedJnlpArtifacts().add( name.substring( 0, name.lastIndexOf( '.' ) ) );

                }

                packagedJnlpArtifacts.add( artifact );

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

    private void generateJnlpFile( File outputDirectory )
            throws MojoExecutionException
    {
    	getLog().info("Generate the JNLP file.");
        // ---
        // get output file
        // ---

        if ( StringUtils.isBlank( jnlp.getOutputFile() ) )
        {
            getLog().debug( "Jnlp output file name not specified. Using default output file name: launch.jnlp." );
            jnlp.setOutputFile( "launch.jnlp" );
        }
        File jnlpOutputFile = new File( outputDirectory, jnlp.getOutputFile() );

        // ---
        // get template directory
        // ---

        File templateDirectory;

        if ( StringUtils.isNotBlank( jnlp.getInputTemplateResourcePath() ) )
        {
            templateDirectory = new File( jnlp.getInputTemplateResourcePath() );
            getLog().debug( "Use jnlp directory : " + templateDirectory );
        }
        else
        {
            // use default template directory
            templateDirectory = getTemplateDirectory();
            getLog().debug( "Use default template directory : " + templateDirectory );
        }

        // ---
        // get template filename
        // ---

        if ( StringUtils.isBlank( jnlp.getInputTemplate() ) )
        {
            getLog().debug( "Jnlp template file name not specified. Checking if default output file name exists: " +
                                    JNLP_TEMPLATE_FILENAME );

            File templateFile = new File( templateDirectory, JNLP_TEMPLATE_FILENAME );

            if ( templateFile.isFile() )
            {
                jnlp.setInputTemplate( JNLP_TEMPLATE_FILENAME );
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
                throw new MojoExecutionException(
                        "The specified JNLP template does not exist: [" + templateFile + "]" );
            }
        }
        String templateFileName = jnlp.getInputTemplate();

        GeneratorTechnicalConfig generatorTechnicalConfig =
                new GeneratorTechnicalConfig( getProject(), templateDirectory, jnlp.getType().getDefaultTemplateName(),
                                              jnlpOutputFile, templateFileName, jnlp.getMainClass(),
                                              getWebstartJarURLForVelocity(), getEncoding() );

        GeneratorConfig generatorConfig =
                new GeneratorConfig( getLibPath(), isPack200(), outputJarVersions, isUseUniqueVersions(), artifactWithMainClass,
                                     getDependencyFilenameStrategy(), packagedJnlpArtifacts, jnlpExtensions, getCodebase(),
                                     jnlp );

        Generator jnlpGenerator = new Generator( getLog(), generatorTechnicalConfig, generatorConfig );

        try
        {
            jnlpGenerator.generate();
        }
        catch ( Exception e )
        {
            getLog().debug( e.toString() );
            throw new MojoExecutionException( "Could not generate the JNLP deployment descriptor", e );
        }

        if ( addApplicationFile )
        {
        	getLog().info("Add the application file, artifactWithMainClass: " + artifactWithMainClass);

        	// must handle outputJarVersions == true
        	String targetFilename =
                    getDependencyFilenameStrategy().getDependencyFilename( artifactWithMainClass, outputJarVersions, isUseUniqueVersions() );
        	
            File jarFile = new File( getLibDirectory(), targetFilename);

            if ( isVerbose() )
            {
                getLog().info( "Add " + JNLP_INF_APPLICATION_JNLP + " to " + jarFile );
            }
            
            JarFile inputJar = null;
            try
            {

                inputJar = new JarFile( jarFile );
                File tempJarFile = new File( jarFile.getParentFile(), jarFile.getName() + "-temp" );
                JarOutputStream jarOutputStream = new JarOutputStream( new FileOutputStream( tempJarFile ) );

                try
                {
                    Enumeration<JarEntry> entries = inputJar.entries();
                    while ( entries.hasMoreElements() )
                    {
                        JarEntry jarEntry = entries.nextElement();
                        
                        if (JNLP_INF_APPLICATION_JNLP.equals(jarEntry.getName())) {
                            // skip existing JNLP-INF/APPLICATION.JNLP from jar
                        	getLog().info("Skip add existing " + JNLP_INF_APPLICATION_JNLP);
                        	continue;
                        }
                        
                        jarOutputStream.putNextEntry( jarEntry );
                        InputStream inputStream = inputJar.getInputStream( jarEntry );
                        org.apache.maven.shared.utils.io.IOUtil.copy( inputStream, jarOutputStream );

                    }
                    JarEntry jarEntry = new JarEntry( JNLP_INF_APPLICATION_JNLP );
                    jarOutputStream.putNextEntry( jarEntry );
                    jarOutputStream.write( FileUtils.fileRead( jnlpOutputFile ).getBytes() );
                    jarOutputStream.flush();
                    jarOutputStream.close();

                }
                finally
                {
                    org.apache.maven.shared.utils.io.IOUtil.close( jarOutputStream );
                }

//                jarFile.delete();
                signJar( tempJarFile, jarFile, false );

            }
            catch ( IOException e )
            {
                throw new MojoExecutionException( "Could not copy generated JNLP deployment descriptor to application file", e );
            }
            finally {
            	if (inputJar != null) {
            		try {
						inputJar.close();
					} 
            		catch (IOException e) 
            		{
						// ignore
					}
            	}
            }
        }
    }

    private void processNativeLibs()
    {
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
    }

    private void logCollection( final String prefix, final Collection collection )
    {
        getLog().debug( prefix + " " + collection );
        if ( collection == null )
        {
            return;
        }
        for ( Object aCollection : collection )
        {
            getLog().debug( prefix + aCollection );
        }
    }

    private void checkInput()
            throws MojoExecutionException
    {

        getLog().debug( "basedir " + this.basedir );
        getLog().debug( "gzip " + isGzip() );
        getLog().debug( "pack200 " + isPack200() );
        getLog().debug( "Commons Compress pack200 " + isCommonsCompressEnabled() );
        getLog().debug( "project " + this.getProject() );
        getLog().debug( "verbose " + isVerbose() );

        checkJnlpConfig();
        checkDependencyFilenameStrategy();
        checkDependencies();

        findDefaultTemplateURL( jnlp.getType() );

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

    private void checkExtension( JnlpExtension extension )
            throws MojoExecutionException
    {
        if ( StringUtils.isEmpty( extension.getName() ) )
        {
            throw new MojoExecutionException( "JnlpExtension name is mandatory. Review your project configuration." );
        }
        if ( StringUtils.isEmpty( extension.getVendor() ) )
        {
            throw new MojoExecutionException( "JnlpExtension vendor is mandatory. Review your project configuration." );
        }
        if ( StringUtils.isEmpty( extension.getTitle() ) )
        {
            throw new MojoExecutionException( "JnlpExtension name is title. Review your project configuration." );
        }
        if ( extension.getIncludes() == null || extension.getIncludes().isEmpty() )
        {
            throw new MojoExecutionException(
                    "JnlpExtension need at least one include artifact. Review your project configuration." );
        }
    }

    protected URL findDefaultJnlpExtensionTemplateURL()
    {
        return getClass().getClassLoader().getResource( "default-jnlp-extension-template.vm" );
    }

    /**
     * Prepare extensions.
     * <p>
     * Copy all includes of all extensions as to be excluded.
     *
     * @throws MojoExecutionException if could not prepare extensions
     */
    private void prepareExtensions()
            throws MojoExecutionException
    {
        List<String> includes = new ArrayList<>();
        for ( JnlpExtension extension : jnlpExtensions )
        {
            // Check extensions (mandatory name, title and vendor and at least one include)

            checkExtension( extension );

            for ( String o : extension.getIncludes() )
            {
                includes.add( o.trim() );
            }

            if ( StringUtils.isEmpty( extension.getOutputFile() ) )
            {
                String name = extension.getName() + ".jnlp";
                verboseLog(
                        "Jnlp extension output file name not specified. Using default output file name: " + name + "." );
                extension.setOutputFile( name );
            }
        }
        // copy all includes libs fro extensions to be exclude from the mojo
        // treatments (extensions by nature are already signed)
        if ( dependencies == null )
        {
            dependencies = new Dependencies();
        }

        if ( dependencies.getExcludes() == null )
        {
            dependencies.setExcludes( new ArrayList<String>() );
        }

        dependencies.getExcludes().addAll( includes );
    }

    /**
     * Iterate through all the extensions dependencies declared in the project and
     * collect all the runtime scope dependencies for inclusion in the .zip and just
     * copy them to the lib directory.
     * <p>
     * TODO, should check that all dependencies are well signed with the same
     * extension with the same signer.
     *
     * @throws MojoExecutionException TODO
     */
    private void processExtensionsDependencies()
            throws MojoExecutionException
    {

        Collection<Artifact> artifacts =
                isExcludeTransitive() ? getProject().getDependencyArtifacts() : getProject().getArtifacts();

        for ( JnlpExtension extension : jnlpExtensions )
        {
            ArtifactFilter filter = new IncludesArtifactFilter( extension.getIncludes() );

            for ( Artifact artifact : artifacts )
            {
                if ( filter.include( artifact ) )
                {
                    processExtensionDependency( extension, artifact );
                }
            }
        }
    }

    private void processExtensionDependency( JnlpExtension extension, Artifact artifact )
            throws MojoExecutionException
    {
        // TODO: scope handler
        // Include runtime and compile time libraries
        if ( !Artifact.SCOPE_SYSTEM.equals( artifact.getScope() ) &&
                !Artifact.SCOPE_PROVIDED.equals( artifact.getScope() ) &&
                !Artifact.SCOPE_TEST.equals( artifact.getScope() ) )
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

                // check jar is signed
                boolean jarSigned = isJarSigned( toCopy );
                if ( !jarSigned )
                {
                    throw new IllegalStateException(
                            "artifact " + artifact + " must be signed as part of an extension.." );
                }

                String targetFilename =
                        getDependencyFilenameStrategy().getDependencyFilename( artifact, outputJarVersions, isUseUniqueVersions() );

                File targetFile = new File( getLibDirectory(), targetFilename );
                boolean copied = getIoUtil().shouldCopyFile( toCopy, targetFile );

                if ( copied )
                {
                    getIoUtil().copyFile( toCopy, targetFile );
                    verboseLog( "copy extension artifact " + toCopy );
                }
                else
                {
                    verboseLog( "already up to date artifact " + toCopy );
                }

                // save the artifact dependency for the extension

                List<Artifact> deps = extensionsJnlpArtifacts.get( extension );
                if ( deps == null )
                {
                    deps = new ArrayList<>();
                    extensionsJnlpArtifacts.put( extension, deps );
                }
                deps.add( artifact );
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

    private void generateJnlpExtensionsFile( File outputDirectory )
            throws MojoExecutionException
    {
        for ( JnlpExtension jnlpExtension : jnlpExtensions )
        {
            generateJnlpExtensionFile( outputDirectory, jnlpExtension );
        }
    }

    private void generateJnlpExtensionFile( File outputDirectory, JnlpExtension extension )
            throws MojoExecutionException
    {

        // ---
        // get output file
        // ---

        File jnlpOutputFile = new File( outputDirectory, extension.getOutputFile() );

        // ---
        // get template directory
        // ---

        File templateDirectory;

        if ( StringUtils.isNotBlank( extension.getInputTemplateResourcePath() ) )
        {
            // if user overrides the input template resource path
            templateDirectory = new File( extension.getInputTemplateResourcePath() );
        }
        else
        {

            // use default template directory
            templateDirectory = getTemplateDirectory();
            getLog().debug( "Use default jnlp directory : " + templateDirectory );
        }

        // ---
        // get template filename
        // ---

        if ( StringUtils.isBlank( extension.getInputTemplate() ) )
        {
            getLog().debug(
                    "Jnlp extension template file name not specified. Checking if default output file name exists: " +
                            EXTENSION_TEMPLATE_FILENAME );

            File templateFile = new File( templateDirectory, EXTENSION_TEMPLATE_FILENAME );

            if ( templateFile.isFile() )
            {
                extension.setInputTemplate( EXTENSION_TEMPLATE_FILENAME );
            }
            else
            {
                getLog().debug( "Jnlp extension template file not found in default location. Using inbuilt one." );
            }
        }
        else
        {
            File templateFile = new File( templateDirectory, extension.getInputTemplate() );

            if ( !templateFile.isFile() )
            {
                throw new MojoExecutionException(
                        "The specified JNLP extension template does not exist: [" + templateFile + "]" );
            }
        }
        String templateFileName = extension.getInputTemplate();

        GeneratorTechnicalConfig generatorTechnicalConfig =
                new GeneratorTechnicalConfig( getProject(), templateDirectory, BUILT_IN_EXTENSION_TEMPLATE_FILENAME,
                                              jnlpOutputFile, templateFileName, getJnlp().getMainClass(),
                                              getWebstartJarURLForVelocity(), getEncoding() );

        ExtensionGeneratorConfig extensionGeneratorConfig =
                new ExtensionGeneratorConfig( getLibPath(), isPack200(), outputJarVersions, isUseUniqueVersions(),
                                              artifactWithMainClass, getDependencyFilenameStrategy(),
                                              extensionsJnlpArtifacts, getCodebase(), extension );
        ExtensionGenerator jnlpGenerator =
                new ExtensionGenerator( getLog(), generatorTechnicalConfig, extensionGeneratorConfig );

//        jnlpGenerator.setExtraConfig( new ExtensionGeneratorExtraConfig( extension, getCodebase() ) );

        try
        {
            jnlpGenerator.generate();
        }
        catch ( Exception e )
        {
            getLog().debug( e.toString() );
            throw new MojoExecutionException( "Could not generate the JNLP deployment descriptor", e );
        }
    }

}

