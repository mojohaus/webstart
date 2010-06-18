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

import com.sun.tools.apache.ant.pack200.Pack200Task;
import com.sun.tools.apache.ant.pack200.Unpack200Task;
import org.apache.tools.ant.Project;

import java.io.File;
import java.io.FileFilter;

/**
 * Handles pack200 operations.
 *
 * @author <a href="jerome@coffeebreaks.org">Jerome Lacoste</a>
 */
public class Pack200
{

    public static void packJars( File directory, FileFilter jarFileFilter, boolean gzip )
    {
        // getLog().debug( "packJars for " + directory );
        Pack200Task packTask;
        File[] jarFiles = directory.listFiles( jarFileFilter );
        for ( int i = 0; i < jarFiles.length; i++ )
        {
            // getLog().debug( "packJars: " + jarFiles[i] );

            final String extension = gzip ? ".pack.gz" : ".pack";

            File pack200Jar = new File( jarFiles[i].getParentFile(), jarFiles[i].getName() + extension );

            if ( pack200Jar.exists() )
            {
                pack200Jar.delete();
            }

            packTask = new Pack200Task();
            packTask.setProject( new Project() );
            packTask.setDestfile( pack200Jar );
            packTask.setSrc( jarFiles[i] );
            packTask.setGZIPOutput( gzip );

            // Work around a JDK bug affecting large JAR files, see MWEBSTART-125
            packTask.setSegmentLimit( "-1" );

            packTask.execute();
            pack200Jar.setLastModified( jarFiles[i].lastModified() );
        }
    }

    public static void unpackJars( File directory, FileFilter pack200FileFilter )
    {
        // getLog().debug( "unpackJars for " + directory );
        Unpack200Task unpackTask;
        File[] packFiles = directory.listFiles( pack200FileFilter );
        for ( int i = 0; i < packFiles.length; i++ )
        {
            final String packedJarPath = packFiles[i].getAbsolutePath();
            int extensionLength = packedJarPath.endsWith( ".jar.pack.gz" ) ? 8 : 5;
            String jarFileName = packedJarPath.substring( 0, packedJarPath.length() - extensionLength );
            File jarFile = new File( jarFileName );

            if ( jarFile.exists() )
            {
                jarFile.delete();
            }
            unpackTask = new Unpack200Task();
            unpackTask.setProject( new Project() );
            unpackTask.setDest( jarFile );
            unpackTask.setSrc( packFiles[i] );
            unpackTask.execute();
            jarFile.setLastModified( packFiles[i].lastModified() );
        }
    }
}
