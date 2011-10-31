package org.codehaus.mojo.webstart;

/*
 * Copyright 2001-2007 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.embedder.MavenEmbedder;
import org.apache.maven.embedder.MavenEmbedderConsoleLogger;
import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;

/**
 * This is more an integration test as we exercise both the signing and unsigning operations
 * on existing jars
 *
 * @author Jerome Lacoste <jerome@coffeebreaks.org>
 * @version $Id$
 */
public class JarUnsignMojoTest
    extends PlexusTestCase
{
    private JarUnsignMojo mojo;

    private File tempdir;

    private SignConfig sign;

    private MavenEmbedder embedder;

    /**
     * JarSigner tool.
     *
     * @plexus.requirement role="org.apache.maven.shared.jarsigner.JarSigner"
     */
    private SignTool signTool;

    public void setUp()
        throws Exception
    {
        super.setUp();

        embedder = new MavenEmbedder();

        embedder.setClassLoader( Thread.currentThread().getContextClassLoader() );
        embedder.setLogger( new MavenEmbedderConsoleLogger() );
        embedder.start();

        tempdir = new File( System.getProperty( "java.io.tmpdir" ) + File.separator + System.nanoTime() );
        FileUtils.mkdir( tempdir.getAbsolutePath() );

        File unsignTempDir = new File( tempdir, "unsign" );

        FileUtils.deleteDirectory( unsignTempDir );

        mojo = new JarUnsignMojo();
        mojo.setTempDir( unsignTempDir );
        mojo.setVerbose( false );

        signTool = (SignTool) lookup( SignTool.ROLE );
        mojo.setSignTool( signTool );

        File keystore = new File( tempdir, "keystore" );

        sign = new DefaultSignConfig();
        sign.setAlias( "test" );
        sign.setKeypass( "123456" );
        sign.setKeystore( keystore.getAbsolutePath() );
        sign.setStorepass( "123456" );
        sign.setVerify( false );

        sign.setDnameCn( "CN" );
        sign.setDnameOu( "OU" );
        sign.setDnameL( "L" );
        sign.setDnameSt( "ST" );
        sign.setDnameO( "O" );
        sign.setDnameC( "C" );

        KeystoreConfig keystoreConfig = new KeystoreConfig();
        keystoreConfig.setDelete( true );
        keystoreConfig.setGen( true );
        sign.setKeystoreConfig( keystoreConfig );

        sign.init( tempdir, false, signTool );
    }

    public void tearDown()
        throws Exception
    {
        mojo = null;
        signTool = null;
        super.tearDown();
    }

    public void testAddThenRemoveSignatureCheckUsingJarSignVerifyMojo()
        throws Exception
    {
        File unsignedJar = getUnsignedJarFile();

        ensureJarSignedOrNot( unsignedJar, false, "initial jar must be unsigned" );

        FileUtils.copyFileToDirectory( unsignedJar, tempdir );

        File copiedJar = new File( tempdir, unsignedJar.getName() );

        signJar( copiedJar );

        ensureJarSignedOrNot( copiedJar, true, "now jar has been successfully signed" );

        mojo.setJarPath( copiedJar );

        mojo.execute();

        ensureJarSignedOrNot( copiedJar, false, "Now jar must be unsigned" );
    }

    private File getUnsignedJarFile()
        throws Exception
    {
        ArtifactRepository localRepository = embedder.getLocalRepository();
        Artifact junit =
            new DefaultArtifact( "junit", "junit", VersionRange.createFromVersion( "3.8.1" ), "test", "jar", "",
                                 new DefaultArtifactHandler( "" ) );

        return new File( localRepository.getBasedir() + "/" + localRepository.pathOf( junit ) + ".jar" );
    }

    private void signJar( File jarToSign )
        throws MojoExecutionException
    {

        signTool.sign( sign, jarToSign, null );
    }

    private void ensureJarSignedOrNot( File jarFile, boolean signed, String msg )
        throws MojoExecutionException
    {
        assertTrue( "jar file exists", jarFile.exists() );
        boolean isSigned = signTool.isJarSigned( sign, jarFile );
        assertEquals( msg, signed, isSigned );
    }
}
