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
import org.codehaus.mojo.webstart.dependency.task.JnlpDependencyTask;

import java.io.File;

/**
 * Created on 1/4/14.
 *
 * @author Tony Chemit <chemit@codelutin.com>
 * @since 1.0-beta-5
 */
public class JnlpDependencyRequest
{

    private final JnlpDependencyConfig config;

    private final JnlpDependencyTask[] tasks;

    private final File originalFile;

    private final File finalFile;

    private final boolean uptodate;

    public JnlpDependencyRequest( JnlpDependencyConfig config, JnlpDependencyTask... tasks )
    {
        this.config = config;
        this.tasks = tasks;

        originalFile = buildOriginalFile();
        finalFile = buildFinalFile();

        File incomingFile = getConfig().getArtifact().getFile();

        long limitDate = incomingFile.lastModified();
        uptodate = originalFile.exists() && originalFile.lastModified() > limitDate &&
            finalFile.exists() && finalFile.lastModified() > limitDate;
    }

    public JnlpDependencyConfig getConfig()
    {
        return config;
    }

    public JnlpDependencyTask[] getTasks()
    {
        return tasks;
    }

    public File getOriginalFile()
    {
        return originalFile;
    }

    public boolean isUptodate()
    {
        return uptodate;
    }

    public File getFinalFile()
    {
        return finalFile;
    }

    private File buildOriginalFile()
    {

        File workingDirectory = config.getWorkingDirectory();

        Artifact artifact = config.getArtifact();

        String fileName = config.getDependencyFilenameStrategy().getDependencyFilename( artifact, false );

        return new File( workingDirectory, fileName );
    }

    private File buildFinalFile()
    {

        File finalDirectory = config.getFinalDirectory();
        String filename = config.getDependencyFilenameStrategy().getDependencyFilename( config.getArtifact(),
                                                                                        config.isOutputJarVersion() );
        if ( config.isPack200() )
        {
            filename += ".pack";
        }

        if ( config.isGzip() )
        {
            filename += ".gz";
        }

        return new File( finalDirectory, filename );
    }
}
