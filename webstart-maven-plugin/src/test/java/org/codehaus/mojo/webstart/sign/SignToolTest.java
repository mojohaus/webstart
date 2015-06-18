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

import org.apache.commons.lang.SystemUtils;
import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.io.IOException;

/**
 * Tests the {@link SignToolTest}.
 *
 * @author tchemit <chemit@codelutin.com>
 * @since 1.1
 */
public class SignToolTest
    extends PlexusTestCase
{

    protected SignTool signTool;

    public void setUp()
        throws Exception
    {
        super.setUp();
        signTool = (SignTool) lookup( SignTool.class.getName() );
    }

    public void testIsJarSigned()
        throws Exception
    {

        // -- test with an unsigned jar -- //
        File unsignedTarget = new File( "target/SignToolTest-IsJarSigned/simple.jar" );

        copyTestFile( new File( "src/test/simple.jar" ), unsignedTarget );

        // jar is not signed
        assertFalse( signTool.isJarSigned( unsignedTarget ) );

        // -- test with an signed jar -- //

        File signedTarget = new File( "target/SignToolTest-IsJarSigned/simpleSignedLowerCase.jar" );

        copyTestFile( new File( "src/test/simpleSignedLowerCase.jar" ), signedTarget );

        // jar is signed
        assertTrue( signTool.isJarSigned( signedTarget ) );
    }

    public void testUnsignArchiveWithLowerExtensionNames()
        throws Exception
    {

        // usign an already signed jar but with some signed files with mixed extension case (.Sf, .das)

        File signedTarget = new File( "target/SignToolTest/simpleSignedLowerCase.jar" );

        copyTestFile( new File( "src/test/simpleSignedLowerCase.jar" ), signedTarget );

        // jar is signed
        assertTrue( signTool.isJarSigned( signedTarget ) );

        signTool.unsign( signedTarget, true );

        // jar is now unsigned
        assertFalse( signTool.isJarSigned( signedTarget ) );
    }

    public void testGetKeyStoreFile()
        throws Exception
    {

        File tmpDir = SystemUtils.getJavaIoTmpDir();

        File parentDir = new File( tmpDir, "tmp" );
        File keyStoreFile;

        ClassLoader classLoader = getClassLoader();

        // from classpath with / start
        keyStoreFile =
            signTool.getKeyStoreFile( "classpath:/test/myfile.txt", new File( tmpDir, "myfile2.txt" ), classLoader );
        assertNotNull( keyStoreFile );
        assertEquals( "myfile2.txt", keyStoreFile.getName() );
        assertEquals( tmpDir, keyStoreFile.getParentFile() );

        // from classpath
        keyStoreFile =
            signTool.getKeyStoreFile( "classpath:test/myfile.txt", new File( tmpDir, "myfile2.txt" ), classLoader );
        assertNotNull( keyStoreFile );
        assertEquals( "myfile2.txt", keyStoreFile.getName() );
        assertEquals( tmpDir, keyStoreFile.getParentFile() );

        // from a direct file (no change)
        keyStoreFile = signTool.getKeyStoreFile( keyStoreFile.getAbsolutePath(), new File( parentDir, "myfile3.txt" ),
                                                 classLoader );
        assertNotNull( keyStoreFile );
        assertEquals( "myfile2.txt", keyStoreFile.getName() );
        assertEquals( tmpDir, keyStoreFile.getParentFile() );

        //from a url (from a file)
        keyStoreFile = signTool.getKeyStoreFile( keyStoreFile.toURI().toString(), new File( parentDir, "myfile3.txt" ),
                                                 classLoader );
        assertNotNull( keyStoreFile );
        assertEquals( "myfile3.txt", keyStoreFile.getName() );
        assertEquals( parentDir, keyStoreFile.getParentFile() );
    }

    protected void copyTestFile( File file, File target )
        throws IOException
    {
        if ( target.exists() )
        {
            FileUtils.forceDelete( target );
        }

        FileUtils.copyFile( file, target );

    }

}
