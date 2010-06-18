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
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.artifact.resolver.filter.ScopeArtifactFilter;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.codehaus.mojo.webstart.generator.GeneratorExtraConfig;
import org.codehaus.mojo.webstart.generator.JarResourcesGenerator;
import org.codehaus.mojo.webstart.generator.VersionXmlGenerator;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;

/**
 * This MOJO is tailored for use within a Maven web application project that uses 
 * the JnlpDownloadServlet to serve up the JNLP application.
 * 
 * @author Kevin Stembridge
 * @since 1.0-alpha-2
 * @version $Id$
 * @goal jnlp-download-servlet
 * @requiresDependencyResolution runtime
 * @requiresProject
 * @inheritedByDefault true
 */
public class JnlpDownloadServletMojo extends AbstractBaseJnlpMojo
{
    
    /**
     * @parameter default-value="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    /**
     * The project's artifact metadata source, used to resolve transitive dependencies.
     * @component
     * @required
     * @readonly
     */
    private ArtifactMetadataSource artifactMetadataSource;
    
    /**
     * The name of the directory into which the jnlp file and other 
     * artifacts will be stored after processing. This directory will be created 
     * directly within the root of the WAR produced by the enclosing project.
     * 
     * @parameter default-value="webstart"
     */
    private String outputDirectoryName;
    
    /**
     * The collection of JnlpFile configuration elements. Each one represents a
     * JNLP file that is to be generated and deployed within the enclosing 
     * project's WAR artifact. At least one JnlpFile must be specified.
     * 
     * @parameter 
     * @required
     */
    private List/*JnlpFile*/ jnlpFiles;

    /**
     * The configurable collection of jars that are common to all jnlpFile elements declared in 
     * plugin configuration. These jars will be output as jar elements in the resources section of 
     * every generated JNLP file and bundled into the specified output directory of the artifact 
     * produced by the project.
     * 
     * @parameter
     */
    private List/*JarResource*/ commonJarResources;

    /**
     * Creates a new uninitialized {@code JnlpDownloadServletMojo}.
     */
    public JnlpDownloadServletMojo()
    {
        // do nothing
    }

    /**
     * {@inheritDoc}
     */
    public void execute() throws MojoExecutionException, MojoFailureException
    {
        
        checkConfiguration();
        
        try
        {
            copyResources( getResourcesDirectory(), getWorkDirectory() );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "An error occurred attempting to copy " 
                                             + "resources to the working directory.", e );
        }
        
        if ( this.commonJarResources != null )
        {
            retrieveJarResources( this.commonJarResources );
        }
        
        for ( Iterator itr = this.jnlpFiles.iterator(); itr.hasNext(); )
        {
            JnlpFile jnlpFile = ( JnlpFile ) itr.next();
            retrieveJarResources( jnlpFile.getJarResources() );
        }
        
        signOrRenameJars();
        packJars();
        
        for ( Iterator itr = this.jnlpFiles.iterator(); itr.hasNext(); )
        {
            generateJnlpFile( ( JnlpFile ) itr.next(), getLibPath() );
        }
        
