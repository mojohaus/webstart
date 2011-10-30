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

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.shared.jarsigner.JarSignerRequest;
import org.apache.maven.shared.jarsigner.JarSignerVerifyRequest;

import java.io.File;

/**
 * Bean that represents the JarSigner configuration.
 *
 * @author <a href="jerome@coffeebreaks.org">Jerome Lacoste</a>
 * @version $Id$
 */
public interface SignConfig
{

    /**
     * Gets the verbose state of the configuration.
     *
     * @return {@code true} if configuration state is on, {@code false} otherwise.
     */
    boolean isVerbose();

    /**
     * Called before any Jars get signed or verified.
     * <p/>
     * This method allows you to create any keys or perform any initialisation that the
     * method of signature that you're implementing requires.
     *
     * @param log              logger injected from the mojos
     * @param workingDirectory working directory
     * @param verbose          verbose flag coming from the mojo configuration
     * @throws MojoExecutionException if something wrong occurs while init (mainly when preparing keys)
     */
    void init( Log log, File workingDirectory, boolean verbose )
        throws MojoExecutionException;

    /**
     * Creates a jarsigner request to do a sign operation.
     *
     * @param jarToSign the location of the jar to sign
     * @param signedJar the optional location of the signed jar to produce (if not set, will use the original location)
     * @return the jarsigner request
     */
    JarSignerRequest createSignRequest( File jarToSign, File signedJar );

    /**
     * Creates a jarsigner request to do a verify operation.
     *
     * @param jarFile the location of the jar to sign
     * @param certs   flag to show certificats details
     * @return the jarsigner request
     */
    JarSignerVerifyRequest createVerifyRequest( File jarFile, boolean certs );
}
