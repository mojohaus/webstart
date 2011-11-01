package org.codehaus.mojo.webstart.sign;

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
import org.apache.maven.shared.jarsigner.JarSignerRequest;
import org.apache.maven.shared.jarsigner.JarSignerVerifyRequest;
import org.codehaus.mojo.shared.keytool.KeyToolGenKeyRequest;

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
     * @param workingDirectory working directory
     * @param verbose          verbose flag coming from the mojo configuration
     * @param signTool         the sign tool used eventually to create or delete key store
     * @throws MojoExecutionException if something wrong occurs while init (mainly when preparing keys)
     */
    void init( File workingDirectory, boolean verbose, SignTool signTool )
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

    /**
     * Creates a keytool request to do a key store generation operation.
     *
     * @param keystoreFile the location of the key store file to generate
     * @return the keytool request
     */
    KeyToolGenKeyRequest createKeyGenRequest( File keystoreFile );

    void setAlias( String alias );

    void setDnameCn( String dnameCn );

    void setDnameOu( String dnameOu );

    void setDnameL( String dnameL );

    void setDnameSt( String dnameSt );

    void setDnameO( String dnameO );

    void setDnameC( String dnameC );

    void setKeypass( String keypass );

    void setKeystore( String keystore );

    void setStorepass( String storepass );

    void setVerify( boolean verify );

    void setValidity( String validity );

    void setMaxMemory( String maxMemory );

    void setKeystoreConfig( KeystoreConfig keystoreConfig );

    void setKeyalg( String keyalg );

    void setKeysize( String keysize );

    void setSigalg( String sigalg );

    void setSigfile( String sigfile );

    void setStoretype( String storetype );

}
