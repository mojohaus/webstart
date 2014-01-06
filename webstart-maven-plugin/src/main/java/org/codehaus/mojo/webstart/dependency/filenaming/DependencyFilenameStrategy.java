package org.codehaus.mojo.webstart.dependency.filenaming;

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

/**
 * Define the naming strategy for the file name of an jnlp dependency from his associated artifact.
 * <p/>
 * Created on 1/6/14.
 *
 * @author Tony Chemit <chemit@codelutin.com>
 * @since 1.0-beta-5
 */
public interface DependencyFilenameStrategy
{
    /**
     * Plexus component role.
     */
    String ROLE = DependencyFilenameStrategy.class.getName();

    /**
     * Get the dependency file basename (filename without extension) for the given artifact.
     *
     * @param artifact         the artifact of the dependency
     * @param outputJarVersion flag if outputJarVersion is used
     * @return dependency file basename (filename without extension) for the given artifact.
     */
    String getDependencyFileBasename( Artifact artifact, Boolean outputJarVersion );

    /**
     * Get the dependency file name for the given artifact.
     *
     * @param artifact         the artifact of the dependency
     * @param outputJarVersion flag if outputJarVersion is used
     * @return dependency file name for the given artifact.
     */
    String getDependencyFilename( Artifact artifact, Boolean outputJarVersion );

    /**
     * Get the dependency file extension for the given artifact.
     *
     * @param artifact origin of dependency
     * @return dependency file extension for the given artifact.
     */
    String getDependencyFileExtension( Artifact artifact );

}
