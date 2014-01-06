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

import java.io.File;

/**
 * Created on 1/4/14.
 *
 * @author Tony Chemit <chemit@codelutin.com>
 * @since 1.0-beta-5
 */
public class JnlpDependencyResult
{
    private final Artifact artifact;

    private final File originalfile;

    private File finalFile;

    private Exception error;

    public JnlpDependencyResult( Artifact artifact, File originalFile )
    {
        this.artifact = artifact;
        this.originalfile = originalFile;
    }

    public Artifact getArtifact()
    {
        return artifact;
    }

    public File getOriginalfile()
    {
        return originalfile;
    }

    public File getFinalFile()
    {
        return finalFile;
    }

    public Exception getError()
    {
        return error;
    }

    public void setFinalFile( File finalFile )
    {
        this.finalFile = finalFile;
    }

    public void setError( Exception error )
    {
        this.error = error;
    }

    public boolean isError()
    {
        return error != null;
    }
}
