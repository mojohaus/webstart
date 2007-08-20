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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.MavenProject;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.log.NullLogSystem;
import org.codehaus.mojo.webstart.AbstractJnlpMojo;

/**
 * Generates a JNLP deployment descriptor
 *
 * @author ngc
 * @author <a href="jerome@coffeebreaks.org">Jerome Lacoste</a>
 */
public class Generator
{
    private VelocityEngine engine = new VelocityEngine();

    private AbstractJnlpMojo config;

    private Template template;

    private File outputFile;

    /**
     * @param task
     * @param resourceLoaderPath  used to find the template in conjunction to inputFileTemplatePath
     * @param outputFile
     * @param inputFileTemplatePath relative to resourceLoaderPath
     */
    public Generator( AbstractJnlpMojo task, File resourceLoaderPath, File outputFile, String inputFileTemplatePath )
    {
        this.config = task;

        this.outputFile = outputFile;
        //initialise the resource loader to use the class loader
        Properties props = new Properties();

        props.setProperty( VelocityEngine.RUNTIME_LOG_LOGSYSTEM_CLASS,
                           "org.apache.velocity.runtime.log.NullLogSystem" );
        props.setProperty( "file.resource.loader.path", resourceLoaderPath.getAbsolutePath() );

        // System.out.println("OUHHHHH " + resourceLoaderPath.getAbsolutePath());

        // props.setProperty( VelocityEngine.RESOURCE_LOADER, "classpath" );
        // props.setProperty( "classpath." + VelocityEngine.RESOURCE_LOADER + ".class",
        //                   ClasspathResourceLoader.class.getName() );
        try
        {
            //initialise the Velocity engine
            engine.setProperty( "runtime.log.logsystem", new NullLogSystem() );
            engine.init( props );
        }
        catch ( Exception e )
        {
            IllegalArgumentException iae = new IllegalArgumentException( "Could not initialise Velocity" );
            iae.initCause( e );
            throw iae;
        }
        //set the template
        if ( ! engine.templateExists( inputFileTemplatePath ) )
        {
            System.out.println( "Template not found!! ");
        }
        try
        {
            this.template = engine.getTemplate( inputFileTemplatePath );
        }
        catch ( Exception e )
        {
            IllegalArgumentException iae =
                new IllegalArgumentException( "Could not load the template file from '" + inputFileTemplatePath + "'" );
            iae.initCause( e );
            throw iae;
        }
    }

    public void generate()
        throws Exception
    {
        VelocityContext context = createContext();

        context.put( "dependencies", getDependenciesText( config ) );

        // I don't think we really need this anymore. Let's reenable it when really required.
        /*
        if ( config.getJnlp().getCodebase() != null ) {
            context.put( "codebase", config.getJnlp().getCodebase() );
        }
        */
        /*
        if ( config.getVendor() != null ) {
            context.put( "vendor", config.getVendor() );
        }
        */

        context.put( "outputFile", outputFile.getName() );
        context.put( "mainClass", config.getJnlp().getMainClass() );
        FileWriter writer = new FileWriter( outputFile );
        try
        {
            //parse the template
            //StringWriter writer = new StringWriter();
            template.merge( context, writer );
            writer.flush();
        }
        catch ( Exception e )
        {
            throw new Exception( "Could not generate the template " + template.getName() + ": " + e.getMessage(), e );
        }
        finally
        {
            writer.close();
        }
    }

    static String getDependenciesText( AbstractJnlpMojo config )
    {
        String dependenciesText = "";
        List artifacts = config.getPackagedJnlpArtifacts();
        if ( artifacts.size() != 0 )
        {
            StringBuffer buffer = new StringBuffer( 100 * artifacts.size() );
            buffer.append( "\n" );
            for ( int i = 0; i < artifacts.size(); i++ )
            {
                Artifact artifact = (Artifact) artifacts.get( i );
                buffer.append( "<jar href=\"" ).append( artifact.getFile().getName() ).append( "\"" );
                
                if (config.isOutputJarVersions()) 
                {
                    buffer.append(" version=\"").append(artifact.getVersion()).append("\"");
                }
                
                if ( config.isArtifactWithMainClass( artifact ) )
                {
                    buffer.append( " main=\"true\"" );
                }
                buffer.append( "/>\n" );
            }
            dependenciesText = buffer.toString();
        }
        return dependenciesText;
    }

    /**
     * @return Returns a velocity context with system and maven properties added
     */
    private VelocityContext createContext() {
        VelocityContext context = new VelocityContext();

        context.put( "dependencies", getDependenciesText( config ) );

        MavenProject project = config.getProject();

        // Note: properties that contain dots will not be properly parsed by Velocity. Should we replace dots with underscores ?        
        addPropertiesToContext( System.getProperties(), context );
        addPropertiesToContext( project.getProperties(), context );
     
        context.put( "project", project.getModel() );

        // aliases named after the JNLP file structure
        context.put( "informationTitle", project.getModel().getName() );
        context.put( "informationDescription", project.getModel().getDescription() );
        if ( project.getModel().getOrganization() != null )
        {
            context.put( "informationVendor", project.getModel().getOrganization().getName() );
            context.put( "informationHomepage", project.getModel().getOrganization().getUrl() );
        }
        
        // explicit timestamps in local and and UTC time zones
        Date timestamp = new Date();
        context.put( "explicitTimestamp", dateToExplicitTimestamp(timestamp) );
        context.put( "explicitTimestampUTC", dateToExplicitTimestampUTC(timestamp) );

        return context;
    }

    private static void addPropertiesToContext( Properties properties, VelocityContext context ) {
        for ( Iterator iter = properties.keySet().iterator(); iter.hasNext(); ) {
            String nextKey = (String) iter.next();
            String nextValue = properties.getProperty( nextKey );
            context.put( nextKey, nextValue );
        }
    }
    
    /**
     * Converts a given date to an explicit timestamp string in local time zone.
     * 
     * @param date a timestamp to convert.
     * @return a string representing a timestamp.
     */
    private static String dateToExplicitTimestamp(Date date) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");        
        return new StringBuffer("TS: ").append(df.format(date)).toString();
    }
    
    /**
     * Converts a given date to an explicit timestamp string in UTC time zone.
     * 
     * @param date a timestamp to convert.
     * @return a string representing a timestamp.
     */
    private static String dateToExplicitTimestampUTC(Date date) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        return new StringBuffer("TS: ").append(df.format(date)).append("Z").toString();
    }
}
