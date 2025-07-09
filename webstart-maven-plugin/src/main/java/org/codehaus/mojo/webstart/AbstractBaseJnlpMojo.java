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

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.mojo.webstart.dependency.filenaming.DependencyFilenameStrategy;
import org.codehaus.mojo.webstart.pack200.Pack200Config;
import org.codehaus.mojo.webstart.pack200.Pack200Tool;
import org.codehaus.mojo.webstart.sign.SignConfig;
import org.codehaus.mojo.webstart.sign.SignTool;
import org.codehaus.mojo.webstart.util.ArtifactUtil;
import org.codehaus.mojo.webstart.util.IOUtil;
import org.codehaus.mojo.webstart.util.JarUtil;
import org.sonatype.plexus.components.sec.dispatcher.SecDispatcher;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private static final String DEFAULT_RESOURCES_DIR = "src/main/jnlp/resources";

    /**
     * unprocessed files (that will be signed) are prefixed with this
     */
    private static final String UNPROCESSED_PREFIX = "unprocessed_";

    /**
     * Suffix extension of a jar file.
     */
    public static final String JAR_SUFFIX = ".jar";

    // ----------------------------------------------------------------------
    // Mojo Parameters
    // ----------------------------------------------------------------------

    /**
     * Local repository.
     */
    @Parameter( defaultValue = "${localRepository}", required = true, readonly = true )
    private ArtifactRepository localRepository;

    /**
     * The collection of remote artifact repositories.
     */
    @Parameter( defaultValue = "${project.remoteArtifactRepositories}", required = true, readonly = true )
    private List<ArtifactRepository> remoteRepositories;

    /**
     * The directory in which files will be stored prior to processing.
     */
    @Parameter( property = "jnlp.workDirectory", defaultValue = "${project.build.directory}/jnlp", required = true )
    private File workDirectory;

    /**
     * The path where the libraries are placed within the jnlp structure.
     */
    @Parameter( property = "jnlp.libPath", defaultValue = "" )
    protected String libPath;

    /**
     * The location of the directory (relative or absolute) containing non-jar resources that
     * are to be included in the JNLP bundle.
     */
    @Parameter( property = "jnlp.resourcesDirectory" )
    private File resourcesDirectory;

    /**
     * The location where the JNLP Velocity template files are stored.
     */
    @Parameter( property = "jnlp.templateDirectory", defaultValue = "${project.basedir}/src/main/jnlp", required = true )
    private File templateDirectory;

    /**
     * The Pack200 Config.
     *
     * @since 1.0-beta-4
     */
    @Parameter
    private Pack200Config pack200;

    /**
     * The Sign Config.
     */
    @Parameter
    private SignConfig sign;

    /**
     * Indicates whether or not gzip archives will be created for each of the jar
     * files included in the webstart bundle.
     */
    @Parameter( property = "jnlp.gzip", defaultValue = "false" )
    private boolean gzip;

    /**
     * Enable verbose output.
     */
    @Parameter( property = "webstart.verbose", alias = "verbose", defaultValue = "false" )
    private boolean verbose;

    /**
     * Set to true to exclude all transitive dependencies.
     */
    @Parameter( property = "jnlp.excludeTransitive" )
    private boolean excludeTransitive;

    /**
     * The code base to use on the generated jnlp files.
     *
     * @since 1.0-beta-2
     */
    @Parameter( property = "jnlp.codebase", defaultValue = "${project.url}/jnlp" )
    private String codebase;

    /**
     * Encoding used to read and write jnlp files.
     * <p>
     * <strong>Note:</strong> If this property is not defined, then will use a default value {@code utf-8}.
     *
     * @since 1.0-beta-2
     */
    @Parameter( property = "jnlp.encoding", defaultValue = "${project.build.sourceEncoding}" )
    private String encoding;

    /**
     * Define whether to remove existing signatures.
     */
    @Parameter( property = "jnlp.unsign", alias = "unsign", defaultValue = "false" )
    private boolean unsignAlreadySignedJars;

    /**
     * To authorize or not to unsign some already signed jar.
     * <p>
     * If set to false and the {@code unsign} parameter is set to {@code true} then the build will fail if there is
     * a jar to unsign, to avoid this use then the extension jnlp component.
     *
     * @since 1.0-beta-2
     */
    @Parameter( property = "jnlp.canUnsign", defaultValue = "true" )
    private boolean canUnsign;

    /**
     * To update manifest entries of all jar resources.
     * <p>
     * Since jdk 1.7u45, you need to add some entries to be able to open jnlp files in High security level.
     * See http://www.oracle.com/technetwork/java/javase/7u45-relnotes-2016950.html
     * <p>
     * <strong>Note:</strong> Won't affect any already signed jar resources if you configuration does not authorize it.
     * <p>
     * See parameters {@link #unsignAlreadySignedJars} and {@link #canUnsign}.
     *
     * @since 1.0-beta-4
     */
    @Parameter
    private Map<String, String> updateManifestEntries;

    /**
     * Compile class-path elements used to search for the keystore
     * (if kestore location was prefixed by {@code classpath:}).
     *
     * @since 1.0-beta-4
     */
    @Parameter( defaultValue = "${project.compileClasspathElements}", required = true, readonly = true )
    private List<?> compileClassPath;

    /**
     * Naming strategy for dependencies of a jnlp application.
     * <p>
     * The strategy purpose is to transform the name of the dependency file.
     * <p>
     * The actual authorized values are:
     * <ul>
     * <li><strong>simple</strong>: artifactId[-classifier]-version.jar</li>
     * <li><strong>full</strong>: groupId-artifactId[-classifier]-version.jar</li>
     * </ul>
     * <p>
     * Default value is {@code simple} which avoid any collision of naming.
     *
     * @since 1.0-beta-5
     */
    @Parameter( property = "jnlp.filenameMapping", defaultValue = "simple", required = true )
    private String filenameMapping;

    /**
     * Use unique version for any snapshot dependency, or just use the {@code -SNAPSHOT} version suffix.
     *
     * @since 1.0-beta-7
     */
    @Parameter( property = "jnlp.useUniqueVersions", defaultValue = "false" )
    private boolean useUniqueVersions;

    // ----------------------------------------------------------------------
    // Components
    // ----------------------------------------------------------------------

    /**
     * Sign tool.
     */
    @Component
    private SignTool signTool;

    /**
     * All available pack200 tools.
     * <p>
     * We use a plexus list injection instead of a direct component injection since for a jre 1.4, we will at the
     * moment have no implementation of this tool.
     * <p>
     * Later in the execute of mojo, we will check if at least one implementation is available if required.
     *
     * @since 1.0-beta-2
     */
    @Component( role = Pack200Tool.class )
    private Pack200Tool pack200Tool;

    /**
     * Artifact helper.
     *
     * @since 1.0-beta-4
     */
    @Component
    private ArtifactUtil artifactUtil;

    /**
     * io helper.
     *
     * @since 1.0-beta-4
     */
    @Component
    private IOUtil ioUtil;

    /**
     * Jar util.
     *
     * @since 1.0-beta-4
     */
    @Component( hint = "default" )
    private JarUtil jarUtil;

    /**
     * All dependency filename strategy indexed by their role-hint.
     *
     * @since 1.0-beta-5
     */
    @Component( role = DependencyFilenameStrategy.class )
    private Map<String, DependencyFilenameStrategy> dependencyFilenameStrategyMap;

    /**
     * @since 1.0-beta-7
     */
    @Component( hint = "mng-4384" )
    private SecDispatcher securityDispatcher;

    // ----------------------------------------------------------------------
    // Fields
    // ----------------------------------------------------------------------

    /**
     * List of detected modified artifacts (will then re-apply stuff on them).
     */
    private final List<String> modifiedJnlpArtifacts = new ArrayList<>();

    // the jars to sign and pack are selected if they are prefixed by UNPROCESSED_PREFIX.
    // as the plugin copies the new versions locally before signing/packing them
    // we just need to see if the plugin copied a new version
    // We achieve that by only filtering files modified after the plugin was started
    // Note: if other files (the pom, the keystore config) have changed, one needs to clean
    private final FileFilter unprocessedJarFileFilter;

    /**
     * Filter of processed jar files.
     */
    private final FileFilter processedJarFileFilter;

    /**
     * Filter of jar files that need to be pack200.
     */
    private final FileFilter unprocessedPack200FileFilter;

    /**
     * The dependency filename strategy.
     */
    private DependencyFilenameStrategy dependencyFilenameStrategy;

    /**
     * Creates a new {@code AbstractBaseJnlpMojo}.
     */
    public AbstractBaseJnlpMojo()
    {

        processedJarFileFilter = new FileFilter()
        {
            /**
             * {@inheritDoc}
             */
            public boolean accept( File pathname )
            {
                return pathname.isFile() && pathname.getName().endsWith( JAR_SUFFIX ) &&
                        !pathname.getName().startsWith( UNPROCESSED_PREFIX );
            }
        };

        unprocessedJarFileFilter = new FileFilter()
        {
            /**
             * {@inheritDoc}
             */
            public boolean accept( File pathname )
            {
                return pathname.isFile() && pathname.getName().startsWith( UNPROCESSED_PREFIX ) &&
                        pathname.getName().endsWith( JAR_SUFFIX );
            }
        };

        unprocessedPack200FileFilter = new FileFilter()
        {
            /**
             * {@inheritDoc}
             */
            public boolean accept( File pathname )
            {
                return pathname.isFile() && pathname.getName().startsWith( UNPROCESSED_PREFIX ) &&
                        ( pathname.getName().endsWith( JAR_SUFFIX + Pack200Tool.PACK_GZ_EXTENSION ) ||
                                pathname.getName().endsWith( JAR_SUFFIX + Pack200Tool.PACK_EXTENSION ) );
            }
        };
    }

    // ----------------------------------------------------------------------
    // Public Methods
    // ----------------------------------------------------------------------

    public abstract MavenProject getProject();

    /**
     * Returns the library path. This is ths subpath within the working directory, where the libraries are placed.
     * If the path is not configured it is <code>null</code>.
     *
     * @return the library path or <code>null</code> if not configured.
     */
    public String getLibPath()
    {
        if ( StringUtils.isBlank( libPath ) )
        {
            return null;
        }
        return libPath;
    }

    /**
     * Returns the flag that indicates whether or not jar resources
     * will be compressed using pack200.
     *
     * @return Returns the value of the pack200.enabled field.
     */
    public boolean isPack200()
    {
        return pack200 != null && pack200.isEnabled();
    }

    /**
     * Returns the flag that indicates whether or not jar resources
     * will be compressed using the Apache Commons Compress pack200 variant.
     *
     * @return Returns the value of the pack200.enabled field.
     */
    public boolean isCommonsCompressEnabled()
    {
        return pack200 != null && pack200.isCommonsCompressEnabled();
    }

    /**
     * Returns the files to be passed without pack200 compression.
     *
     * @return Returns the list value of the pack200.passFiles.
     */
    public List<String> getPack200PassFiles()
    {
        return pack200 == null ? null : pack200.getPassFiles();
    }

    // ----------------------------------------------------------------------
    // Protected Methods
    // ----------------------------------------------------------------------

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
    protected List<ArtifactRepository> getRemoteRepositories()
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
     * Returns the collection of artifacts that have been modified
     * since the last time this mojo was run.
     *
     * @return Returns the value of the modifiedJnlpArtifacts field.
     */
    protected List<String> getModifiedJnlpArtifacts()
    {
        return modifiedJnlpArtifacts;
    }

    /**
     * @return the mojo encoding to use to write files.
     */
    protected String getEncoding()
    {
        if ( StringUtils.isEmpty( encoding ) )
        {
            encoding = "utf-8";
            getLog().warn( "No encoding defined, will use the default one : " + encoding );
        }
        return encoding;
    }

    protected DependencyFilenameStrategy getDependencyFilenameStrategy()
    {
        if ( dependencyFilenameStrategy == null )
        {
            dependencyFilenameStrategy = dependencyFilenameStrategyMap.get( filenameMapping );
        }
        return dependencyFilenameStrategy;
    }

    protected boolean isUseUniqueVersions()
    {
        return useUniqueVersions;
    }

    protected void checkDependencyFilenameStrategy()
            throws MojoExecutionException
    {
        if ( getDependencyFilenameStrategy() == null )
        {

            dependencyFilenameStrategy = dependencyFilenameStrategyMap.get( filenameMapping );
            if ( dependencyFilenameStrategy == null )
            {
                throw new MojoExecutionException(
                        "Could not find filenameMapping named '" + filenameMapping + "', use one of the following one: " +
                                dependencyFilenameStrategyMap.keySet() );
            }
        }
    }

    /**
     * Conditionally copy the jar file into the target directory.
     * The operation is not performed when a signed target file exists and is up to date.
     * The signed target file name is taken from the <code>sourceFile</code> name.E
     * The unsigned target file name is taken from the <code>sourceFile</code> name prefixed with UNPROCESSED_PREFIX.
     * TODO this is confusing if the sourceFile is already signed. By unsigned we really mean 'unsignedbyus'
     *
     * @param sourceFile      source file to copy
     * @param targetDirectory location of the target directory where to copy file
     * @param targetFilename  [optional] to change the target filename to use (if {@code null} will
     *                        use the sourceFile name).
     * @return <code>true</code> when the file was copied, <code>false</code> otherwise.
     * @throws IllegalArgumentException if sourceFile is <code>null</code> or
     *                                  <code>sourceFile.getName()</code> is <code>null</code>
     * @throws MojoExecutionException   if an error occurs attempting to copy the file.
     */
    protected boolean copyJarAsUnprocessedToDirectoryIfNecessary( File sourceFile, File targetDirectory,
                                                                  String targetFilename )
            throws MojoExecutionException
    {

        if ( sourceFile == null )
        {
            throw new IllegalArgumentException( "sourceFile is null" );
        }

        if ( targetFilename == null )
        {
            targetFilename = sourceFile.getName();
        }

        File signedTargetFile = new File( targetDirectory, targetFilename );

        File unsignedTargetFile = toUnprocessFile( targetDirectory, targetFilename );

        boolean shouldCopy =
                !signedTargetFile.exists() || ( signedTargetFile.lastModified() < sourceFile.lastModified() );

        shouldCopy &=
                ( !unsignedTargetFile.exists() || ( unsignedTargetFile.lastModified() < sourceFile.lastModified() ) );

        if ( shouldCopy )
        {
            getIoUtil().copyFile( sourceFile, unsignedTargetFile );

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
     * @throws MojoExecutionException if can not sign or rename jars
     */
    protected void signOrRenameJars()
            throws MojoExecutionException
    {

        if ( sign != null )
        {
            try
            {
                ClassLoader loader = getCompileClassLoader();
                sign.init( getWorkDirectory(), getLog().isDebugEnabled(), signTool, securityDispatcher, loader );
            }
            catch ( MalformedURLException e )
            {
                throw new MojoExecutionException( "Could not create classloader", e );
            }

            if ( unsignAlreadySignedJars )
            {
                removeExistingSignatures( getLibDirectory() );
            }

            if ( isPack200() )
            {

                //TODO tchemit  use a temporary directory to pack-unpack

                // http://java.sun.com/j2se/1.5.0/docs/guide/deployment/deployment-guide/pack200.html
                // we need to pack then unpack the files before signing them
                unpackJars( getLibDirectory(), isCommonsCompressEnabled() );

                // As out current Pack200 ant tasks don't give us the ability to use a temporary area for
                // creating those temporary packing, we have to delete the temporary files.
                ioUtil.deleteFiles( getLibDirectory(), unprocessedPack200FileFilter );
                // specs says that one should do it twice when there are unsigned jars??
                // Pack200.unpackJars( applicationDirectory, updatedPack200FileFilter );
            }

            if ( MapUtils.isNotEmpty( updateManifestEntries ) )
            {
                updateManifestEntries( getLibDirectory() );
            }

            int signedJars = signJars( getLibDirectory() );

            if ( signedJars != getModifiedJnlpArtifacts().size() )
            {
                throw new IllegalStateException(
                        "The number of signed artifacts (" + signedJars + ") differ from the number of modified " +
                                "artifacts (" + getModifiedJnlpArtifacts().size() + "). Implementation error" );
            }

        }
        else
        {
            makeUnprocessedFilesFinal( getLibDirectory() );
        }

        if ( isPack200() )
        {
            verboseLog( "-- Pack jars" );
            pack200Jars( getLibDirectory(), processedJarFileFilter );
        }
    }


    protected void pack200Jars( File directory, FileFilter filter )
            throws MojoExecutionException
    {
        try
        {
            getPack200Tool().packJars( directory, filter, isGzip(), getPack200PassFiles(), isCommonsCompressEnabled() );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Could not pack200 jars: ", e );
        }
    }

    protected URL findDefaultTemplateURL( JnlpFileType fileType )
    {
        return getClass().getClassLoader().getResource( fileType.getDefaultTemplateName() );
    }

    /**
     * @return something of the form jar:file:..../webstart-maven-plugin-.....jar!/
     */
    protected String getWebstartJarURLForVelocity()
    {
        String url = findDefaultTemplateURL( JnlpFileType.application ).toString();
        return url.substring( 0, url.indexOf( "!" ) + 2 );
    }

    protected boolean isJarSigned( File jarFile )
            throws MojoExecutionException
    {

        return signTool.isJarSigned( jarFile );
    }

    protected ArtifactUtil getArtifactUtil()
    {
        return artifactUtil;
    }

    protected IOUtil getIoUtil()
    {
        return ioUtil;
    }

    protected Pack200Tool getPack200Tool()
    {
        return pack200Tool;
    }

    /**
     * Log as info when verbose or info is enabled, as debug otherwise.
     *
     * @param msg the message to display
     */
    protected void verboseLog( String msg )
    {
        if ( isVerbose() )
        {
            getLog().info( msg );
        }
        else
        {
            getLog().debug( msg );
        }
    }

    // ----------------------------------------------------------------------
    // Private Methods
    // ----------------------------------------------------------------------

    private void unpackJars( File directory, boolean commonsCompress )
            throws MojoExecutionException
    {
        getLog().info( "-- Unpack jars before sign operation " );

        verboseLog(
                "see http://docs.oracle.com/javase/7/docs/technotes/guides/deployment/deployment-guide/pack200.html" );

        // pack
        pack200Jars( directory, unprocessedJarFileFilter );

        // then unpack
        try
        {
            getPack200Tool().unpackJars( directory, unprocessedPack200FileFilter, commonsCompress );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Could not unpack200 jars: ", e );
        }
    }

    private int makeUnprocessedFilesFinal( File directory )
            throws MojoExecutionException
    {
        File[] jarFiles = directory.listFiles( unprocessedJarFileFilter );

        getLog().debug(
                "makeUnprocessedFilesFinal in " + directory + " found " + jarFiles.length + " file(s) to rename" );

        if ( jarFiles.length == 0 )
        {
            return 0;
        }

        for ( File unprocessedJarFile : jarFiles )
        {

            File finalJar = toProcessFile( unprocessedJarFile );

            ioUtil.deleteFile( finalJar );

            ioUtil.renameTo( unprocessedJarFile, finalJar );
        }
        return jarFiles.length;
    }

    /**
     * @param directory location of directory where to update manifest entries jars
     * @throws MojoExecutionException if can not update manifest entries jars
     */
    private void updateManifestEntries( File directory )
            throws MojoExecutionException
    {

        File[] jarFiles = directory.listFiles( unprocessedJarFileFilter );

        getLog().info( "-- Update manifest entries" );
        getLog().debug( "updateManifestEntries in " + directory + " found " + jarFiles.length + " jar(s) to treat" );

        if ( jarFiles.length == 0 )
        {
            return;
        }

        for ( File unprocessedJarFile : jarFiles )
        {
            verboseLog( "Update manifest " + toProcessFile( unprocessedJarFile ).getName() );

            jarUtil.updateManifestEntries( unprocessedJarFile, updateManifestEntries );
        }
    }

    /**
     * @param directory location of directory where to sign jars
     * @return the number of signed jars
     * @throws MojoExecutionException if can not sign jars
     */
    private int signJars( File directory )
            throws MojoExecutionException
    {

        File[] jarFiles = directory.listFiles( unprocessedJarFileFilter );

        getLog().info( "-- Sign jars" );
        getLog().debug( "signJars in " + directory + " found " + jarFiles.length + " jar(s) to sign" );

        if ( jarFiles.length == 0 )
        {
        	getLog().info( "No jar files to sign available." );
            return 0;
        }

        final boolean signVerify = sign.isVerify();

        ExecutorService ex = Executors.newFixedThreadPool( sign.getParallel() );

        HashMap<File, Future<?>> signRequests = new HashMap<>();
        for ( final File unprocessedJarFile : jarFiles )
        {
            signRequests.put( unprocessedJarFile, ex.submit( new Callable<Object>()
            {
                public Object call()
                        throws Exception
                {
                    File signedJar = toProcessFile( unprocessedJarFile );
                    ioUtil.deleteFile( signedJar );

                    signJar( unprocessedJarFile, signedJar, signVerify );
                    return null;
                }
            } ) );
        }

        while ( !signRequests.isEmpty() )
        {
            for ( final File file : new ArrayList<>( signRequests.keySet() ) )
            {
                if ( signRequests.get( file ).isDone() )
                {
                    try
                    {
                        signRequests.get( file ).get();
                    }
                    catch ( ExecutionException ee )
                    {
                        throw new MojoExecutionException(
                                "Error while signing resource " + file.toString(), ee.getCause() );
                    }
                    catch ( InterruptedException e )
                    {
                        e.printStackTrace();
                    }
                    signRequests.remove( file );
                }
            }
        }

        ex.shutdown();

        return jarFiles.length;
    }

    protected void signJar( File fileToSign, File signedJar, boolean signVerify ) throws MojoExecutionException
    {

        verboseLog( "Sign " + signedJar.getName() );
        
        if ( sign != null )
        {
	        signTool.sign( sign, fileToSign, signedJar );
	
	        getLog().debug(
	                "lastModified signedJar:" + signedJar.lastModified() + " unprocessed signed Jar:" +
	                        fileToSign.lastModified() );
	
	        if ( signVerify )
	        {
	            verboseLog( "Verify signature of " + signedJar.getName() );
	            signTool.verify( sign, signedJar, isVerbose() );
	        }
        }
        else 
        {
        	getLog().error("No sign configuration available! Jar is not signed: " +signedJar.getName());
        }

        // remove unprocessed files
        // TODO wouldn't have to do that if we copied the
        // unprocessed jar files in a temporary area
        ioUtil.deleteFile( fileToSign );
    }

    /**
     * Removes the signature of the files in the specified directory which satisfy the
     * specified filter.
     *
     * @param workDirectory working directory used to unsign jars
     * @return the number of unsigned jars
     * @throws MojoExecutionException if could not remove signatures
     */
    private int removeExistingSignatures( File workDirectory )
            throws MojoExecutionException
    {
        getLog().info( "-- Remove existing signatures" );

        // cleanup tempDir if exists
        File tempDir = new File( workDirectory, "temp_extracted_jars" );
        ioUtil.removeDirectory( tempDir );

        // recreate temp dir
        ioUtil.makeDirectoryIfNecessary( tempDir );

        // process jars
        File[] jarFiles = workDirectory.listFiles( unprocessedJarFileFilter );

        for ( File jarFile : jarFiles )
        {
            if ( isJarSigned( jarFile ) )
            {
                if ( !canUnsign )
                {
                    throw new MojoExecutionException(
                            "neverUnsignAlreadySignedJar is set to true and a jar file [" + jarFile +
                                    " was asked to be unsign,\n please prefer use in this case an extension for " +
                                    "signed jars or not set to true the neverUnsignAlreadySignedJar parameter, Make " +
                                    "your choice:)" );
                }
                verboseLog( "Remove signature " + toProcessFile( jarFile ).getName() );

                signTool.unsign( jarFile, isVerbose() );
            }
            else
            {
                verboseLog( "Skip not signed " + toProcessFile( jarFile ).getName() );
            }
        }

        // cleanup tempDir
        ioUtil.removeDirectory( tempDir );

        return jarFiles.length; // FIXME this is wrong. Not all jars are signed.
    }

    private ClassLoader getCompileClassLoader()
            throws MalformedURLException
    {
        URL[] urls = new URL[compileClassPath.size()];
        for ( int i = 0; i < urls.length; i++ )
        {
            String spec = compileClassPath.get( i ).toString();
            URL url = new File( spec ).toURI().toURL();
            urls[i] = url;
        }
        return new URLClassLoader( urls );
    }

    File toUnprocessFile( File targetDirectory, String sourceName )
    {
        if ( sourceName.startsWith( UNPROCESSED_PREFIX ) )
        {
            throw new IllegalStateException( sourceName + " does start with " + UNPROCESSED_PREFIX );
        }
        String targetFilename = UNPROCESSED_PREFIX + sourceName;
        return new File( targetDirectory, targetFilename );
    }

    private File toProcessFile( File source )
    {
        if ( !source.getName().startsWith( UNPROCESSED_PREFIX ) )
        {
            throw new IllegalStateException( source.getName() + " does not start with " + UNPROCESSED_PREFIX );
        }
        String targetFilename = source.getName().substring( UNPROCESSED_PREFIX.length() );
        return new File( source.getParentFile(), targetFilename );
    }

}
