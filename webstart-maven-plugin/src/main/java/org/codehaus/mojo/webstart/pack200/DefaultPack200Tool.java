package org.codehaus.mojo.webstart.pack200;

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

import org.apache.maven.shared.utils.io.IOUtil;
import org.codehaus.plexus.component.annotations.Component;

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
import java.util.List;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Pack200;
import java.util.jar.Pack200.Packer;
import java.util.zip.Deflater;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Default implementation of the {@link Pack200Tool}.
 *
 * @author Tony Chemit - dev@tchemit.fr
 * @since 1.0-beta-2
 */
@Component( role = Pack200Tool.class, hint = "default" )
public class DefaultPack200Tool
        implements Pack200Tool
{

    public static final String PACK_GZ_EXTENSION = ".pack.gz";

    public static final String PACK_EXTENSION = ".pack";

    @Override
    public void pack( File source, File destination, Map<String, String> props, boolean gzip, boolean commonsCompress )
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

            if (commonsCompress) {
                org.apache.commons.compress.java.util.jar.Pack200.Packer packer = org.apache.commons.compress.java.util.jar.Pack200.newPacker();
                packer.properties().putAll( props );
                packer.pack( jar, out );
            } else {
                Pack200.Packer packer = Pack200.newPacker();
                packer.properties().putAll( props );
                packer.pack( jar, out );
            }
        }
        finally
        {
            IOUtil.close( out );
            if ( jar != null )
            {
                jar.close();
            }
        }
    }

    @Override
    public void repack( File source, File destination, Map<String, String> props, boolean commonsCompress )
        throws IOException
    {
        File tempFile = new File( source.toString() + ".tmp" );

        try
        {
            pack( source, tempFile, props, false, commonsCompress );
            unpack( tempFile, destination, props, commonsCompress );
        }
        finally
        {
            deleteFile( tempFile );
        }
    }

    @Override
    public void unpack( File source, File destination, Map<String, String> props, boolean commonsCompress )
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

            if (commonsCompress) {
                org.apache.commons.compress.java.util.jar.Pack200.Unpacker unpacker = org.apache.commons.compress.java.util.jar.Pack200.newUnpacker();
                unpacker.properties().putAll( props );
                unpacker.unpack( in, out );
            } else {
                Pack200.Unpacker unpacker = Pack200.newUnpacker();
                unpacker.properties().putAll( props );
                unpacker.unpack( in, out );
            }
        }
        finally
        {
            IOUtil.close( in );
            IOUtil.close( out );
        }
    }

    @Override
    public void packJars( File directory, FileFilter jarFileFilter, boolean gzip, List<String> passFiles, boolean commonsCompress )
            throws IOException
    {
        // getLog().debug( "packJars for " + directory );
        File[] jarFiles = directory.listFiles( jarFileFilter );
        for ( File jarFile1 : jarFiles )
        {
            // getLog().debug( "packJars: " + jarFiles[i] );

            final String extension = gzip ? PACK_GZ_EXTENSION : PACK_EXTENSION;

            File jarFile = jarFile1;

            File pack200Jar = new File( jarFile.getParentFile(), jarFile.getName() + extension );

            deleteFile( pack200Jar );

            Map<String, String> propMap = new HashMap<>();
            // Work around a JDK bug affecting large JAR files, see MWEBSTART-125
            propMap.put( Pack200.Packer.SEGMENT_LIMIT, String.valueOf( -1 ) );

            // set passFiles if available
            if ( passFiles != null && !passFiles.isEmpty() )
            {
                for ( int j = 0; j < passFiles.size(); j++ )
                {
                    propMap.put( Packer.PASS_FILE_PFX + j, passFiles.get( j ) );
                }
            }

            pack( jarFile, pack200Jar, propMap, gzip, commonsCompress );
            setLastModified( pack200Jar, jarFile.lastModified() );
        }
    }

    @Override
    public File packJar( File jarFile, boolean gzip, List<String> passFiles, boolean commonsCompress)
            throws IOException
    {
        final String extension = gzip ? PACK_GZ_EXTENSION : PACK_EXTENSION;

        File pack200Jar = new File( jarFile.getParentFile(), jarFile.getName() + extension );

        deleteFile( pack200Jar );

        Map<String, String> propMap = new HashMap<>();
        // Work around a JDK bug affecting large JAR files, see MWEBSTART-125
        propMap.put( Pack200.Packer.SEGMENT_LIMIT, String.valueOf( -1 ) );

        // set passFiles if available
        if ( passFiles != null && !passFiles.isEmpty() )
        {
            for ( int j = 0; j < passFiles.size(); j++ )
            {
                propMap.put( Packer.PASS_FILE_PFX + j, passFiles.get( j ) );
            }
        }

        pack( jarFile, pack200Jar, propMap, gzip, commonsCompress );
        setLastModified( pack200Jar, jarFile.lastModified() );
        return pack200Jar;
    }


    @Override
    public void unpackJars( File directory, FileFilter pack200FileFilter, boolean commonsCompress )
            throws IOException
    {
        // getLog().debug( "unpackJars for " + directory );
        File[] packFiles = directory.listFiles( pack200FileFilter );
        for ( File packFile : packFiles )
        {
            final String packedJarPath = packFile.getAbsolutePath();
            int extensionLength = packedJarPath.endsWith( PACK_GZ_EXTENSION ) ? 8 : 5;
            String jarFileName = packedJarPath.substring( 0, packedJarPath.length() - extensionLength );
            File jarFile = new File( jarFileName );

            deleteFile( jarFile );

            unpack( packFile, jarFile, Collections.<String, String>emptyMap(), commonsCompress );
            setLastModified( jarFile, packFile.lastModified() );
        }
    }

    @Override
    public File unpackJar( File packFile, boolean commonsCompress )
            throws IOException
    {
        final String packedJarPath = packFile.getAbsolutePath();
        int extensionLength = packedJarPath.endsWith( PACK_GZ_EXTENSION ) ? 8 : 5;
        String jarFileName = packedJarPath.substring( 0, packedJarPath.length() - extensionLength );
        File jarFile = new File( jarFileName );

        deleteFile( jarFile );

        unpack( packFile, jarFile, Collections.<String, String>emptyMap(), commonsCompress );
        setLastModified( jarFile, packFile.lastModified() );
        return jarFile;
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
     * @param file the file to test
     * @return {@code true} if the specified file is gzipped.
     * @throws IOException if any error while testing file
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
