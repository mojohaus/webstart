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

import java.util.List;

/**
 * To generate an extension jnlp file.
 *
 * @author chemit
 */
public class ExtensionGenerator
    extends AbstractGenerator<ExtensionGeneratorConfig>
{

    public ExtensionGenerator( Log log, GeneratorTechnicalConfig technicalConfig, ExtensionGeneratorConfig extraConfig )
    {
        super( log, technicalConfig, extraConfig );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected VelocityContext createAndPopulateContext()
    {
        VelocityContext context = super.createAndPopulateContext();
        // add the extension in velocity context
        context.put( "extension", getExtraConfig().getExtension() );
        return context;
    }

    /**
     * {@inheritDoc}
     */
    protected String getDependenciesText()
    {
        List<Artifact> dependencies = getExtraConfig().getExtensionJnlpArtifacts( getExtraConfig().getExtension() );

        return indentText( 4, Generator.getDependenciesText( getExtraConfig(), dependencies ) );
    }

}
