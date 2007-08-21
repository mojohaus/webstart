package org.codehaus.mojo.webstart.generator;

/*
 * Copyright 2005 Nick C
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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.maven.project.MavenProject;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.log.NullLogSystem;
import org.codehaus.mojo.webstart.JarResource;
import org.codehaus.plexus.util.StringUtils;

/**
 * Generates a JNLP deployment descriptor.
 * 
 * TODO this class is a modified copy of <code>Generator</code>. The main 
 * difference being that this one uses JarResources whereas Generator 
 * determines dependencies from the project. This class was created 
 * because of the requirements of the new JnlpDownloadServletMojo, which
 * doesn't use project dependencies, but specifies JarResources instead.
 * The existing Mojos could be refactored to use JarResources also, 
 * and this class could be merged with Generator.
 *
 * @author ngc
 * @author <a href="jerome@coffeebreaks.org">Jerome Lacoste</a>
 * @author Kevin Stembridge
 */
public class JarResourcesGenerator

{
    private VelocityEngine engine = new VelocityEngine();

    private final Template template;

    private final MavenProject mavenProject;
    
    private final File outputFile;
    
    private final List jarResources;
    
    private final String mainClass;

    /**
     * Creates a new {@code JarResources}.
     * 
     * @param mavenProject The Maven project that this generator is being run within.
     * @param resourceLoaderPath  used to find the template in conjunction to inputFileTemplatePath
     * @param outputFile
     * @param templateFile relative to resourceLoaderPath
     * @param jarResources The collection of JarResources that will be output in the JNLP file.
     */
    public JarResourcesGenerator( MavenProject mavenProject, 
                       File resourceLoaderPath, 
                       File outputFile, 
                       String templateFile, 
                       List jarResources, 
                       String mainClass )
    {
        
        if ( mavenProject == null )
        {
            throw new IllegalArgumentException( "mavenProject must not be null" );
        }

        if ( resourceLoaderPath == null )
        {
            throw new IllegalArgumentException( "resourceLoaderPath must not be null" );
        }

        if ( outputFile == null )
        {
            throw new IllegalArgumentException( "outputFile must not be null" );
        }

        if ( templateFile == null )
        {
            throw new IllegalArgumentException("templateFile must not be null");
        }
        
        if ( StringUtils.isNotEmpty( this.mainClass ) )
        {
            throw new IllegalArgumentException( "mainClass must not be null." );
        }
        
        this.mavenProject = mavenProject;
        this.outputFile = outputFile;
        this.jarResources = jarResources;
        this.mainClass = mainClass;
        
        Properties props = new Properties();

        props.setProperty( VelocityEngine.RUNTIME_LOG_LOGSYSTEM_CLASS, 
                           "org.apache.velocity.runtime.log.NullLogSystem" );
        props.setProperty( "file.resource.loader.path", resourceLoaderPath.getAbsolutePath() );

        try
        {
            engine.setProperty( "runtime.log.logsystem", new NullLogSystem() );
            engine.init( props );
        }
        catch ( Exception e )
        {
            IllegalArgumentException iae = new IllegalArgumentException( "Could not initialise Velocity" );
            iae.initCause( e );
            throw iae;
        }

        if ( ! engine.templateExists( templateFile ) )
        {
            System.out.println( "Template not found!! ");
        }

        try
        {
            this.template = engine.getTemplate( templateFile );
        }
        catch ( Exception e )
        {
            IllegalArgumentException iae =
                new IllegalArgumentException( "Could not load the template file from '" + templateFile + "'" );
            iae.initCause( e );
            throw iae;
        }
        
    }

    public void generate() throws Exception
    {
        
        VelocityContext context = new VelocityContext();
        populateContext(context);

        FileWriter writer = null;
        
        try
        {
            writer = new FileWriter( this.outputFile );
            this.template.merge( context, writer );
            writer.flush();
        }
        catch ( Exception e )
        {
            throw new Exception( "An error occurred attempting to generate the JNLP file from the template [" 
                                 + this.template.getName() + "]: " + e.getMessage(), e );
        }
        finally
        {
            
            if ( writer != null )
            {
                
                try 
                {
                    writer.close();
                } 
                catch (IOException e) 
                {
                    // do nothing
                }
                
            }
            
        }
        
    }

    /**
     * Produces a string that is the XML fragment for the given collection of jar resources. 
     *
     * @param jarResources The collection of jar resources for which XML fragments will
     * be generated.
     * @return The XML string for the given jar resources.
     */
    private String getJarResourcesText( List/*JarResource*/ jarResources )
    {
        
        String jarResourcesText = "";
        
        if ( jarResources.size() != 0 )
        {
            
            StringBuffer buffer = new StringBuffer( 100 * jarResources.size() );
            buffer.append( "\n" );

            for ( Iterator itr = this.jarResources.iterator(); itr.hasNext(); )
            {
                JarResource jarResource = (JarResource) itr.next();
                
                if ( !jarResource.isIncludeInJnlp() )
                {
                    continue;
                }
                
                buffer.append( "<jar href=\"" ).append( jarResource.getHrefValue() ).append( "\"" );
                
                if ( jarResource.isOutputJarVersion() ) 
                {
                    buffer.append(" version=\"").append(jarResource.getVersion()).append("\"");
                }
                
                if ( jarResource.getMainClass() != null )
                {
                    buffer.append( " main=\"true\"" );
                }
                
                buffer.append( "/>\n" );
            }
                
            jarResourcesText = buffer.toString();
            
        }
            
        return jarResourcesText;
        
    }

    /**
     * @return Returns a velocity context with system and maven properties added
     */
    private void populateContext( VelocityContext context ) 
    {
        
        if ( this.jarResources != null && !this.jarResources.isEmpty() )
        {
            context.put( "dependencies", getJarResourcesText( this.jarResources ) );
        }
        
        context.put( "outputFile", this.outputFile.getName() );
        context.put( "mainClass", this.mainClass );

        // Note: properties that contain dots will not be properly parsed by Velocity. Should we replace dots with underscores ?        
        addPropertiesToContext( System.getProperties(), context );
        addPropertiesToContext( this.mavenProject.getProperties(), context );
     
        context.put( "project", this.mavenProject.getModel() );

        // aliases named after the JNLP file structure
        context.put( "informationTitle", this.mavenProject.getModel().getName() );
        context.put( "informationDescription", this.mavenProject.getModel().getDescription() );
        
        if ( this.mavenProject.getModel().getOrganization() != null )
        {
            context.put( "informationVendor", this.mavenProject.getModel().getOrganization().getName() );
            context.put( "informationHomepage", this.mavenProject.getModel().getOrganization().getUrl() );
        }

    }

    private static void addPropertiesToContext( Properties properties, VelocityContext context ) {
        for ( Iterator iter = properties.keySet().iterator(); iter.hasNext(); ) {
            String nextKey = (String) iter.next();
            String nextValue = properties.getProperty( nextKey );
            context.put( nextKey, nextValue );
        }
    }
    
}
