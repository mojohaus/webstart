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

import org.apache.commons.collections.CollectionUtils;
import org.apache.maven.artifact.Artifact;
import org.codehaus.mojo.webstart.JnlpConfig;
import org.codehaus.mojo.webstart.JnlpExtension;
import org.codehaus.mojo.webstart.dependency.filenaming.DependencyFilenameStrategy;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * configuration of {@link Generator}.
 * <p/>
 * Created on 1/5/14.
 *
 * @author Tony Chemit <chemit@codelutin.com>
 * @since 1.0-beta-5
 */
public class GeneratorConfig
    extends AbstractGeneratorExtraConfigWithDeps
{

    private final Collection<Artifact> packagedJnlpArtifacts;

    private final List<JnlpExtension> jnlpExtensions;

    private final String codebase;

    private final JnlpConfig jnlp;

    public GeneratorConfig( String libPath, boolean pack200, boolean outputJarVersions, Artifact artifactWithMainClass,
                            DependencyFilenameStrategy dependencyFilenameStrategy,
                            Collection<Artifact> packagedJnlpArtifacts, List<JnlpExtension> jnlpExtensions,
                            String codebase, JnlpConfig jnlp )
    {
        super( libPath, pack200, outputJarVersions, artifactWithMainClass, dependencyFilenameStrategy );

        this.packagedJnlpArtifacts = packagedJnlpArtifacts;
        this.jnlpExtensions = jnlpExtensions;
        this.codebase = codebase;
        this.jnlp = jnlp;
    }

    /**
     * {@inheritDoc}
     */
    public Collection<Artifact> getPackagedJnlpArtifacts()
    {
        return packagedJnlpArtifacts;
    }

    public List<JnlpExtension> getJnlpExtensions()
    {
        return jnlpExtensions;
    }

    public boolean hasJnlpExtensions()
    {
        return CollectionUtils.isNotEmpty( jnlpExtensions );
    }

    /**
     * {@inheritDoc}
     */
    public String getJnlpSpec()
    {
        // shouldn't we automatically identify the spec based on the features used in the spec?
        // also detect conflicts. If user specified 1.0 but uses a 1.5 feature we should fail in checkInput().
        if ( jnlp.getSpec() != null )
        {
            return jnlp.getSpec();
        }
        return "1.0+";
    }

    /**
     * {@inheritDoc}
     */
    public String getOfflineAllowed()
    {
        if ( jnlp.getOfflineAllowed() != null )
        {
            return jnlp.getOfflineAllowed();
        }
        return "false";
    }

    /**
     * {@inheritDoc}
     */
    public String getAllPermissions()
    {
        if ( jnlp.getAllPermissions() != null )
        {
            return jnlp.getAllPermissions();
        }
        return "true";
    }

    /**
     * {@inheritDoc}
     */
    public String getJ2seVersion()
    {
        if ( jnlp.getJ2seVersion() != null )
        {
            return jnlp.getJ2seVersion();
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
        return jnlp.getProperties();
    }
}
