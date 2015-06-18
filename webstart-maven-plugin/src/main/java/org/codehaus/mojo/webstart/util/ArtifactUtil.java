package org.codehaus.mojo.webstart.util;

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
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.mojo.webstart.JarResource;

import java.util.List;
import java.util.Set;

/**
 * Some usefull methods on artifacts.
 *
 * @author tchemit <chemit@codelutin.com>
 * @since 1.0-beta-4
 */
public interface ArtifactUtil
{

    /**
     * Plexus component role.
     */
    String ROLE = ArtifactUtil.class.getName();

    /**
     * Tests if the given fully qualified name exists in the given artifact.
     *
     * @param artifact  artifact to test
     * @param mainClass the fully qualified name to find in artifact
     * @return {@code true} if given artifact contains the given fqn, {@code false} otherwise
     * @throws MojoExecutionException if artifact file url is mal formed
     */
    public boolean artifactContainsClass( Artifact artifact, final String mainClass )
        throws MojoExecutionException;

    /**
     * Creates from the given jar resource the underlying artifact.
     *
     * @param jarResource the jar resource
     * @return the created artifact from the given jar resource
     */
    Artifact createArtifact( JarResource jarResource );

    MavenProject resolveFromReactor( Artifact artifact, MavenProject mavenProject, List<MavenProject> reactorProjects )
        throws MojoExecutionException;


    void resolveFromRepositories( Artifact artifact, List remoteRepositories, ArtifactRepository localRepository )
        throws MojoExecutionException;

    Set<Artifact> resolveTransitively( Set<Artifact> jarResourceArtifacts, Set<MavenProject> siblingProjects,
                                       Artifact artifact, ArtifactRepository localRepository,
                                       List<ArtifactRepository> remoteRepositories, ArtifactFilter artifactFilter )
        throws MojoExecutionException;
}
