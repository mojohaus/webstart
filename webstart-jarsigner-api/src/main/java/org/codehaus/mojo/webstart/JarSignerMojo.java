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

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Provides an abstraction for all Mojos that are capable of signing
 * jar files. Most use cases will involve signing jars with the keytool
 * provided with the java development kit. However, a particular enterprise
 * may require a different method of signing jars.
 * 
 * In a corporate environment, PCs typically have a company certificate
 * installed so that applications like Java Webstart know that when
 * code is signed by the company certificate it can be trusted. Companies
 * don't want to expose this certificate's private key to individual
 * developers. Instead, the company needs to keep the key secret and provide
 * a customised way of developers submitting jar files to be signed.
 * 
 * This interface allows enterprise users to develop a Mojo which doesn't
 * use the java keytool to sign jars. For example, the Maven Webstart
 * plugin signs jars as part of its operation. Using this interface, the
 * Webstart plugin is able to use pluggable jar signers (although it will
 * use JarSignMojo as a default) so that the user can plug in a custom
 * Mojo to sign their jars.
 */
public interface JarSignerMojo 
{
    /**
     * Sets the location of the unsigned jar file.
     * @param jarPath path to jar
     */
    void setJarPath( File jarPath );
    
    /**
     * Sets the output filename for the signed jar.
     * This may be the same location as specified in setJarPath(). If this
     * is the case, the unsigned jar file will be overwritten with the
     * signed jar file.
     * @param signedJar the signed jar
     */
    void setSignedJar( File signedJar );
    
    /**
     * Executes the jar signing process.
     * 
     * Same throws declaration as AbstractMojo.execute() 
     * @throws MojoExecutionException
     * @throws MojoFailureException
     */
    void execute() throws MojoExecutionException, MojoFailureException;
}
