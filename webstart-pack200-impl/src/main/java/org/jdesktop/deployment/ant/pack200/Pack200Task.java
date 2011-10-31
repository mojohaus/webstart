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

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.Copy;
import org.apache.tools.ant.util.FileUtils;
import org.apache.tools.ant.util.GlobPatternMapper;

import java.io.*;
import java.util.*;
import java.util.jar.Pack200.Packer;

/**
 * Ant Task performing the pack200 operation on a single file or on a fileset.
 *
 * @author Kumar Srinivasan
 * @author Emmanuel Bourg
 * @version $Revision$, $Date$
 */
public class Pack200Task
    extends Copy
{

    public static final List MODIFICATION_TIME_VALUES;

    public static final List DEFLATE_HINT_VALUES;

    public static final List UNKNOWN_ATTRIBUTE_VALUES;

    static
    {
        MODIFICATION_TIME_VALUES = new ArrayList();
        MODIFICATION_TIME_VALUES.add( "latest" );
        MODIFICATION_TIME_VALUES.add( "keep" );
        DEFLATE_HINT_VALUES = new ArrayList();
        DEFLATE_HINT_VALUES.add( "true" );
        DEFLATE_HINT_VALUES.add( "false" );
        DEFLATE_HINT_VALUES.add( "keep" );
        UNKNOWN_ATTRIBUTE_VALUES = new ArrayList();
        UNKNOWN_ATTRIBUTE_VALUES.add( "error" );
        UNKNOWN_ATTRIBUTE_VALUES.add( "strip" );
        UNKNOWN_ATTRIBUTE_VALUES.add( "pass" );
    }

    private boolean repack;

    private boolean gzip;

    private File config;

    /**
     * Storage for the properties used by the setters.
     */
    private Map/*<String, String>*/ propMap = new HashMap/*<String, String>*/();

    protected void validateAttributes()
    {
        super.validateAttributes();

        if ( config != null )
        {
            if ( config.isDirectory() )
            {
                throw new BuildException( "The pack200 property file specified is a directory: " + config,
                                          getLocation() );
            }
            else if ( !config.exists() )
            {
                throw new BuildException( "The pack200 property file specified doesn't exist: " + config,
                                          getLocation() );
            }

            // load the properties from the configuration file
            Properties properties = getPackProperties();
            
            Iterator iterator = properties.entrySet().iterator();
            while ( iterator.hasNext() )
            {
                Object next = iterator.next();
                Map.Entry entry = (Map.Entry) next;
                propMap.put( entry.getKey(), entry.getValue() );
            }
        }

        // validate the modification time
        String modificationTime = (String) propMap.get( Packer.MODIFICATION_TIME );
        if ( modificationTime != null && !MODIFICATION_TIME_VALUES.contains( modificationTime ) )
        {
            throw new BuildException( modificationTime + " is not a legal value for the modificationtime attribute",
                                      getLocation() );
        }

        // validate the deflate hint
        String deflateHint = (String) propMap.get( Packer.DEFLATE_HINT );
        if ( deflateHint != null && !DEFLATE_HINT_VALUES.contains( deflateHint ) )
        {
            throw new BuildException( deflateHint + " is not a legal value for the deflatehint attribute",
                                      getLocation() );
        }

        // validate the unknown attribute
        String unknownAttribute = (String) propMap.get( Packer.UNKNOWN_ATTRIBUTE );
        if ( unknownAttribute != null && !UNKNOWN_ATTRIBUTE_VALUES.contains( unknownAttribute ) )
        {
            throw new BuildException( deflateHint + " is not a legal value for the unknownattribute attribute",
                                      getLocation() );
        }
    }

    /**
     * Set the file to pack. This method remains to maintain
     * the backward compatibility. The setFile() method is preferred.
     *
     * @param src the source file
     * @deprecated Use setFile() instead
     */
    public void setSrc( File src )
    {
        setFile( src );
    }

    /**
     * Set the destination file. This method remains to maintain
     * the backward compatibility. The setTofile() method is preferred.
     *
     * @param destFile the destination file
     * @deprecated Use setTofile() instead
     */
    public void setDestfile( File destFile )
    {
        setTofile( destFile );
    }

    /**
     * Sets the repack option, ie the jar will be packed and repacked.
     */
    public void setRepack( boolean value )
    {
        repack = value;
    }

    /**
     * Sets whether the pack archive is additionally deflated with gzip.
     */
    public void setGZIPOutput( boolean value )
    {
        gzip = value;
    }

    /**
     * Sets whether the Java debug attributes should be stripped
     */
    public void setStripDebug( boolean value )
    {
        propMap.put( "com.sun.java.util.jar.pack.strip.debug", String.valueOf( value ) );
    }

    /**
     * Sets the modification time for the archive
     */
    public void setModificationTime( String value )
    {
        propMap.put( Packer.MODIFICATION_TIME, value );
    }

    /**
     * Sets the deflate hint for the archive
     */
    public void setDeflateHint( String value )
    {
        propMap.put( Packer.DEFLATE_HINT, value );
    }

    /**
     * Sets the file ordering.
     */
    public void setKeepFileOrder( boolean value )
    {
        propMap.put( Packer.KEEP_FILE_ORDER, String.valueOf( value ) );
    }

    /**
     * Sets the segment limit.
     */
    public void setSegmentLimit( int value )
    {
        propMap.put( Packer.SEGMENT_LIMIT, String.valueOf( value ) );
    }

    /**
     * Sets the effort.
     */
    public void setEffort( int value )
    {
        propMap.put( Packer.EFFORT, String.valueOf( value ) );
    }

    /**
     * Sets the action to be taken if an unknown attribute is encountered.
     */
    public void setUnknownAttribute( String value )
    {
        propMap.put( Packer.UNKNOWN_ATTRIBUTE, value );
    }

    /**
     * Useful to set those Pack200 attributes which are not commonly used.
     */
    public void setConfigFile( File config )
    {
        this.config = config;
    }

    public void execute()
    {
        // define the default mapper if none is specified
        if ( mapperElement == null && !repack )
        {
            GlobPatternMapper mapper = new GlobPatternMapper();
            mapper.setFrom( "*.jar" );
            mapper.setTo( "*.jar.pack" + ( gzip ? ".gz" : "" ) );
            add( mapper );
        }

        super.execute();
    }

    /**
     * Returns the packing properties from the property file if specified.
     */
    protected Properties getPackProperties()
    {
        Properties properties = new Properties();

        InputStream in = null;
        try
        {
            in = new BufferedInputStream( new FileInputStream( config ) );
            properties.load( in );
        }
        catch ( IOException e )
        {
            throw new BuildException( "Could not load the pack200 properties file: " + config, e, getLocation() );
        }
        finally
        {
            FileUtils.close( in );
        }

        return properties;
    }

    protected void doFileOperations()
    {
        if ( fileCopyMap.size() > 0 )
        {
            log( ( repack ? "Repacking " : "Packing " ) + fileCopyMap.size() + " file" +
                     ( fileCopyMap.size() == 1 ? "" : "s" ) + " to " + destDir.getAbsolutePath() );

            Enumeration e = fileCopyMap.keys();
            while ( e.hasMoreElements() )
            {
                String fromFile = (String) e.nextElement();
                String[] toFiles = (String[]) fileCopyMap.get( fromFile );
                for ( int i = 0; i < toFiles.length; i++ )
                {
                    String toFile = toFiles[i];
                    if ( !repack && fromFile.equals( toFile ) )
                    {
                        log( "Skipping self-pack of " + fromFile, verbosity );
                        continue;
                    }
                    try
                    {
                        log( ( repack ? "Repacking " : "Packing " ) + fromFile + " to " + toFile, verbosity );

                        if ( repack )
                        {
                            PackUtils.repack( new File( fromFile ), new File( toFile ), propMap );
                        }
                        else
                        {
                            boolean gzip = this.gzip || toFile.endsWith( ".gz" );
                            PackUtils.pack( new File( fromFile ), new File( toFile ), propMap, gzip );
                        }
                    }
                    catch ( IOException ioe )
                    {
                        String msg = "Failed to pack " + fromFile + " to " + toFile + " due to " + ioe.getMessage();
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
