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

import org.apache.tools.ant.util.FileUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Pack200;
import java.util.zip.Deflater;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Default implementation of the {@link Pack200Tool}.
 *
 * @author tchemit <chemit@codelutin.com>
 * @plexus.component role-hint="default"
 * @since 1.0-beta-2
 */
public class DefaultPack200Tool
    implements Pack200Tool
{
    /**
     * {@inheritDoc}
     */
    public void pack( File source, File destination, Map props, boolean gzip )
        throws IOException
    {
        JarFile jar = null;
        OutputStream out = null;
        try
        {
            out = new FileOutputStream( destination );
            if ( gzip )
            {
                out = new GZIPOutputStream( out )
                {
                    {
                        def.setLevel( Deflater.BEST_COMPRESSION );
                    }
                };
            }
            out = new BufferedOutputStream( out );

            jar = new JarFile( source, false );

            Pack200.Packer packer = Pack200.newPacker();
            packer.properties().putAll( props );
            packer.pack( jar, out );
        }
        finally
        {
            FileUtils.close( out );
            if ( jar != null )
            {
                jar.close();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void repack( File source, File destination, Map props )
        throws IOException
    {
        File tempFile = new File( source.toString() + ".tmp" );

        try
        {
            pack( source, tempFile, props, false );
            unpack( tempFile, destination, props );
        }
        finally
        {
            deleteFile( tempFile );
        }
    }

    /**
     * {@inheritDoc}
     */
    public void unpack( File source, File destination, Map props )
        throws IOException
    {
        InputStream in = null;
        JarOutputStream out = null;
        try
        {
            in = new FileInputStream( source );
            if ( isGzipped( source ) )
            {
                in = new GZIPInputStream( in );
            }
            in = new BufferedInputStream( in );

            out = new JarOutputStream( new BufferedOutputStream( new FileOutputStream( destination ) ) );

            Pack200.Unpacker unpacker = Pack200.newUnpacker();
            unpacker.properties().putAll( props );
            unpacker.unpack( in, out );
        }
        finally
        {
            FileUtils.close( in );
            FileUtils.close( out );
        }
    }

    /**
     * {@inheritDoc}
     */
    public void packJars( File directory, FileFilter jarFileFilter, boolean gzip )
        throws IOException
    {
        // getLog().debug( "packJars for " + directory );
        File[] jarFiles = directory.listFiles( jarFileFilter );
        for ( File jarFile1 : jarFiles )
        {
            // getLog().debug( "packJars: " + jarFiles[i] );

            final String extension = gzip ? ".pack.gz" : ".pack";

            File jarFile = jarFile1;

            File pack200Jar = new File( jarFile.getParentFile(), jarFile.getName() + extension );

            deleteFile( pack200Jar );

            Map propMap = new HashMap();
            // Work around a JDK bug affecting large JAR files, see MWEBSTART-125
            propMap.put( Pack200.Packer.SEGMENT_LIMIT, String.valueOf( -1 ) );

            pack( jarFile, pack200Jar, propMap, gzip );
            setLastModified( pack200Jar, jarFile.lastModified() );
        }
    }

    /**
     * {@inheritDoc}
     */
    public void unpackJars( File directory, FileFilter pack200FileFilter )
        throws IOException
    {
        // getLog().debug( "unpackJars for " + directory );
        File[] packFiles = directory.listFiles( pack200FileFilter );
        for ( File packFile : packFiles )
        {
            final String packedJarPath = packFile.getAbsolutePath();
            int extensionLength = packedJarPath.endsWith( ".jar.pack.gz" ) ? 8 : 5;
            String jarFileName = packedJarPath.substring( 0, packedJarPath.length() - extensionLength );
            File jarFile = new File( jarFileName );

            deleteFile( jarFile );

            unpack( packFile, jarFile, Collections.emptyMap() );
            setLastModified( jarFile, packFile.lastModified() );
        }
    }

    private void deleteFile( File file )
        throws IOException
    {
        if ( file.exists() )
        {
            boolean delete = file.delete();
            if ( !delete )
            {
                throw new IOException( "Could not delete file " + file );
            }
        }
    }

    private void setLastModified( File file, long modifi )
        throws IOException
    {
        boolean b = file.setLastModified( modifi );
        if ( !b )
        {
            throw new IOException( "Could not change last modifified on file: " + file );
        }
    }

    /**
     * Tells if the specified file is gzipped.
     *
     * @param file the file to test
     */
    private static boolean isGzipped( File file )
        throws IOException
    {
        DataInputStream is = new DataInputStream( new FileInputStream( file ) );
        int i = is.readInt();
        is.close();
        return ( i & 0xffffff00 ) == 0x1f8b0800;
    }
}
