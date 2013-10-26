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

import org.codehaus.mojo.webstart.JnlpExtension;

/**
 * {@link GeneratorExtraConfig} implementation for a {@link JnlpExtension}.
 *
 * @author tchemit <chemit@codelutin.com>
 * @since 1.0-beta-4
 */
public class ExtensionGeneratorExtraConfig
    implements GeneratorExtraConfig
{
    private final JnlpExtension extension;

    private final String codebase;

    public ExtensionGeneratorExtraConfig( JnlpExtension extension, String codebase )
    {
        this.extension = extension;
        this.codebase = codebase;
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

}
