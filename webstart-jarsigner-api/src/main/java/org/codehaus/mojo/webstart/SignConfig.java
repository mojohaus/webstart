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

import java.io.File;

import org.apache.maven.plugin.logging.Log;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Bean that represents the JarSigner configuration.
 *
 * @author <a href="jerome@coffeebreaks.org">Jerome Lacoste</a>
 * @version $Id$
 */
public interface SignConfig 
{

    /**
     * Returns a fully configured version of a Mojo ready to sign jars.
     * 
     * @return
     * @throws MojoExecutionException
     * @throws MojoFailureException
     */
    JarSignerMojo getJarSignerMojo() 
        throws MojoExecutionException, MojoFailureException;
    

    /**
     * Called before any Jars get signed. This method allows you to
     * create any keys or perform any initialisation that the
     * method of signature that you're implementing requires.
     * 
     * @param log
     * @param workingDirectory
     * @param verbose
     * @throws MojoExecutionException
     * @throws MojoFailureException
     */
    void init( Log log, File workingDirectory, boolean verbose )
        throws MojoExecutionException, MojoFailureException;
}
