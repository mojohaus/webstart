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

import org.apache.commons.lang.BooleanUtils;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.log.NullLogSystem;
import org.codehaus.plexus.util.WriterFactory;

import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;

/**
 * The abstract superclass for classes that generate the JNLP files produced by the
 * various MOJOs available in the plugin.
 *
 * @author Kevin Stembridge
 * @version $Revision$
 * @since 30 Aug 2007
 */
public abstract class AbstractGenerator<C extends GeneratorExtraConfig>
{

//    private final MavenProject mavenProject;

//    private final File outputFile;

//    private final String encoding;
//
//    private final String mainClass;
//

    private VelocityEngine engine;

    private Template velocityTemplate;

    private final GeneratorTechnicalConfig config;

    private final C extraConfig;

    private Log log;

    protected AbstractGenerator( Log log, GeneratorTechnicalConfig config, C extraConfig )
    {

        this.log = log;
        this.config = config;
        this.extraConfig = extraConfig;

        Properties props = new Properties();

        String inputFileTemplatePath = config.getInputFileTemplatePath();

        if ( inputFileTemplatePath != null )
        {
            props.setProperty( VelocityEngine.RUNTIME_LOG_LOGSYSTEM_CLASS,
                               "org.apache.velocity.runtime.log.NullLogSystem" );
            props.setProperty( "file.resource.loader.path", config.getResourceLoaderPath().getAbsolutePath() );

            initVelocity( props );

            if ( !engine.templateExists( inputFileTemplatePath ) )
            {
                log.warn( "Warning, template not found. Will probably fail." );
            }
        }
        else
        {
            log.info( "No template specified Using default one." );

            inputFileTemplatePath = config.getDefaultTemplateResourceName();

            String webstartJarURL = config.getWebstartJarURL();
            log.debug( "***** Webstart JAR URL: " + webstartJarURL );

            props = new Properties();
            props.setProperty( "resource.loader", "jar" );
            props.setProperty( "jar.resource.loader.description",
                               "Jar resource loader for default webstart templates" );
            props.setProperty( "jar.resource.loader.class",
                               "org.apache.velocity.runtime.resource.loader.JarResourceLoader" );
            props.setProperty( "jar.resource.loader.path", webstartJarURL );

            initVelocity( props );

            if ( !engine.templateExists( inputFileTemplatePath ) )
            {
                log.error( "Inbuilt template not found!! " + config.getDefaultTemplateResourceName() +
                               " Will probably fail." );
            }
        }

        try
        {
            this.velocityTemplate = engine.getTemplate( inputFileTemplatePath, config.getEncoding() );
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

//    public void setExtraConfig( GeneratorExtraConfig extraConfig )
//    {
//        this.extraConfig = extraConfig;
//    }

    public C getExtraConfig()
    {
        return extraConfig;
    }

    /**
     * Generate the JNLP file.
     *
     * @throws Exception
     */
    public final void generate()
        throws Exception
    {
        VelocityContext context = createAndPopulateContext();

        Writer writer = WriterFactory.newWriter( config.getOutputFile(), config.getEncoding() );

        try
        {
            velocityTemplate.merge( context, writer );
            writer.flush();
        }
        catch ( Exception e )
        {
            throw new Exception(
                "Could not generate the template " + velocityTemplate.getName() + ": " + e.getMessage(), e );
        }
        finally
        {
            writer.close();
        }

    }

    /**
     * Subclasses must implement this method to return the text that should
     * replace the $dependencies placeholder in the JNLP template.
     *
     * @return The dependencies text, never null.
     */
    protected abstract String getDependenciesText();

    /**
     * Creates a Velocity context and populates it with replacement values
     * for our pre-defined placeholders.
     *
     * @return Returns a velocity context with system and maven properties added
     */
    protected VelocityContext createAndPopulateContext()
    {
        VelocityContext context = new VelocityContext();

        context.put( "dependencies", getDependenciesText() );

        // Note: properties that contain dots will not be properly parsed by Velocity. 
        // Should we replace dots with underscores ?        
        addPropertiesToContext( System.getProperties(), context );

        MavenProject mavenProject = config.getMavenProject();
        String encoding = config.getEncoding();

        addPropertiesToContext( mavenProject.getProperties(), context );
        addPropertiesToContext( extraConfig.getProperties(), context );

        context.put( "project", mavenProject.getModel() );
        context.put( "jnlpCodebase", extraConfig.getJnlpCodeBase() );

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

        context.put( "outputFile", config.getOutputFile().getName() );
        context.put( "mainClass", config.getMainClass() );

        context.put( "encoding", encoding );
        context.put( "input.encoding", encoding );
        context.put( "output.encoding", encoding );

        // TODO make this more extensible
        context.put( "allPermissions", BooleanUtils.toBoolean( extraConfig.getAllPermissions() ) );
        context.put( "offlineAllowed", BooleanUtils.toBoolean( extraConfig.getOfflineAllowed() ) );
        context.put( "jnlpspec", extraConfig.getJnlpSpec() );
        context.put( "j2seVersion", extraConfig.getJ2seVersion() );

        return context;
    }

    private void addPropertiesToContext( Map properties, VelocityContext context )
    {
        if ( properties != null )
        {
            for ( Object o : properties.keySet() )
            {
                String nextKey = (String) o;
                Object nextValue = properties.get( nextKey );
                context.put( nextKey, nextValue.toString() );
            }
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
        return "TS: " + df.format( date );
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
        return "TS: " + df.format( date ) + "Z";
    }

    /**
     * Add {@code level} space caracteres at the begin of each lines of the
     * given {@code text}.
     *
     * @param level the number of space caracteres to add
     * @param text  the text to prefix
     * @return the indented text
     */
    protected String indentText( int level, String text )
    {
        StringBuilder buffer = new StringBuilder();
        String[] lines = text.split( "\n" );
        String prefix = "";
        for ( int i = 0; i < level; i++ )
        {
            prefix += " ";
        }
        for ( String line : lines )
        {
            buffer.append( prefix ).append( line ).append( "\n" );
        }
        return buffer.toString();
    }
}
