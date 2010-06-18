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

import org.apache.maven.project.MavenProject;

/**
 * Packages a jnlp application.
 * <p/>
 * The plugin tries to not re-sign/re-pack if the dependent jar hasn't changed.
 * As a consequence, if one modifies the pom jnlp config or a keystore, one should clean before rebuilding.
 * <p/>This mojo forks a build lifecycle and won't install the zip packages in your local repository. 
 * You probably want to use the jnlp-inline instead.
 *
 * @author <a href="jerome@coffeebreaks.org">Jerome Lacoste</a>
 * @version $Id$
 * @goal jnlp
 * @execute phase="package"
 * @aggregator
 * @requiresDependencyResolution runtime
 * @requiresProject
 * @inheritedByDefault true
 */
public class JnlpMojo
    extends AbstractJnlpMojo
{
    /**
     * Get the executed project from the forked lifecycle.
     *
     * @parameter default-value="${executedProject}"
     * @required
     * @readonly
     */
    private MavenProject executedProject;

    public MavenProject getProject()
    {
        return executedProject;
    }
}

