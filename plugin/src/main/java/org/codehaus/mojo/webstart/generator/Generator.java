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

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.log.NullLogSystem;
import org.apache.maven.artifact.Artifact;
import org.codehaus.mojo.webstart.JnlpMojo;

import java.io.File;
import java.io.FileWriter;
import java.util.Properties;
import java.util.List;

/**
 * Generates a JNLP deployment descriptor
 *
 * @author ngc
 * @author <a href="jerome@coffeebreaks.org">Jerome Lacoste</a>
 */
public class Generator
{
    private VelocityEngine engine = new VelocityEngine();

    private JnlpMojo config;

    private Template template;

    private File outputFile;

    public Generator( JnlpMojo task, File outputFile, String inputFileTemplatePath )
    {
        this.config = task;

        this.outputFile = outputFile;
        //initialise the resource loader to use the class loader
        Properties props = new Properties();
        props.setProperty( VelocityEngine.RUNTIME_LOG_LOGSYSTEM, "org.apache.velocity.runtime.log.NullLogSystem" );
        // props.setProperty( VelocityEngine.RESOURCE_LOADER, "classpath" );
        // props.setProperty( "classpath." + VelocityEngine.RESOURCE_LOADER + ".class",
        //                    ClasspathResourceLoader.class.getName() );
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
        try
        {
            this.template = engine.getTemplate( inputFileTemplatePath );
        }
        catch ( Exception e )
        {
            IllegalArgumentException iae = new IllegalArgumentException(
                  "Could not load the template file from '" + inputFileTemplatePath + "'" );
            iae.initCause( e );
            throw iae;
        }
    }

    public void generate()
        throws Exception
    {
        VelocityContext context = new VelocityContext();

        List artifacts = config.getPackagedJnlpArtifacts();
        if ( artifacts.size() != 0 ) {
            StringBuffer buffer = new StringBuffer( 100 * artifacts.size() );
            buffer.append("\n");
            for (int i = 0; i < artifacts.size(); i++) {
                Artifact artifact = (Artifact) artifacts.get( i );
                buffer.append( "<jar href=\"" ).append( artifact.toString() ).append( "\"" );
                if ( config.isArtifactWithMainClass( artifact )) {
                    buffer.append( " main=\"true\"" );
                }
                buffer.append( "/>\n" );
            }
            context.put( "dependencies", buffer.toString() );
        } else {
            context.put( "dependencies", "" );
        }

        if ( config.getJnlp().getCodebase() != null ) {
            context.put( "codebase", config.getJnlp().getCodebase() );
        }
        /*
        if ( config.getVendor() != null ) {
            context.put( "vendor", config.getVendor() );
        }
        */
	
        context.put( "outputFile", outputFile );
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

}
