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
import java.util.Iterator;
import java.util.List;

import org.apache.maven.project.MavenProject;
import org.codehaus.mojo.webstart.JarResource;

/**
 * Generates a JNLP deployment descriptor.
 * 
 * @author ngc
 * @author <a href="jerome@coffeebreaks.org">Jerome Lacoste</a>
 * @author Kevin Stembridge
 */
public class JarResourcesGenerator extends AbstractGenerator

{

    private final List jarResources;
    
    /**
     * Creates a new {@code JarResources}.
     * 
     * @param mavenProject The Maven project that this generator is being run within.
     * @param resourceLoaderPath  used to find the template in conjunction to inputFileTemplatePath
     * @param outputFile
     * @param templateFile relative to resourceLoaderPath
     * @param jarResources The collection of JarResources that will be output in the JNLP file.
     * @param mainClass The fully qualified name of the application's main class.
     */
    public JarResourcesGenerator( MavenProject mavenProject, 
                                  File resourceLoaderPath, 
                                  File outputFile, 
                                  String templateFile, 
                                  List jarResources, 
                                  String mainClass )
    {
        super(mavenProject, resourceLoaderPath, outputFile, templateFile, mainClass);
        this.jarResources = jarResources;
    }

    /** 
     * {@inheritDoc}
     */
    protected String getDependenciesText() {

        String jarResourcesText = "";
        
        if ( this.jarResources.size() != 0 )
        {
            
            StringBuffer buffer = new StringBuffer( 100 * this.jarResources.size() );
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

}
