package org.codehaus.mojo.webstart.dependency.filenaming;

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

/**
 * Created on 1/6/14.
 *
 * @author Tony Chemit - dev@tchemit.fr
 * @since 1.0-beta-5
 */
public abstract class AbstractDependencyFilenameStrategy implements DependencyFilenameStrategy {

    /**
     * {@inheritDoc}
     */
    public String getDependencyFilename(Artifact artifact, Boolean outputJarVersion, Boolean useUniqueVersions) {
        String filename = getDependencyFileBasename(artifact, outputJarVersion, useUniqueVersions);

        filename += "." + getDependencyFileExtension(artifact);

        return filename;
    }

    /**
     * {@inheritDoc}
     */
    public String getDependencyFileExtension(Artifact artifact) {
        String extension = artifact.getArtifactHandler().getExtension();
        return extension;
    }

    /**
     * {@inheritDoc}
     */
    public String getDependencyFileVersion(Artifact artifact, Boolean useUniqueVersions) {
        if (useUniqueVersions != null && useUniqueVersions) {
            return UniqueVersionsHelper.getUniqueVersion(artifact);
        }

        return artifact.getVersion();
    }
}