        generateVersionXml();
        copyWorkingDirToOutputDir();
        
    }
    
    /**
     * Confirms that all plugin configuration provided by the user 
     * in the pom.xml file is valid.
     *
     * @throws MojoExecutionException if any user configuration is invalid. 
     */
    private void checkConfiguration() throws MojoExecutionException 
    {
        
        if ( this.jnlpFiles.isEmpty() )
        {
            throw new MojoExecutionException( 
                    "Configuration error: At least one <jnlpFile> element must be specified" );
        }

        if ( this.jnlpFiles.size() == 1 
                        && StringUtils.isEmpty( ( ( JnlpFile ) this.jnlpFiles.get( 0 ) ).getOutputFilename() ) )
        {
            getLog().debug( "Jnlp output file name not specified in single set of jnlpFiles. " 
                            + "Using default output file name: launch.jnlp." );
            ( ( JnlpFile ) this.jnlpFiles.get( 0 ) ).setOutputFilename( "launch.jnlp" );
        }
         
        for ( Iterator itr = this.jnlpFiles.iterator(); itr.hasNext(); ) 
        {
            checkJnlpFileConfiguration( ( JnlpFile ) itr.next() );
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
    private void checkJnlpFileConfiguration( JnlpFile jnlpFile ) throws MojoExecutionException 
    {
        
        if ( StringUtils.isEmpty( jnlpFile.getOutputFilename() ) )
        {
            throw new MojoExecutionException( 
                    "Configuration error: An outputFilename must be specified for each jnlpFile element" );
        }
        
        if ( jnlpFile.getTemplateFilename() == null )
        {
            getLog().info( "No templateFilename found for " + jnlpFile.getOutputFilename()
                          + ". Will use the default template." );
        }
        else
        {        
          File templateFile = new File( getTemplateDirectory(), jnlpFile.getTemplateFilename() );
        
          if ( !templateFile.isFile() )
          {
              throw new MojoExecutionException( "The specified JNLP template does not exist: [" + templateFile + "]" );
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
    private void checkJnlpJarResources( JnlpFile jnlpFile ) throws MojoExecutionException
    {
        
        List jnlpJarResources = jnlpFile.getJarResources();
        
        if ( jnlpJarResources == null || jnlpJarResources.isEmpty() )
        {
            throw new MojoExecutionException(
                    "Configuration error: A non-empty <jarResources> element must be specified in the plugin "
                    + "configuration for the JNLP file named [" + jnlpFile.getOutputFilename() + "]" );
        }

        Iterator itr = jnlpJarResources.iterator();
        List/*JarResource*/ jarsWithMainClass = new ArrayList();

        while ( itr.hasNext() )
        {
            JarResource jarResource = (JarResource) itr.next();
            
            checkMandatoryJarResourceFields( jarResource );
            
            if ( jarResource.getMainClass() != null )
            {
                jnlpFile.setMainClass( jarResource.getMainClass() );
                jarsWithMainClass.add( jarResource );
            }

        }

        if ( jarsWithMainClass.isEmpty() )
        {
            throw new MojoExecutionException(
                    "Configuration error: Exactly one <jarResource> element must "
                    + "be declared with a <mainClass> element in the configuration for JNLP file ["
                    + jnlpFile.getOutputFilename() + "]" );
        }

        if ( jarsWithMainClass.size() > 1 )
        {
            throw new MojoExecutionException(
                    "Configuration error: More than one <jarResource> element has been declared "
                    + "with a <mainClass> element in the configuration for JNLP file ["
                    + jnlpFile.getOutputFilename() + "]" );
        }

    }
    
    /**
     * Checks that any jarResources defined in the jnlpFile elements are not also defined in 
     * commonJarResources.
     * @throws MojoExecutionException if a duplicate is found. 
     */
    private void checkForDuplicateJarResources() throws MojoExecutionException 
    {
        
        if ( this.commonJarResources == null || this.commonJarResources.isEmpty() )
        {
            return;
        }
        
        for ( Iterator jnlpFileItr = this.jnlpFiles.iterator(); jnlpFileItr.hasNext(); ) 
        {
            
            JnlpFile jnlpFile = (JnlpFile) jnlpFileItr.next();
            
            List jnlpJarResources = jnlpFile.getJarResources();
            
            for ( Iterator jarResourceItr = jnlpJarResources.iterator(); jarResourceItr.hasNext(); )
            {
                JarResource jarResource = (JarResource) jarResourceItr.next();
                
                if ( this.commonJarResources.contains( jarResource ) )
                {
                    String message = "Configuration Error: The jar resource element for artifact "
                                     + jarResource
                                     + " defined in common jar resources is duplicated in the jar "
                                     + "resources configuration of the jnlp file identified by the template file "
                                     + jnlpFile.getTemplateFilename()
                                     + ".";
       
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
    private void checkCommonJarResources( ) throws MojoExecutionException 
    {
        
        if ( this.commonJarResources == null )
        {
            return;
        }
        
        for ( Iterator itr = this.commonJarResources.iterator(); itr.hasNext(); )
        {
            JarResource jarResource = (JarResource) itr.next();
            checkMandatoryJarResourceFields( jarResource );
            
            if ( jarResource.getMainClass() != null )
            {
                throw new MojoExecutionException( "Configuration Error: A mainClass must not be specified "
                                                 + "on a JarResource in the commonJarResources collection." );
            }
            
        }
        
    }

    private void checkMandatoryJarResourceFields( JarResource jarResource ) throws MojoExecutionException
    {

        if ( StringUtils.isEmpty( jarResource.getGroupId() )
             || StringUtils.isEmpty( jarResource.getArtifactId() )
             || StringUtils.isEmpty( jarResource.getVersion() ) ) 
        {
            throw new MojoExecutionException(
                    "Configuration error: groupId, artifactId or version missing for jarResource["
                    + jarResource + "]." );
        }

    }
    
    /**
     * Confirms that each jnlpFile element is configured with a unique JNLP file name.
     *
     * @throws MojoExecutionException
     */
    private void checkForUniqueJnlpFilenames() throws MojoExecutionException
    {
        Set filenames = new HashSet( this.jnlpFiles.size() );
        
        for ( Iterator itr = this.jnlpFiles.iterator(); itr.hasNext(); ) 
        {
            JnlpFile jnlpFile = (JnlpFile) itr.next();
            
            if ( !filenames.add( jnlpFile.getOutputFilename() ) ) 
            {
                throw new MojoExecutionException( "Configuration error: Unique JNLP filenames must be provided. "
                                                  + "The following file name appears more than once [" 
                                                  + jnlpFile.getOutputFilename() + "]." );
            }
            
        }
        
    }

    /**
     * Resolve the artifacts represented by the given collection of JarResources and 
     * copy them to the working directory if a newer copy of the file doesn't already 
     * exist there. Transitive dependencies will also be retrieved.
     * <p> 
     * Transitive dependencies are added to the list specified as parameter. TODO fix that.
     * 
     * @throws MojoExecutionException
     */
    private void retrieveJarResources( List jarResources ) throws MojoExecutionException
    {
        
        Set jarResourceArtifacts = new HashSet();
        
        try
        {
            //for each configured JarResource, create and resolve the corresponding artifact and 
            //check it for the mainClass if specified
            for (  Iterator itr = jarResources.iterator(); itr.hasNext(); )
            {   
                JarResource jarResource = ( JarResource ) itr.next();
                Artifact artifact = createArtifact( jarResource );
                getArtifactResolver().resolve( artifact, getRemoteRepositories(), getLocalRepository() );
                jarResource.setArtifact( artifact );
                checkForMainClass( jarResource );
                jarResourceArtifacts.add( artifact );
            }
            
            if ( !isExcludeTransitive() )
            {
                
                retrieveTransitiveDependencies( jarResourceArtifacts , jarResources );
                
            }
            
            //for each JarResource, copy its artifact to the lib directory if necessary
            for ( Iterator itr = jarResources.iterator(); itr.hasNext(); )
            {
                JarResource jarResource = ( JarResource ) itr.next();
                Artifact artifact = jarResource.getArtifact();
                boolean copied = copyJarAsUnprocessedToDirectoryIfNecessary( artifact.getFile(), getLibDirectory() );
                
                if ( copied )
                {
                    String name = artifact.getFile().getName();
                    if ( getLog().isDebugEnabled() )
                    {
                        getLog().debug( "Adding " + name + " to modifiedJnlpArtifacts list." );
                    }
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
    
    private Artifact createArtifact( JarResource jarResource ) 
    {

        if ( jarResource.getClassifier() == null )
        {
            return getArtifactFactory().createArtifact( jarResource.getGroupId(), 
                                                        jarResource.getArtifactId(), 
                                                        jarResource.getVersion(), 
                                                        Artifact.SCOPE_RUNTIME,
                                                        "jar" );
        } 
        else 
        {
            return getArtifactFactory().createArtifactWithClassifier( jarResource.getGroupId(), 
                                                                      jarResource.getArtifactId(),
                                                                      jarResource.getVersion(), 
                                                                      "jar",
                                                                      jarResource.getClassifier() );
        }
                
    }
    
    /**
     * If the given jarResource is configured with a main class, the underlying artifact 
     * is checked to see if it actually contains the specified class.
     *
     * @param jarResource
     * @throws IllegalStateException if the jarResource's underlying artifact has not yet been resolved.
     * @throws MojoExecutionException
     */
    private void checkForMainClass( JarResource jarResource ) throws MojoExecutionException 
    {
        
        String mainClass = jarResource.getMainClass();
        
        if ( mainClass == null )
        {
            return;
        }
        
        Artifact artifact = jarResource.getArtifact();
        
        if ( artifact == null )
        {
            throw new IllegalStateException( "Implementation Error: The given jarResource cannot be checked for "
                                             + "a main class until the underlying artifact has been resolved: ["
                                             + jarResource
                                             + "]" );
        }
        
        try
        {
            if ( !artifactContainsClass( artifact, mainClass ) )
            {
                throw new MojoExecutionException(
                        "The jar specified by the following jarResource does not contain the declared main class:"
                        + jarResource );
            }
        }
        catch ( MalformedURLException e )
        {
            throw new MojoExecutionException( "Attempting to find main class ["
                                             + mainClass
                                             + "] in ["
                                             + artifact
                                             + "]",
                                             e );
        }
            
    }
    
    private void retrieveTransitiveDependencies( Set jarResourceArtifacts , List jarResources ) 
    throws ArtifactResolutionException, ArtifactNotFoundException 
    {

        // this restricts to runtime and compile scope            
        ScopeArtifactFilter artifactFilter = new ScopeArtifactFilter( Artifact.SCOPE_RUNTIME );
        
        ArtifactResolutionResult result = getArtifactResolver().resolveTransitively( jarResourceArtifacts, 
                                                                                     getProject().getArtifact(),
                                                                                     null, //managedVersions
                                                                                     getLocalRepository(),
                                                                                     getRemoteRepositories(), 
                                                                                     this.artifactMetadataSource,
                                                                                     artifactFilter );
        
        Set transitiveResolvedArtifacts = result.getArtifacts();

        if ( getLog().isDebugEnabled() )
        {
            getLog().debug( "transitively resolved artifacts = " + transitiveResolvedArtifacts );
            getLog().debug( "jarResources = " + jarResources );
            getLog().debug( "jarResourceArtifacts = " + jarResourceArtifacts );
        }
        
        //for each transitive dependency, wrap it in a JarResource and add it to the collection of
        //existing jar resources
        for ( Iterator itr = transitiveResolvedArtifacts.iterator(); itr.hasNext(); )
        {
            Artifact resolvedArtifact = (Artifact) itr.next();
            
            // this whole double check is ugly as well as this method changing the input variable
            // we should really improve the way we collect the jarResources
            if ( !jarResourceArtifacts.contains( resolvedArtifact ) )
            {
                JarResource newJarResource = new JarResource( resolvedArtifact );
                if ( !jarResources.contains( newJarResource ) )
                {
                    newJarResource.setOutputJarVersion( true );
                    jarResources.add( newJarResource );
                }
            }
            
        }
        
    }
    
    private void generateJnlpFile( JnlpFile jnlpFile, String libPath ) throws MojoExecutionException
    {
        
        File jnlpOutputFile = new File( getWorkDirectory(), jnlpFile.getOutputFilename() );
        
        Set jarResources = new LinkedHashSet();
        jarResources.addAll( jnlpFile.getJarResources() );
        
        if ( this.commonJarResources != null && !this.commonJarResources.isEmpty() )
        {
            
            for ( Iterator itr = this.commonJarResources.iterator(); itr.hasNext(); )
            {
                JarResource jarResource = ( JarResource ) itr.next();
                jarResources.add( jarResource );
            }
            
            jarResources.addAll( this.commonJarResources );
        }
        
        JarResourcesGenerator jnlpGenerator = new JarResourcesGenerator( getProject(), 
                                                   getTemplateDirectory(),
                                                   "default-jnlp-servlet-template.vm", 
                                                   jnlpOutputFile, 
                                                   jnlpFile.getTemplateFilename(), 
                                                   jarResources, 
                                                   jnlpFile.getMainClass(),
                                                   getWebstartJarURLForVelocity(),
                                                   libPath );

        jnlpGenerator.setExtraConfig( getGeneratorExtraConfig() );

        try
        {
            jnlpGenerator.generate();
        }
        catch ( Exception e )
        {
            throw new MojoExecutionException( "The following error occurred attempting to generate "
                                              + "the JNLP deployment descriptor: " + e, e );
        }
    
    }

    private GeneratorExtraConfig getGeneratorExtraConfig()
    {
        return new GeneratorExtraConfig()
        {
            public String getJnlpSpec()
            {
                return "1.0+";
            }
            public String getOfflineAllowed()
            {
                return "false";
            }
            public String getAllPermissions()
            {
                return "true";
            }
            public String getJ2seVersion()
            {
                return "1.5+";
            }
        };
    }



    /**
     * Generates a version.xml file for all the jarResources configured either in jnlpFile elements
     * or in the commonJarResources element.
     *
     * @throws MojoExecutionException
     */
    private void generateVersionXml() throws MojoExecutionException 
    {
        
        Set/*JarResource*/ jarResources = new LinkedHashSet();
        
        //combine the jar resources from commonJarResources and each JnlpFile config
        
        for ( Iterator itr = this.jnlpFiles.iterator(); itr.hasNext(); )
        {
            JnlpFile jnlpFile = ( JnlpFile ) itr.next();
            jarResources.addAll( jnlpFile.getJarResources() );
        }
        
        if ( this.commonJarResources != null )
        {
            jarResources.addAll( this.commonJarResources );
        }
        
        VersionXmlGenerator generator = new VersionXmlGenerator();
        generator.generate( getLibDirectory(), jarResources );
        
    }
    
    /**
     * {@inheritDoc}
     */
    public MavenProject getProject()
    {
        return this.project;
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
        StringBuffer sbuf = new StringBuffer();
        sbuf.append( artifact.getArtifactId() );
        
        if ( StringUtils.isNotEmpty( artifact.getClassifier() ) ) 
        {
            sbuf.append( "-" ).append( artifact.getClassifier() );
        }

        sbuf.append( "." ).append( artifact.getArtifactHandler().getExtension() );

        return sbuf.toString();
    
    }

    /**
     * Copies the contents of the working directory to the output directory.
     */
    private void copyWorkingDirToOutputDir() throws MojoExecutionException
    {
        
        File outputDir = new File( getProject().getBuild().getDirectory(), 
                                  getProject().getBuild().getFinalName() + File.separator + this.outputDirectoryName );
        
        if ( !outputDir.exists() ) 
        {
            
            if ( getLog().isInfoEnabled() )
            {
                getLog().info( "Creating JNLP output directory: " + outputDir.getAbsolutePath() );
            }
            
            if ( !outputDir.mkdirs() )
            {
                throw new MojoExecutionException( "Unable to create the output directory for the jnlp bundle" );
            }
            
        }
        
        try
        {
            FileUtils.copyDirectoryStructure( getWorkDirectory(), outputDir );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException(
                    "An error occurred attempting to copy a file to the JNLP output directory.", e );
        }
        
    }

}
