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
import org.codehaus.mojo.webstart.dependency.filenaming.DependencyFilenameStrategy;

/**
 * Created on 1/5/14.
 *
 * @author Tony Chemit - dev@tchemit.fr
 * @since 1.0-beta-5
 */
public abstract class AbstractGeneratorExtraConfigWithDeps implements GeneratorExtraConfigWithDeps {

    private final String libPath;

    private final boolean pack200;

    private final boolean outputJarVersions;

    private final boolean useUniqueVersions;

    private final Artifact artifactWithMainClass;

    private final DependencyFilenameStrategy dependencyFilenameStrategy;

    public AbstractGeneratorExtraConfigWithDeps(
            String libPath,
            boolean pack200,
            boolean outputJarVersions,
            boolean useUniqueVersions,
            Artifact artifactWithMainClass,
            DependencyFilenameStrategy dependencyFilenameStrategy) {

        this.libPath = libPath;
        this.pack200 = pack200;
        this.outputJarVersions = outputJarVersions;
        this.useUniqueVersions = useUniqueVersions;
        this.artifactWithMainClass = artifactWithMainClass;
        this.dependencyFilenameStrategy = dependencyFilenameStrategy;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isPack200() {
        return pack200;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isOutputJarVersions() {
        return outputJarVersions;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isUseUniqueVersions() {
        return useUniqueVersions;
    }

    /**
     * {@inheritDoc}
     */
    public String getLibPath() {
        return libPath;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isArtifactWithMainClass(Artifact artifact) {
        return artifactWithMainClass != null && artifactWithMainClass.equals(artifact);
    }

    /**
     * {@inheritDoc}
     */
    public String getDependencyFilename(Artifact artifact, Boolean outputJarVersion) {
        return dependencyFilenameStrategy.getDependencyFilename(artifact, outputJarVersion, useUniqueVersions);
    }

    /**
     * {@inheritDoc}
     */
    public String getDependencyFileVersion(Artifact artifact) {
        return dependencyFilenameStrategy.getDependencyFileVersion(artifact, useUniqueVersions);
    }
}
