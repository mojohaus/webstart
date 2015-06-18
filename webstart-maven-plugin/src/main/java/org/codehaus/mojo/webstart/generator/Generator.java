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
import org.apache.maven.plugin.logging.Log;
import org.apache.velocity.VelocityContext;
import org.codehaus.mojo.webstart.JnlpExtension;

import java.util.Collection;
import java.util.List;

/**
 * Generates a JNLP deployment descriptor
 *
 * @author ngc
 * @author <a href="jerome@coffeebreaks.org">Jerome Lacoste</a>
 */
public class Generator
    extends AbstractGenerator<GeneratorConfig>
{

    public Generator( Log log, GeneratorTechnicalConfig technicalConfig, GeneratorConfig extraConfig )
    {
        super( log, technicalConfig, extraConfig );
    }

    /**
     * {@inheritDoc}
     */
    protected String getDependenciesText()
    {
        return indentText( 4, getDependenciesText( getExtraConfig() ) );
    }

    protected VelocityContext createAndPopulateContext()
    {
        VelocityContext context = super.createAndPopulateContext();
        if ( getExtraConfig().hasJnlpExtensions() )
        {
            // add extensions
            context.put( "extensions", indentText( 4, getExtensionsText( getExtraConfig() ) ) );
        } else {
            context.put( "extensions", "" );
        }
        return context;
    }

    static String getDependenciesText( GeneratorConfig config )
    {
        return getDependenciesText( config, config.getPackagedJnlpArtifacts() );
    }

    static String getDependenciesText( GeneratorExtraConfigWithDeps config, Collection<Artifact> artifacts )
    {
        String dependenciesText = "";
        if ( !artifacts.isEmpty() )
        {
            StringBuilder buffer = new StringBuilder( 100 * artifacts.size() );
            buffer.append( EOL );
            if ( config.isPack200() )
            {
                /*
                 * http://jira.codehaus.org/browse/MWEBSTART-174
                 *
                 * If we're going to use Pack200, we should specify jnlp.packEnabled
                 *
                 */
                buffer.append( "<property name=\"jnlp.packEnabled\" value=\"true\" />" ).append( EOL );
            }
            if ( config.isOutputJarVersions() )
            {
                /*
                 * http://jira.codehaus.org/browse/MWEBSTART-221
                 *
                 * If we're going to use version files, we should specify jnlp.versionEnabled
                 *
                 */
                buffer.append( "<property name=\"jnlp.versionEnabled\" value=\"true\" />" ).append( EOL );
            }
            String jarLibPath = null;
            if ( config.getLibPath() != null )
            {
                jarLibPath = config.getLibPath();
                jarLibPath = ( jarLibPath != null && jarLibPath.trim().length() != 0 ) ? jarLibPath.trim() : null;
            }

            for ( Artifact artifact : artifacts )
            {
                buffer.append( "<jar href=\"" );
                if ( jarLibPath != null )
                {
                    buffer.append( jarLibPath ).append( "/" );
                }

//                    String filename = artifact.getFile().getName();
                if ( config.isOutputJarVersions() )
                {
                    String filename = config.getDependencyFilename( artifact, null );
//                      String extension = filename.substring( filename.lastIndexOf( "." ) );
//                    buffer.append( artifact.getArtifactId() ).append( extension ).append( "\"" );
                    buffer.append( filename ).append( "\"" );
                    buffer.append( " version=\"" ).append( artifact.getVersion() ).append( "\"" );
                }
                else
                {
                    String filename = config.getDependencyFilename( artifact, false );
                    buffer.append( filename ).append( "\"" );
                }

                if ( config.isArtifactWithMainClass( artifact ) )
                {
                    buffer.append( " main=\"true\"" );
                }
                buffer.append( "/>" ).append( EOL );
            }
            dependenciesText = buffer.toString();
        }
        return dependenciesText;
    }

    static String getExtensionsText( GeneratorConfig config )
    {
        String text = "";
        List<JnlpExtension> extensions = config.getJnlpExtensions();
        if ( extensions != null && !extensions.isEmpty() )
        {
            StringBuilder buffer = new StringBuilder( 100 * extensions.size() );
            buffer.append( "\n" );

            for ( JnlpExtension extension : extensions )
            {
                buffer.append( "<extension name=\"" );
                buffer.append( extension.getName() );
                buffer.append( "\" href=\"" );
                buffer.append( extension.getOutputFile() );
                buffer.append( "\"/>" ).append( EOL );
            }
            text = buffer.toString();
        }
        return text;
    }
}
