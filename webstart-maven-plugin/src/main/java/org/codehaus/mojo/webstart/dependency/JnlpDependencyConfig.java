package org.codehaus.mojo.webstart.dependency;

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
import org.codehaus.mojo.webstart.dependency.filenaming.DependencyFilenameStrategy;
import org.codehaus.mojo.webstart.sign.SignConfig;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * To configure a task on a jnlp dependency.
 * <p/>
 * Created on 1/4/14.
 *
 * @author Tony Chemit <chemit@codelutin.com>
 * @since 1.0-beta-5
 */
public class JnlpDependencyConfig
{
    /**
     * Global configuration used by all dependencies.
     */
    private final JnlpDependencyGlobalConfig globalConfig;

    /**
     * Artifact to treat.
     */
    private final Artifact artifact;

    /**
     * Should we use the outputJarVersion convention?
     */
    private final boolean outputJarVersion;

    /**
     * Working directory to process this jnlp dependency.
     */
    private File workingDirectory;

    public JnlpDependencyConfig( JnlpDependencyGlobalConfig globalConfig, Artifact artifact, String finalName,
                                 boolean outputJarVersion )
    {
        this.globalConfig = globalConfig;
        this.artifact = artifact;
        this.outputJarVersion = outputJarVersion;
        this.workingDirectory = new File( globalConfig.getWorkingDirectory(), finalName );
    }

    public Artifact getArtifact()
    {
        return artifact;
    }

    public File getWorkingDirectory()
    {
        return workingDirectory;
    }

    public boolean isOutputJarVersion()
    {
        return outputJarVersion;
    }

    public DependencyFilenameStrategy getDependencyFilenameStrategy()
    {
        return globalConfig.getDependencyFilenameStrategy();
    }

    public File getFinalDirectory()
    {
        return globalConfig.getFinalDirectory();
    }

    /**
     * Returns the flag that indicates whether or not jar resources
     * will be compressed using pack200.
     *
     * @return Returns the value of the pack200.enabled field.
     */
    public boolean isPack200()
    {
        return globalConfig.isPack200();
    }

    /**
     * Returns the files to be passed without pack200 compression.
     *
     * @return Returns the list value of the pack200.passFiles.
     */
    public List<String> getPack200PassFiles()
    {
        return globalConfig.getPack200PassFiles();
    }

    /**
     * Returns the flag that indicates whether or not a gzip should be
     * created for each jar resource.
     *
     * @return Returns the value of the gzip field.
     */
    public boolean isGzip()
    {
        return globalConfig.isGzip();
    }

    public boolean isVerbose()
    {
        return globalConfig.isVerbose();
    }

    public SignConfig getSign()
    {
        return globalConfig.getSign();
    }

    public Map<String, String> getUpdateManifestEntries()
    {
        return globalConfig.getUpdateManifestEntries();
    }

    public boolean isUnsignAlreadySignedJars()
    {
        return globalConfig.isUnsignAlreadySignedJars();
    }

    public boolean isCanUnsign()
    {
        return globalConfig.isCanUnsign();
    }

    public boolean isSign()
    {
        return globalConfig.isSign();
    }

    public boolean isUpdateManifest()
    {
        return globalConfig.isUpdateManifest();
    }
}
