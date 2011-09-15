/*
 * $Id: Unpack200Task.java,v 1.2 2005/11/17 05:30:03 evickroy Exp $
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

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.Copy;
import org.apache.tools.ant.util.GlobPatternMapper;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;

/**
 * Ant Task performing the unpack200 operation on a single file or on a fileset.
 *
 * @author Kumar Srinivasan
 * @author Emmanuel Bourg
 * @version $Revision$, $Date$
 */
public class Unpack200Task
    extends Copy
{

    /**
     * The file to unpack.
     *
     * @param src file to unpack
     * @deprecated use setFile() instead
     */
    public void setSrc( File src )
    {
        setFile( src );
    }

    /**
     * The destination file or directory.
     *
     * @param dest destination file or directory
     * @deprecated use setTofile() or setTodir() instead
     */
    public void setDest( File dest )
    {
        if ( dest.isDirectory() )
        {
            setTodir( dest );
        }
        else
        {
            setTofile( dest );
        }
    }

    public void execute()
    {
        // define the default mapper if none is specified
        if ( mapperElement == null )
        {
            createMapper();

            GlobPatternMapper mapper = new GlobPatternMapper();
            mapper.setFrom( "*.jar" );
            mapper.setTo( "*.jar" );

            mapperElement.add( mapper );

            mapper = new GlobPatternMapper();
            mapper.setFrom( "*.jar.pack" );
            mapper.setTo( "*.jar" );

            mapperElement.add( mapper );

            mapper = new GlobPatternMapper();
            mapper.setFrom( "*.jar.pack.gz" );
            mapper.setTo( "*.jar" );

            mapperElement.add( mapper );
        }

        super.execute();
    }

    protected void doFileOperations()
    {
        if ( fileCopyMap.size() > 0 )
        {
            log( "Unpacking " + fileCopyMap.size() + " file" + ( fileCopyMap.size() == 1 ? "" : "s" ) + " to " +
                     destDir.getAbsolutePath() );

            Enumeration e = fileCopyMap.keys();
            while ( e.hasMoreElements() )
            {
                String fromFile = (String) e.nextElement();
                String[] toFiles = (String[]) fileCopyMap.get( fromFile );
                for ( int i = 0; i < toFiles.length; i++ )
                {
                    String toFile = toFiles[i];
                    if ( fromFile.equals( toFile ) )
                    {
                        log( "Skipping self-unpack of " + fromFile, verbosity );
                        continue;
                    }
                    try
                    {
                        log( "Unpacking " + fromFile + " to " + toFile, verbosity );

                        PackUtils.unpack( new File( fromFile ), new File( toFile ), Collections.emptyMap() );

                    }
                    catch ( IOException ioe )
                    {
                        String msg = "Failed to unpack " + fromFile + " to " + toFile + " due to " + ioe.getMessage();
                        File targetFile = new File( toFile );
                        if ( targetFile.exists() && !targetFile.delete() )
                        {
                            msg += " and I couldn't delete the corrupt " + toFile;
                        }
                        throw new BuildException( msg, ioe, getLocation() );
                    }
                }
            }
        }
    }
}
