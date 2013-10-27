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

import java.io.File;

/**
 * Tool api for jarsigner operations.
 *
 * @author tchemit <chemit@codelutin.com>
 * @since 1.0-beta-3
 */
public interface SignTool
{

    /**
     * Plexus component role.
     */
    String ROLE = SignTool.class.getName();

    /**
     * Obtain the location of the given keystore.
     * <p/>
     * If the keystore is a file then just return it, otherwise if is a resource from class path or a valid url,
     * then copy the resource to given working keystore location.
     *
     * @param keystore        keystore location to find
     * @param workingKeystore location where to copy keystore if coming from an url or from classpath
     * @param classLoader     classloader where to find keystore in classpath
     * @return the file location of the keystore saved if required in working directory or {@code null} if could not
     * locate keystore.
     * @throws MojoExecutionException if something wrong occurs
     * @since 1.0-beta-4
     */
    File getKeyStoreFile( String keystore, File workingKeystore, ClassLoader classLoader )
        throws MojoExecutionException;

    /**
     * Generate a key store using keytool.
     *
     * @param config       sign configuration
     * @param keystoreFile location of the keystore to generate
     * @throws MojoExecutionException if something wrong occurs
     */
    void generateKey( SignConfig config, File keystoreFile )
        throws MojoExecutionException;

    /**
     * Sign a jar using jarsigner.
     *
     * @param config    sign configuration
     * @param jarFile   location of the jar to sign
     * @param signedJar optional location of the signed jar to produce (if not set, will use the original location)
     * @throws MojoExecutionException if something wrong occurs
     */
    void sign( SignConfig config, File jarFile, File signedJar )
        throws MojoExecutionException;

    /**
     * Verify a jar file using jarsigner.
     *
     * @param config  sign configuration
     * @param jarFile location of the jar to sign
     * @param certs   flag to show certificats details
     * @throws MojoExecutionException if something wrong occurs
     */
    void verify( SignConfig config, File jarFile, boolean certs )
        throws MojoExecutionException;

    /**
     * Tests if the given jar is signed.
     *
     * @param jarFile the jar file to test
     * @return {@code true} if jar file is signed, {@code false} otherwise
     * @throws MojoExecutionException if something wrong occurs
     * @since 1.0-beta-4
     */
    boolean isJarSigned( File jarFile )
        throws MojoExecutionException;

    /**
     * Unsign a jar.
     *
     * @param jarFile location of the jar to unsign
     * @param verbose flag to display verbose logs
     * @throws MojoExecutionException if something wrong occurs
     * @since 1.0-beta-4
     */
    void unsign( File jarFile, boolean verbose )
        throws MojoExecutionException;

    /**
     * Delete an existing key store
     *
     * @param keystore the keystore to delete
     * @param verbose  flag to display verbose logs
     */
    void deleteKeyStore( File keystore, boolean verbose );
}
