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
import org.codehaus.mojo.webstart.ResolvedJarResource;
import org.codehaus.plexus.util.StringUtils;

import java.util.Collection;

/**
 * Generates a JNLP deployment descriptor.
 *
 * @author ngc
 * @author <a href="jerome@coffeebreaks.org">Jerome Lacoste</a>
 * @author Kevin Stembridge
 */
public class JarResourcesGenerator
    extends AbstractGenerator<JarResourceGeneratorConfig>
{

    public JarResourcesGenerator( Log log, GeneratorTechnicalConfig technicalConfig,
                                  JarResourceGeneratorConfig extraConfig )
    {
        super( log, technicalConfig, extraConfig );
    }

    /**
     * {@inheritDoc}
     */
    protected String getDependenciesText()
    {

        String jarResourcesText = "";

        String libPath = getExtraConfig().getLibPath();
        Collection<ResolvedJarResource> jarResources = getExtraConfig().getJarResources();

        if ( jarResources.size() != 0 )
        {
            final int multiplier = 100;
            StringBuilder buffer = new StringBuilder( multiplier * jarResources.size() );
            buffer.append( EOL );

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

                buffer.append( "/>" ).append( EOL );
            }
            jarResourcesText = buffer.toString();
        }
        return jarResourcesText;
    }

}
