package org.codehaus.mojo.webstart.generator;

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
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Properties;
import java.util.TimeZone;

import org.apache.maven.project.MavenProject;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.log.NullLogSystem;
import org.codehaus.plexus.util.WriterFactory;

/**
 * The abstract superclass for classes that generate the JNLP files produced by the 
 * various MOJOs available in the plugin.
 *
 * @author Kevin Stembridge
 * @since 30 Aug 2007
 * @version $Revision$
 *
 */
public abstract class AbstractGenerator 
{
    
    private VelocityEngine engine;
    
    private final MavenProject mavenProject;

    private Template velocityTemplate;

    private final File outputFile;
    
    private final String mainClass;

    private GeneratorExtraConfig extraConfig;


    /**
     * Creates a new {@code AbstractGenerator}.
     * 
     * @param resourceLoaderPath  used to find the template in conjunction to inputFileTemplatePath
     * @param outputFile The location of the file to be generated.
     * @param inputFileTemplatePath relative to resourceLoaderPath 
     * @param mainClass The text that should replace the $mainClass placeholder in the JNLP template.
     * 
     * @throws IllegalArgumentException if any argument is null.
     */
    protected AbstractGenerator( MavenProject mavenProject, 
                                File resourceLoaderPath, 
                                String defaultTemplateResourceName,
                                File outputFile, 
                                String inputFileTemplatePath, 
                                String mainClass,
                                String webstartJarURL ) 
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

        if ( mainClass == null )
        {
            throw new IllegalArgumentException( "mainClass must not be null" );
        }
        
        this.outputFile = outputFile;
        this.mainClass = mainClass;
        this.mavenProject = mavenProject;
        
        Properties props = new Properties();

        if ( inputFileTemplatePath != null )
        {
            props.setProperty( VelocityEngine.RUNTIME_LOG_LOGSYSTEM_CLASS,
                           "org.apache.velocity.runtime.log.NullLogSystem" );
            props.setProperty( "file.resource.loader.path", resourceLoaderPath.getAbsolutePath() );

            initVelocity( props );

            if ( !engine.templateExists( inputFileTemplatePath ) )
            {
                System.out.println( "Warning, template not found. Will probably fail." );
            }
        }
        else 
        {
            System.out.println( "No template specified Using default one." );

            inputFileTemplatePath = defaultTemplateResourceName;

            System.out.println( "***** Webstart JAR URL: " + webstartJarURL );

            props = new Properties();
            props.setProperty( "resource.loader", "jar" );
            props.setProperty( "jar.resource.loader.description", 
                               "Jar resource loader for default webstart templates" );
            props.setProperty( "jar.resource.loader.class", 
                               "org.apache.velocity.runtime.resource.loader.JarResourceLoader" );
            props.setProperty( "jar.resource.loader.path", webstartJarURL );

            initVelocity( props );

            if ( ! engine.templateExists( inputFileTemplatePath ) )
            {
                System.out.println( "Inbuilt template not found!! "  + defaultTemplateResourceName
                                    + " Will probably fail." );
            }
        }

        try
        {
            this.velocityTemplate = engine.getTemplate( inputFileTemplatePath );
        }
        catch ( Exception e )
        {
            IllegalArgumentException iae =
                new IllegalArgumentException( "Could not load the template file from '" + inputFileTemplatePath + "'" );
            iae.initCause( e );
            throw iae;
        }
    }

    private void initVelocity( Properties props )
    {
        try
        {
            engine = new VelocityEngine();
            engine.setProperty( "runtime.log.logsystem", new NullLogSystem() );
            engine.init( props );
        }
        catch ( Exception e )
        {
            IllegalArgumentException iae = new IllegalArgumentException( "Could not initialise Velocity" );
            iae.initCause( e );
            throw iae;
        }
    }

    public void setExtraConfig( GeneratorExtraConfig extraConfig )
    {
        this.extraConfig = extraConfig;
    }
    
    /**
     * Generate the JNLP file.
     * @throws Exception
     */
    public final void generate() throws Exception
    {
        VelocityContext context = createAndPopulateContext();

        Writer writer = WriterFactory.newXmlWriter( outputFile );
        
        try
        {
            velocityTemplate.merge( context, writer );
            writer.flush();
        }
        catch ( Exception e )
        {
            throw new Exception( "Could not generate the template " + velocityTemplate.getName() 
                                 + ": " + e.getMessage(), e );
        }
        finally
        {
            writer.close();
        }
        
    }

    /**
     * Subclasses must implement this method to return the text that should 
     * replace the $dependencies placeholder in the JNLP template.
     * @return The dependencies text, never null.
     */
    protected abstract String getDependenciesText( );
    
    /**
     * Creates a Velocity context and populates it with replacement values
     * for our pre-defined placeholders.
     * 
     * @return Returns a velocity context with system and maven properties added
     */
    private VelocityContext createAndPopulateContext() 
    {
        VelocityContext context = new VelocityContext();

        context.put( "dependencies", getDependenciesText( ) );

        // Note: properties that contain dots will not be properly parsed by Velocity. 
        // Should we replace dots with underscores ?        
        addPropertiesToContext( System.getProperties(), context );
        addPropertiesToContext( mavenProject.getProperties(), context );
     
        context.put( "project", mavenProject.getModel() );

        // aliases named after the JNLP file structure
        context.put( "informationTitle", mavenProject.getModel().getName() );
        context.put( "informationDescription", mavenProject.getModel().getDescription() );
        if ( mavenProject.getModel().getOrganization() != null )
        {
            context.put( "informationVendor", mavenProject.getModel().getOrganization().getName() );
            context.put( "informationHomepage", mavenProject.getModel().getOrganization().getUrl() );
        }
        
        // explicit timestamps in local and and UTC time zones
        Date timestamp = new Date();
        context.put( "explicitTimestamp", dateToExplicitTimestamp( timestamp ) );
        context.put( "explicitTimestampUTC", dateToExplicitTimestampUTC( timestamp ) );
        
        context.put( "outputFile", outputFile.getName() );
        context.put( "mainClass", this.mainClass );

        // TODO make this more extensible
        context.put( "allPermissions", extraConfig.getJnlpSpec() );
        context.put( "offlineAllowed", extraConfig.getOfflineAllowed() );
        context.put( "jnlpspec", extraConfig.getJnlpSpec() );
        context.put( "j2seVersion", extraConfig.getJ2seVersion() );

        return context;
    }


    private void addPropertiesToContext( Properties properties, VelocityContext context ) 
    {
        for ( Iterator iter = properties.keySet().iterator(); iter.hasNext(); ) 
        {
            String nextKey = ( String ) iter.next();
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
    private String dateToExplicitTimestamp( Date date ) 
    {
        DateFormat df = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ssZ" );        
        return new StringBuffer( "TS: " ).append( df.format( date ) ).toString();
    }
    
    /**
     * Converts a given date to an explicit timestamp string in UTC time zone.
     * 
     * @param date a timestamp to convert.
     * @return a string representing a timestamp.
     */
    private String dateToExplicitTimestampUTC( Date date ) 
    {
        DateFormat df = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );
        df.setTimeZone( TimeZone.getTimeZone( "UTC" ) );
        return new StringBuffer( "TS: " ).append( df.format( date ) ).append( "Z" ).toString();
    }
}
