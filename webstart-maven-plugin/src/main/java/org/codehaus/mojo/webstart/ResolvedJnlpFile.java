package org.codehaus.mojo.webstart;

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

import java.util.Map;
import java.util.Set;

/**
 * Represents a resolved jnlpFile.
 * <p/>
 * Created on 10/29/13.
 *
 * @author Tony Chemit <chemit@codelutin.com>
 * @since 1.0-beta-4
 */
public class ResolvedJnlpFile
{
    /**
     * The user config (from pom).
     */
    private final JnlpFile config;

    /**
     * The resolved jar resources (contains resolved configured jar resources, resolved common jar
     * resources + transitive jar resources if needed).
     */
    private final Set<ResolvedJarResource> jarResources;

    private String inputTemplate;

    public ResolvedJnlpFile( JnlpFile config, Set<ResolvedJarResource> jarResources )
    {
        this.config = config;
        this.jarResources = jarResources;
        this.inputTemplate = this.config.getInputTemplate();
    }

    public String getOutputFilename()
    {
        return config.getOutputFilename();
    }

    public String getMainClass()
    {
        return config.getMainClass();
    }

    public Map<String, String> getProperties()
    {
        return config.getProperties();
    }

    public String getInputTemplateResourcePath()
    {
        return config.getInputTemplateResourcePath();
    }

    public Set<ResolvedJarResource> getJarResources()
    {
        return jarResources;
    }

    public String getInputTemplate()
    {
        return inputTemplate;
    }

    public void setInputTemplate( String inputTemplate )
    {
        this.inputTemplate = inputTemplate;
    }
}
