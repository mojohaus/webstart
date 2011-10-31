/*
 * $Id$
 *
 * Copyright 2004 Sun Microsystems, Inc., 4150 Network Circle,
 * Santa Clara, California 95054, U.S.A. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package org.jdesktop.deployment.ant.pack200;

import org.apache.tools.ant.util.FileUtils;

import java.io.*;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Pack200;
import java.util.zip.Deflater;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Utility methods for packing and unpacking jar files.
 *
 * @author Emmanuel Bourg
 * @version $Revision$, $Date$
 */
public class PackUtils
{

    /**
     * Pack a jar.
     *
     * @param source      the source jar
     * @param destination the packed jar
     * @param props       the packing properties
     * @param gzip        true if the destination file
     */
    public static void pack( File source, File destination, Map props, boolean gzip )
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
     * Repack a jar.
     *
     * @param source      the source jar
     * @param destination the destination jar (may be the same as the source jar)
     * @param props       the packing properties
     */
    public static void repack( File source, File destination, Map props )
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
            tempFile.delete();
        }
    }

    /**
     * Unpack a jar.
     *
     * @param source      the packed jar
     * @param destination the unpacked jar
     * @param props       the packing properties
     */
    public static void unpack( File source, File destination, Map props )
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
     * Tells if the specified file is gzipped.
     *
     * @param file
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
