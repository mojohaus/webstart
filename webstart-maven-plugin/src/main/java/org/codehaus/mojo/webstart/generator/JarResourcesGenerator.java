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

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.codehaus.mojo.webstart.ResolvedJarResource;
import org.codehaus.plexus.util.StringUtils;

import java.io.File;
import java.util.Collection;

/**
 * Generates a JNLP deployment descriptor.
 *
 * @author ngc
 * @author <a href="jerome@coffeebreaks.org">Jerome Lacoste</a>
 * @author Kevin Stembridge
 */
public class JarResourcesGenerator
    extends AbstractGenerator

{

    private final Collection<ResolvedJarResource> jarResources;

    private String libPath;

    /**
     * Creates a new {@code JarResources}.
     *
     * @param mavenProject       The Maven project that this generator is being run within.
     * @param resourceLoaderPath used to find the template in conjunction to inputFileTemplatePath
     * @param outputFile
     * @param templateFile       relative to resourceLoaderPath
     * @param jarResources       The collection of JarResources that will be output in the JNLP file.
     * @param mainClass          The fully qualified name of the application's main class.
     * @param libPath            The path where the libraries are placed within the jnlp structure
     */
    public JarResourcesGenerator( Log log, MavenProject mavenProject, File resourceLoaderPath,
                                  String defaultTemplateResourceName, File outputFile, String templateFile,
                                  Collection<ResolvedJarResource> jarResources, String mainClass, String webstartJarURL,
                                  String libPath, String encoding )
    {
        super( log, mavenProject, resourceLoaderPath, defaultTemplateResourceName, outputFile, templateFile, mainClass,
               webstartJarURL, encoding );
        this.jarResources = jarResources;
        this.libPath = libPath;
    }

    /**
     * {@inheritDoc}
     */
    protected String getDependenciesText()
    {

        String jarResourcesText = "";

        if ( jarResources.size() != 0 )
        {
            final int multiplier = 100;
            StringBuilder buffer = new StringBuilder( multiplier * jarResources.size() );
            buffer.append( "\n" );

            for ( ResolvedJarResource jarResource : jarResources )
            {

                if ( !jarResource.isIncludeInJnlp() )
                {
                    continue;
                }

                buffer.append( "<jar href=\"" );
                if ( StringUtils.isNotEmpty( libPath ) )
                {
                    buffer.append( libPath );
                    buffer.append( '/' );
                }
                buffer.append( jarResource.getHrefValue() );
                buffer.append( "\"" );

                if ( jarResource.isOutputJarVersion() )
                {
                    buffer.append( " version=\"" ).append( jarResource.getVersion() ).append( "\"" );
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

}
