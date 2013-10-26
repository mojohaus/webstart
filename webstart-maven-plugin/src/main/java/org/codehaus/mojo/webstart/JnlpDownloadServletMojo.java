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

import org.apache.commons.lang.StringUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.artifact.resolver.filter.ScopeArtifactFilter;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.codehaus.mojo.webstart.generator.JarResourcesGenerator;
import org.codehaus.mojo.webstart.generator.SimpleGeneratorExtraConfig;
import org.codehaus.mojo.webstart.generator.VersionXmlGenerator;
import org.codehaus.mojo.webstart.util.IOUtil;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * This MOJO is tailored for use within a Maven web application project that uses
 * the JnlpDownloadServlet to serve up the JNLP application.
 *
 * @author Kevin Stembridge
 * @version $Id$
 * @since 1.0-alpha-2
 */
@Mojo( name = "jnlp-download-servlet", requiresProject = true, inheritByDefault = true,
       requiresDependencyResolution = ResolutionScope.RUNTIME )
public class JnlpDownloadServletMojo
    extends AbstractBaseJnlpMojo
{

    // ----------------------------------------------------------------------
    // Mojo Parameters
    // ----------------------------------------------------------------------

    /**
     * The name of the directory into which the jnlp file and other
     * artifacts will be stored after processing. This directory will be created
     * directly within the root of the WAR produced by the enclosing project.
     */
    @Parameter( defaultValue = "webstart" )
    private String outputDirectoryName;

    /**
     * The collection of JnlpFile configuration elements. Each one represents a
     * JNLP file that is to be generated and deployed within the enclosing
     * project's WAR artifact. At least one JnlpFile must be specified.
     */
    @Parameter( required = true )
    private List<JnlpFile> jnlpFiles;

    /**
     * The configurable collection of jars that are common to all jnlpFile elements declared in
     * plugin configuration. These jars will be output as jar elements in the resources section of
     * every generated JNLP file and bundled into the specified output directory of the artifact
     * produced by the project.
     */
    @Parameter
    private List<JarResource> commonJarResources;

    /**
     */
    @Parameter( defaultValue = "${reactorProjects}", required = true, readonly = true )
    private List<MavenProject> reactorProjects;

    // ----------------------------------------------------------------------
    // Components
    // ----------------------------------------------------------------------

    /**
     * Maven project.
     */
    @Component
    private MavenProject project;

    /**
     * The project's artifact metadata source, used to resolve transitive dependencies.
     */
    @Component
    private ArtifactMetadataSource artifactMetadataSource;

    // ----------------------------------------------------------------------
    // Mojo Implementation
    // ----------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    public void execute()
        throws MojoExecutionException, MojoFailureException
    {

        checkConfiguration();

        IOUtil ioUtil = getIoUtil();

        ioUtil.copyResources( getResourcesDirectory(), getWorkDirectory() );

        if ( this.commonJarResources != null )
        {
            retrieveJarResources( this.commonJarResources );
        }

        for ( JnlpFile jnlpFile : this.jnlpFiles )
        {
            retrieveJarResources( jnlpFile.getJarResources() );
        }

        signOrRenameJars();
        packJars();

        for ( JnlpFile jnlpFile : this.jnlpFiles )
        {
            generateJnlpFile( jnlpFile, getLibPath() );
        }

        generateVersionXml();
//        copyWorkingDirToOutputDir();

        File outputDir = new File( getProject().getBuild().getDirectory(),
                                   getProject().getBuild().getFinalName() + File.separator + this.outputDirectoryName );

        ioUtil.makeDirectoryIfNecessary( outputDir );

        ioUtil.copyDirectoryStructure( getWorkDirectory(), outputDir );
    }

    // ----------------------------------------------------------------------
    // AbstractBaseJnlpMojo implementatio
    // ----------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    public MavenProject getProject()
    {
        return this.project;
    }

    // ----------------------------------------------------------------------
    // Private methods
    // ----------------------------------------------------------------------

    /**
     * Confirms that all plugin configuration provided by the user
     * in the pom.xml file is valid.
     *
     * @throws MojoExecutionException if any user configuration is invalid.
     */
    private void checkConfiguration()
        throws MojoExecutionException
    {

        if ( this.jnlpFiles.isEmpty() )
        {
            throw new MojoExecutionException(
                "Configuration error: At least one <jnlpFile> element must be specified" );
        }

        if ( this.jnlpFiles.size() == 1 && StringUtils.isEmpty( this.jnlpFiles.get( 0 ).getOutputFilename() ) )
        {
            getLog().debug( "Jnlp output file name not specified in single set of jnlpFiles. " +
                                "Using default output file name: launch.jnlp." );
            this.jnlpFiles.get( 0 ).setOutputFilename( "launch.jnlp" );
        }

        for ( JnlpFile jnlpFile : this.jnlpFiles )
        {
            checkJnlpFileConfiguration( jnlpFile );
        }

        checkForDuplicateJarResources();
        checkCommonJarResources();
        checkForUniqueJnlpFilenames();
        checkPack200();

    }

    /**
     * Checks the validity of a single jnlpFile configuration element.
     *
     * @param jnlpFile The configuration element to be checked.
     * @throws MojoExecutionException if the config element is invalid.
     */
    private void checkJnlpFileConfiguration( JnlpFile jnlpFile )
        throws MojoExecutionException
    {

        if ( StringUtils.isBlank( jnlpFile.getOutputFilename() ) )
        {
            throw new MojoExecutionException(
                "Configuration error: An outputFilename must be specified for each jnlpFile element" );
        }

        if ( StringUtils.isBlank( jnlpFile.getTemplateFilename() ) )
        {
            getLog().info(
                "No templateFilename found for " + jnlpFile.getOutputFilename() + ". Will use the default template." );
        }
        else
        {
            File templateFile = new File( getTemplateDirectory(), jnlpFile.getTemplateFilename() );

            if ( !templateFile.isFile() )
            {
                throw new MojoExecutionException(
                    "The specified JNLP template does not exist: [" + templateFile + "]" );
            }
        }

        checkJnlpJarResources( jnlpFile );

    }

    /**
     * Checks the collection of jarResources configured for a given jnlpFile element.
     *
     * @param jnlpFile The configuration element whose jarResources are to be checked.
     * @throws MojoExecutionException if any config is invalid.
     */
    private void checkJnlpJarResources( JnlpFile jnlpFile )
        throws MojoExecutionException
    {

        List<JarResource> jnlpJarResources = jnlpFile.getJarResources();

        if ( jnlpJarResources == null || jnlpJarResources.isEmpty() )
        {
            throw new MojoExecutionException(
                "Configuration error: A non-empty <jarResources> element must be specified in the plugin " +
                    "configuration for the JNLP file named [" + jnlpFile.getOutputFilename() + "]" );
        }

        List<JarResource> jarsWithMainClass = new ArrayList<JarResource>();

        for ( JarResource jarResource : jnlpJarResources )
        {
            checkMandatoryJarResourceFields( jarResource );

            if ( jarResource.getMainClass() != null )
            {
                jnlpFile.setMainClass( jarResource.getMainClass() );
                jarsWithMainClass.add( jarResource );
            }
        }

        if ( jarsWithMainClass.isEmpty() )
        {
            throw new MojoExecutionException( "Configuration error: Exactly one <jarResource> element must " +
                                                  "be declared with a <mainClass> element in the configuration for JNLP file [" +
                                                  jnlpFile.getOutputFilename() + "]" );
        }

        if ( jarsWithMainClass.size() > 1 )
        {
            throw new MojoExecutionException(
                "Configuration error: More than one <jarResource> element has been declared " +
                    "with a <mainClass> element in the configuration for JNLP file [" + jnlpFile.getOutputFilename() +
                    "]" );
        }

    }

    /**
     * Checks that any jarResources defined in the jnlpFile elements are not also defined in
     * commonJarResources.
     *
     * @throws MojoExecutionException if a duplicate is found.
     */
    private void checkForDuplicateJarResources()
        throws MojoExecutionException
    {

        if ( this.commonJarResources == null || this.commonJarResources.isEmpty() )
        {
            return;
        }

        for ( JnlpFile jnlpFile : this.jnlpFiles )
        {

            List<JarResource> jnlpJarResources = jnlpFile.getJarResources();

            for ( JarResource jarResource : jnlpJarResources )
            {
                if ( this.commonJarResources.contains( jarResource ) )
                {
                    String message = "Configuration Error: The jar resource element for artifact " + jarResource +
                        " defined in common jar resources is duplicated in the jar " +
                        "resources configuration of the jnlp file identified by the template file " +
                        jnlpFile.getTemplateFilename() + ".";

                    throw new MojoExecutionException( message );

                }

            }

        }

    }

    /**
     * Checks the configuration of common jar resources. Specifying common jar
     * resources is optional but if present, each jar resource must have the
     * same mandatory fields as jar resources configured directly within a
     * jnlpFile element, but it must not have a configured mainClass element.
     *
     * @throws MojoExecutionException if the config is invalid.
     */
    private void checkCommonJarResources()
        throws MojoExecutionException
    {

        if ( this.commonJarResources == null )
        {
            return;
        }

        for ( JarResource jarResource : this.commonJarResources )
        {
            checkMandatoryJarResourceFields( jarResource );

            if ( jarResource.getMainClass() != null )
            {
                throw new MojoExecutionException( "Configuration Error: A mainClass must not be specified " +
                                                      "on a JarResource in the commonJarResources collection." );
            }

        }

    }

    /**
     * Checks mandatory files of the given jar resource (says groupId, artificatId or version).
     *
     * @param jarResource jar resource to check
     * @throws MojoExecutionException if one of the mandatory field is missing
     */
    private void checkMandatoryJarResourceFields( JarResource jarResource )
        throws MojoExecutionException
    {

        if ( StringUtils.isEmpty( jarResource.getGroupId() ) || StringUtils.isEmpty( jarResource.getArtifactId() ) ||
            StringUtils.isEmpty( jarResource.getVersion() ) )
        {
            throw new MojoExecutionException(
                "Configuration error: groupId, artifactId or version missing for jarResource[" + jarResource + "]." );
        }

    }

    /**
     * Confirms that each jnlpFile element is configured with a unique JNLP file name.
     *
     * @throws MojoExecutionException if the config is invalid.
     */
    private void checkForUniqueJnlpFilenames()
        throws MojoExecutionException
    {
        Set<String> filenames = new HashSet<String>( this.jnlpFiles.size() );

        for ( JnlpFile jnlpFile : this.jnlpFiles )
        {
            if ( !filenames.add( jnlpFile.getOutputFilename() ) )
            {
                throw new MojoExecutionException( "Configuration error: Unique JNLP filenames must be provided. " +
                                                      "The following file name appears more than once [" +
                                                      jnlpFile.getOutputFilename() + "]." );
            }

        }

    }

    /**
     * Resolve the artifacts represented by the given collection of JarResources and
     * copy them to the working directory if a newer copy of the file doesn't already
     * exist there. Transitive dependencies will also be retrieved.
     * <p/>
     * Transitive dependencies are added to the list specified as parameter. TODO fix that.
     *
     * @param jarResources list of jar resources to retrieve
     * @throws MojoExecutionException if something bas occurs while retrieving resources
     */
    private void retrieveJarResources( List<JarResource> jarResources )
        throws MojoExecutionException
    {

        Set<JarResource> extraJarResources = new HashSet<JarResource>();
//        Set jarResourceArtifacts = new HashSet();

        try
        {
            //for each configured JarResource, create and resolve the corresponding artifact and
            //check it for the mainClass if specified
            for ( JarResource jarResource : jarResources )
            {
                Artifact artifact = createArtifact( jarResource );
//                getArtifactResolver().resolve( artifact, getRemoteRepositories(), getLocalRepository() );
                jarResource.setArtifact( artifact );
                extraJarResources.addAll( retrieveAdditionalJarResources( jarResource ) );

                checkForMainClass( jarResource );
//                jarResourceArtifacts.add( artifact );
            }

//            if ( !isExcludeTransitive() )
//            {
//
//                retrieveTransitiveDependencies( jarResourceArtifacts, jarResources );
//
//            }
            jarResources.addAll( extraJarResources );

            //for each JarResource, copy its artifact to the lib directory if necessary
            for ( JarResource jarResource : jarResources )
            {
                Artifact artifact = jarResource.getArtifact();
                boolean copied = copyJarAsUnprocessedToDirectoryIfNecessary( artifact.getFile(), getLibDirectory() );

                if ( copied )
                {
                    String name = artifact.getFile().getName();
                    if ( getLog().isDebugEnabled() )
                    {
                        getLog().debug( "Adding " + name + " to modifiedJnlpArtifacts list." );
                    }
                    verboseLog( "Adding " + name + " to modifiedJnlpArtifacts list." );
                    getModifiedJnlpArtifacts().add( name.substring( 0, name.lastIndexOf( '.' ) ) );
                }

                if ( jarResource.isOutputJarVersion() )
                {
                    // Create and set a version-less href for this jarResource
                    String hrefValue = buildHrefValue( artifact );
                    jarResource.setHrefValue( hrefValue );
                }
            }

        }
        catch ( ArtifactResolutionException e )
        {
            throw new MojoExecutionException( "Unable to resolve an artifact", e );
        }
        catch ( ArtifactNotFoundException e )
        {
            throw new MojoExecutionException( "Unable to find an artifact", e );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Unable to copy an artifact to the working directory", e );
        }

    }

    /**
     * Creates from the given jar resource the underlying artifact.
     *
     * @param jarResource the jar resource
     * @return the created artifact from the given jar resource
     */
    private Artifact createArtifact( JarResource jarResource )
    {

        if ( jarResource.getClassifier() == null )
        {
            return getArtifactFactory().createArtifact( jarResource.getGroupId(), jarResource.getArtifactId(),
                                                        jarResource.getVersion(), Artifact.SCOPE_RUNTIME, "jar" );
        }
        else
        {
            return getArtifactFactory().createArtifactWithClassifier( jarResource.getGroupId(),
                                                                      jarResource.getArtifactId(),
                                                                      jarResource.getVersion(), "jar",
                                                                      jarResource.getClassifier() );
        }

    }

    /**
     * If the given jarResource is configured with a main class, the underlying artifact
     * is checked to see if it actually contains the specified class.
     *
     * @param jarResource the jar resources to test
     * @throws IllegalStateException  if the jarResource's underlying artifact has not yet been resolved.
     * @throws MojoExecutionException if could not chek that the jar resource with a main class has really it
     */
    private void checkForMainClass( JarResource jarResource )
        throws MojoExecutionException
    {

        String mainClass = jarResource.getMainClass();

        if ( mainClass == null )
        {
            return;
        }

        Artifact artifact = jarResource.getArtifact();

        if ( artifact == null )
        {
            throw new IllegalStateException( "Implementation Error: The given jarResource cannot be checked for " +
                                                 "a main class until the underlying artifact has been resolved: [" +
                                                 jarResource + "]" );
        }

        try
        {
            boolean containsMainClass = getArtifactUtil().artifactContainsMainClass( artifact, jarResource );
            if ( !containsMainClass )
            {
                throw new MojoExecutionException(
                    "The jar specified by the following jarResource does not contain the declared main class:" +
                        jarResource );
            }
        }
        catch ( MalformedURLException e )
        {
            throw new MojoExecutionException( "Attempting to find main class [" + mainClass + "] in [" + artifact + "]",
                                              e );
        }

    }

    private Set<JarResource> retrieveAdditionalJarResources( JarResource jarResource )
        throws ArtifactResolutionException, ArtifactNotFoundException
    {
        Set<JarResource> result = new HashSet<JarResource>();

        Artifact artifact = jarResource.getArtifact();

        for ( MavenProject mp : reactorProjects )
        {
            Artifact resolvedArtifact = null;
            if ( mp.getArtifact().equals( artifact ) )
            {

                // artifact is exactly a sibling project
                resolvedArtifact = mp.getArtifact();
            }
            else
            {
                // search in attached artifacts of the sibling project
                for ( Object o : mp.getAttachedArtifacts() )
                {
                    Artifact candidate = (Artifact) o;
                    if ( candidate.equals( artifact ) )
                    {
                        resolvedArtifact = mp.getArtifact();
                        break;
                    }
                }
            }
            if ( resolvedArtifact != null )
            {

                verboseLog( "Jar resource " + jarResource + " found from reactor " + mp );
                // artifact found from this sibling project
                jarResource.setArtifact( resolvedArtifact );

                if ( !isExcludeTransitive() )
                {
                    for ( Object o : mp.getArtifacts() )
                    {
                        Artifact artifact1 = (Artifact) o;
                        if ( !"pom".equals( artifact1.getType() ) )
                        {
                            JarResource resource = new JarResource( artifact1 );
                            resource.setOutputJarVersion( true );
                            result.add( resource );
                        }
                    }
                }
                return result;
            }

        }

        // Artifact isn't present in reactor so we resolve it from repo.

        getArtifactResolver().resolve( artifact, getRemoteRepositories(), getLocalRepository() );

        if ( !isExcludeTransitive() )
        {
            // Artifact isn't present in reactor so we resolve it from repo.
            ArtifactResolutionResult arr =
                getArtifactResolver().resolveTransitively( Collections.singleton( artifact ), artifact, null,
                                                           getLocalRepository(), getRemoteRepositories(),
                                                           this.artifactMetadataSource,
                                                           new ScopeArtifactFilter( Artifact.SCOPE_RUNTIME ) );
            //for each transitive dependency, wrap it in a JarResource and add it to the collection of
            //existing jar resources
            for ( Object o : arr.getArtifactResolutionNodes() )
            {
                JarResource newJarResource = new JarResource( (Artifact) o );
                newJarResource.setOutputJarVersion( true );
                result.add( newJarResource );
            }
        }
        return result;
    }

    private void generateJnlpFile( JnlpFile jnlpFile, String libPath )
        throws MojoExecutionException
    {

        File jnlpOutputFile = new File( getWorkDirectory(), jnlpFile.getOutputFilename() );

        Set<JarResource> jarResources = new LinkedHashSet<JarResource>();
        jarResources.addAll( jnlpFile.getJarResources() );

        if ( this.commonJarResources != null && !this.commonJarResources.isEmpty() )
        {

            for ( JarResource jarResource : this.commonJarResources )
            {
                jarResources.add( jarResource );
            }

            jarResources.addAll( this.commonJarResources );
        }

        JarResourcesGenerator jnlpGenerator = new JarResourcesGenerator( getLog(), getProject(), getTemplateDirectory(),
                                                                         "default-jnlp-servlet-template.vm",
                                                                         jnlpOutputFile, jnlpFile.getTemplateFilename(),
                                                                         jarResources, jnlpFile.getMainClass(),
                                                                         getWebstartJarURLForVelocity(), libPath,
                                                                         getEncoding() );

        jnlpGenerator.setExtraConfig( new SimpleGeneratorExtraConfig( getCodebase() ) );

        try
        {
            jnlpGenerator.generate();
        }
        catch ( Exception e )
        {
            throw new MojoExecutionException(
                "The following error occurred attempting to generate " + "the JNLP deployment descriptor: " + e, e );
        }

    }

    /**
     * Generates a version.xml file for all the jarResources configured either in jnlpFile elements
     * or in the commonJarResources element.
     *
     * @throws MojoExecutionException if could not generate the xml version file
     */
    private void generateVersionXml()
        throws MojoExecutionException
    {

        Set<JarResource> jarResources = new LinkedHashSet<JarResource>();

        //combine the jar resources from commonJarResources and each JnlpFile config

        for ( JnlpFile jnlpFile : this.jnlpFiles )
        {
            jarResources.addAll( jnlpFile.getJarResources() );
        }

        if ( this.commonJarResources != null )
        {
            jarResources.addAll( this.commonJarResources );
        }

        VersionXmlGenerator generator = new VersionXmlGenerator( getEncoding() );
        generator.generate( getLibDirectory(), jarResources );

    }

    /**
     * Builds the string to be entered in the href attribute of the jar
     * resource element in the generated JNLP file. This will be equal
     * to the artifact file name with the version number stripped out.
     *
     * @param artifact The underlying artifact of the jar resource.
     * @return The href string for the given artifact, never null.
     */
    private String buildHrefValue( Artifact artifact )
    {
        StringBuilder sbuf = new StringBuilder();
        sbuf.append( artifact.getArtifactId() );

        if ( StringUtils.isNotEmpty( artifact.getClassifier() ) )
        {
            sbuf.append( "-" ).append( artifact.getClassifier() );
        }

        sbuf.append( "." ).append( artifact.getArtifactHandler().getExtension() );

        return sbuf.toString();

    }
}
