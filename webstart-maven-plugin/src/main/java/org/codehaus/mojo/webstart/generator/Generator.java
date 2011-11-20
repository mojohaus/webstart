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

import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.MavenProject;
import org.apache.velocity.VelocityContext;
import org.codehaus.mojo.webstart.AbstractJnlpMojo;
import org.codehaus.mojo.webstart.JnlpExtension;

import java.io.File;
import java.util.Iterator;
import java.util.List;

/**
 * Generates a JNLP deployment descriptor
 *
 * @author ngc
 * @author <a href="jerome@coffeebreaks.org">Jerome Lacoste</a>
 */
public class Generator
    extends AbstractGenerator
{

    private AbstractJnlpMojo config;

    /**
     * @param task
     * @param resourceLoaderPath    used to find the template in conjunction to inputFileTemplatePath
     * @param outputFile
     * @param inputFileTemplatePath relative to resourceLoaderPath
     */
    public Generator( MavenProject mavenProject, AbstractJnlpMojo task, String defaultTemplateResourceName,
                      File resourceLoaderPath, File outputFile, String inputFileTemplatePath, String mainClass,
                      String webstartJarURL, String encoding )
    {
        super( mavenProject, resourceLoaderPath, defaultTemplateResourceName, outputFile, inputFileTemplatePath,
               mainClass, webstartJarURL, encoding );

        this.config = task;
    }

    /**
     * {@inheritDoc}
     */
    protected String getDependenciesText()
    {
        return indentText( 4, getDependenciesText( config ) );
    }

    protected VelocityContext createAndPopulateContext()
    {
        VelocityContext context = super.createAndPopulateContext();
        if ( config.hasJnlpExtensions() )
        {
            // add extensions
            context.put( "extensions", indentText( 4, getExtensionsText( config ) ) );
        }
        return context;
    }

    static String getDependenciesText( AbstractJnlpMojo config )
    {
        return getDependenciesText( config, config.getPackagedJnlpArtifacts() );
    }

    static String getDependenciesText( AbstractJnlpMojo config, List artifacts )
    {
        String dependenciesText = "";
        if ( artifacts.size() != 0 )
        {
            StringBuffer buffer = new StringBuffer( 100 * artifacts.size() );
            buffer.append( "\n" );
            if (config.isPack200()) {
                /*
                 * http://jira.codehaus.org/browse/MWEBSTART-174
                 *
                 * If we're going to use Pack200, we should specify jnlp.packEnabled
                 *
                 */
                buffer.append( "<property name=\"jnlp.packEnabled\" value=\"true\" />\n" );
            }
            String jarLibPath = null;
            if ( config.getLibPath() != null )
            {
                jarLibPath = config.getLibPath();
                jarLibPath = ( jarLibPath != null && jarLibPath.trim().length() != 0 ) ? jarLibPath.trim() : null;
            }

            for ( int i = 0; i < artifacts.size(); i++ )
            {
                Artifact artifact = (Artifact) artifacts.get( i );
                buffer.append( "<jar href=\"" );
                if ( jarLibPath != null )
                {
                    buffer.append( jarLibPath ).append( "/" );
                }
                buffer.append( artifact.getFile().getName() ).append( "\"" );

                if ( config.isOutputJarVersions() )
                {
                    buffer.append( " version=\"" ).append( artifact.getVersion() ).append( "\"" );
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

    static String getExtensionsText( AbstractJnlpMojo config )
    {
        String text = "";
        List extensions = config.getJnlpExtensions();
        if ( extensions != null && !extensions.isEmpty() )
        {
            StringBuffer buffer = new StringBuffer( 100 * extensions.size() );
            buffer.append( "\n" );

            for ( Iterator it = extensions.iterator(); it.hasNext(); )
            {
                JnlpExtension extension = (JnlpExtension) it.next();
                buffer.append( "<extension name=\"" );
                buffer.append( extension.getName() );
                buffer.append( "\" href=\"" );
                buffer.append( extension.getOutputFile() );
                buffer.append( "\"/>\n" );
            }
            text = buffer.toString();
        }
        return text;
    }
}
