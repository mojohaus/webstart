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
package org.codehaus.mojo.webstart.generator;

import java.io.File;
import java.io.FileWriter;
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

/**
 * The abstract superclass for classes that generate the JNLP files produced by the 
 * various MOJOs available in the plugin.
 *
 * @author Kevin Stembridge
 * @since 30 Aug 2007
 * @version $Revision:$
 *
 */
public abstract class AbstractGenerator {
    
    private VelocityEngine engine = new VelocityEngine();
    
    private final MavenProject mavenProject;

    private Template velocityTemplate;

    private final File outputFile;
    
    private final String mainClass;


    /**
     * Creates a new {@code AbstractGenerator}.
     * 
     * @param resourceLoaderPath  used to find the template in conjunction to inputFileTemplatePath
     * @param outputFile The location of the file to be generated.
     * @param inputFileTemplatePath relative to resourceLoaderPath 
     * @param mainCLass The text that should replace the $mainClass placeholder in the JNLP template.
     * 
     * @throws IllegalArgumentException if any argument is null.
     */
    protected AbstractGenerator(MavenProject mavenProject, 
                                File resourceLoaderPath, 
                                File outputFile, 
                                String inputFileTemplatePath, 
                                String mainClass) 
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

        if ( inputFileTemplatePath == null )
        {
            throw new IllegalArgumentException("templateFile must not be null");
        }
        
        if ( mainClass == null )
        {
            throw new IllegalArgumentException("mainClass must not be null");
        }
        
        this.outputFile = outputFile;
        this.mainClass = mainClass;
        this.mavenProject = mavenProject;
        
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

        if ( ! engine.templateExists( inputFileTemplatePath ) )
        {
            System.out.println( "Template not found!! ");
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
    
    /**
     * Generate the JNLP file.
     * @throws Exception
     */
    public final void generate() throws Exception
    {
        VelocityContext context = createAndPopulateContext();

        FileWriter writer = new FileWriter( outputFile );
        
        try
        {
            velocityTemplate.merge( context, writer );
            writer.flush();
        }
        catch ( Exception e )
        {
            throw new Exception( "Could not generate the template " + velocityTemplate.getName() + ": " + e.getMessage(), e );
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
    private VelocityContext createAndPopulateContext() {
        VelocityContext context = new VelocityContext();

        context.put( "dependencies", getDependenciesText( ) );

        // Note: properties that contain dots will not be properly parsed by Velocity. Should we replace dots with underscores ?        
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
        context.put( "explicitTimestamp", dateToExplicitTimestamp(timestamp) );
        context.put( "explicitTimestampUTC", dateToExplicitTimestampUTC(timestamp) );
        
        context.put( "outputFile", outputFile.getName() );
        context.put( "mainClass", this.mainClass );

        return context;
    }

    private void addPropertiesToContext( Properties properties, VelocityContext context ) {
        
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
    private String dateToExplicitTimestamp(Date date) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");        
        return new StringBuffer("TS: ").append(df.format(date)).toString();
    }
    
    /**
     * Converts a given date to an explicit timestamp string in UTC time zone.
     * 
     * @param date a timestamp to convert.
     * @return a string representing a timestamp.
     */
    private String dateToExplicitTimestampUTC(Date date) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        return new StringBuffer("TS: ").append(df.format(date)).append("Z").toString();
    }
    
}
