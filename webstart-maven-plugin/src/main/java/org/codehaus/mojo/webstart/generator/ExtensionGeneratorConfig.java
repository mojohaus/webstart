package org.codehaus.mojo.webstart.generator;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with work for additional information
 * regarding copyright ownership.  The ASF licenses file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use file except in compliance
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
import org.codehaus.mojo.webstart.JnlpExtension;
import org.codehaus.mojo.webstart.dependency.filenaming.DependencyFilenameStrategy;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created on 1/6/14.
 *
 * @author Tony Chemit <chemit@codelutin.com>
 * @since 1.0-beta-5
 */
public class ExtensionGeneratorConfig
    extends AbstractGeneratorExtraConfigWithDeps
{

    private final Map<JnlpExtension, List<Artifact>> extensionsJnlpArtifacts;

    private final JnlpExtension extension;

    private final String codebase;

    public ExtensionGeneratorConfig( String libPath, boolean pack200, boolean outputJarVersions,
                                     Artifact artifactWithMainClass,
                                     DependencyFilenameStrategy dependencyFilenameStrategy,
                                     Map<JnlpExtension, List<Artifact>> extensionsJnlpArtifacts, String codebase,
                                     JnlpExtension extension )
    {
        super( libPath, pack200, outputJarVersions, artifactWithMainClass, dependencyFilenameStrategy );
        this.extensionsJnlpArtifacts = extensionsJnlpArtifacts;
        this.extension = extension;
        this.codebase = codebase;
    }

    public JnlpExtension getExtension()
    {
        return extension;
    }

    public String getCodebase()
    {
        return codebase;
    }

    public List<Artifact> getExtensionJnlpArtifacts( JnlpExtension extension )
    {
        return extensionsJnlpArtifacts.get( extension );
    }

    /**
     * {@inheritDoc}
     */
    public String getJnlpSpec()
    {
        // shouldn't we automatically identify the spec based on the features used in the spec?
        // also detect conflicts. If user specified 1.0 but uses a 1.5 feature we should fail in checkInput().
        if ( extension.getSpec() != null )
        {
            return extension.getSpec();
        }
        return "1.0+";
    }

    /**
     * {@inheritDoc}
     */
    public String getOfflineAllowed()
    {
        if ( extension.getOfflineAllowed() != null )
        {
            return extension.getOfflineAllowed();
        }
        return "false";
    }

    /**
     * {@inheritDoc}
     */
    public String getAllPermissions()
    {
        if ( extension.getAllPermissions() != null )
        {
            return extension.getAllPermissions();
        }
        return "true";
    }

    /**
     * {@inheritDoc}
     */
    public String getJ2seVersion()
    {
        if ( extension.getJ2seVersion() != null )
        {
            return extension.getJ2seVersion();
        }
        return "1.5+";
    }

    /**
     * {@inheritDoc}
     */
    public String getJnlpCodeBase()
    {
        return codebase;
    }

    /**
     * {@inheritDoc}
     */
    public Map<String, String> getProperties()
    {
        return Collections.emptyMap();
    }
}
