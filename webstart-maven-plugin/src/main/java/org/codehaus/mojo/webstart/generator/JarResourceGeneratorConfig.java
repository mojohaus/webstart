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

import org.codehaus.mojo.webstart.ResolvedJarResource;

import java.util.Collection;
import java.util.Map;

/**
 * Created on 1/6/14.
 *
 * @author Tony Chemit <chemit@codelutin.com>
 * @since 1.0-beta-5
 */
public class JarResourceGeneratorConfig
    implements GeneratorExtraConfig
{

    private final Collection<ResolvedJarResource> jarResources;

    private final String libPath;

    private final String codebase;

    private final Map<String, String> properties;

    public JarResourceGeneratorConfig( Collection<ResolvedJarResource> jarResources, String libPath, String codebase,
                                       Map<String, String> properties )
    {
        this.jarResources = jarResources;
        this.libPath = libPath;
        this.codebase = codebase;
        this.properties = properties;
    }

    public Collection<ResolvedJarResource> getJarResources()
    {
        return jarResources;
    }

    public String getLibPath()
    {
        return libPath;
    }

    /**
     * {@inheritDoc}
     */
    public String getJnlpSpec()
    {
        return "1.0+";
    }

    /**
     * {@inheritDoc}
     */
    public String getOfflineAllowed()
    {
        return "false";
    }

    /**
     * {@inheritDoc}
     */
    public String getAllPermissions()
    {
        return "true";
    }

    /**
     * {@inheritDoc}
     */
    public String getJ2seVersion()
    {
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
        return properties;
    }
}
