package org.codehaus.mojo.webstart.util;

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
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.List;

/**
 * Helper for all IO operations.
 *
 * @author tchemit <chemit@codelutin.com>
 * @plexus.component role-hint="default"
 * @since 1.0-beta-4
 */
public class DefaultIOUtil
    extends AbstractLogEnabled
    implements IOUtil
{

    /**
     * {@inheritDoc}
     */
    public void copyResources( File resourcesDir, File workDirectory )
        throws IOException, MojoExecutionException
    {
        if ( !resourcesDir.exists() && getLogger().isInfoEnabled() )
        {
            getLogger().info( "No resources found in " + resourcesDir.getAbsolutePath() );
        }
        else
        {
            if ( !resourcesDir.isDirectory() )
            {
                getLogger().debug( "Not a directory: " + resourcesDir.getAbsolutePath() );
            }
            else
            {
                getLogger().debug( "Copying resources from " + resourcesDir.getAbsolutePath() );

                // hopefully available from FileUtils 1.0.5-SNAPSHOT
                //FileUtils.copyDirectoryStructure( resourcesDir , workDirectory );

                // this may needs to be parametrized somehow
                String excludes = concat( DirectoryScanner.DEFAULTEXCLUDES, ", " );
                copyDirectoryStructure( resourcesDir, workDirectory, "**", excludes );
            }

        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean copyFileToDirectoryIfNecessary( File sourceFile, File targetDirectory )
        throws IOException
    {

        if ( sourceFile == null )
        {
            throw new IllegalArgumentException( "sourceFile is null" );
        }

        File targetFile = new File( targetDirectory, sourceFile.getName() );

        boolean shouldCopy = !targetFile.exists() || ( targetFile.lastModified() < sourceFile.lastModified() );

        if ( shouldCopy )
        {
            FileUtils.copyFileToDirectory( sourceFile, targetDirectory );
        }
        else
        {
            getLogger().debug(
                "Source file hasn't changed. Do not overwrite " + targetFile + " with " + sourceFile + "." );

        }

        return shouldCopy;
    }

    /**
     * {@inheritDoc}
     */
    public void removeDirectory( File dir )
        throws MojoExecutionException
    {
        if ( dir != null )
        {
            if ( dir.exists() && dir.isDirectory() )
            {
                getLogger().debug( "Deleting directory " + dir.getAbsolutePath() );
                try
                {
                    FileUtils.deleteDirectory( dir );
                }
                catch ( IOException e )
                {
                    throw new MojoExecutionException( "Could not delete directory: " + dir, e );
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void makeDirectoryIfNecessary( File dir )
        throws MojoExecutionException
    {

        if ( !dir.exists() && !dir.mkdirs() )
        {
            throw new MojoExecutionException( "Failed to create directory: " + dir );
        }

    }

    /**
     * {@inheritDoc}
     */
    public int deleteFiles( File directory, FileFilter fileFilter )
        throws MojoExecutionException
    {
        File[] files = directory.listFiles( fileFilter );

        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "deleteFiles in " + directory + " found " + files.length + " file(s) to delete" );
        }

        if ( files.length == 0 )
        {
            return 0;
        }

        for ( File file : files )
        {
            deleteFile( file );
        }
        return files.length;
    }

    /**
     * {@inheritDoc}
     */
    public void deleteFile( File file )
        throws MojoExecutionException
    {
        if ( file.exists() && !file.delete() )
        {
            throw new MojoExecutionException( "Could not delete file: " + file );
        }
    }

    /**
     * {@inheritDoc}
     */
    public void renameTo( File source, File target )
        throws MojoExecutionException
    {
        boolean result = source.renameTo( target );
        if ( !result )
        {
            throw new MojoExecutionException( "Could not rename " + source + " to " + target );
        }
    }

    private void copyDirectoryStructure( File sourceDirectory, File destinationDirectory, String includes,
                                         String excludes )
        throws IOException, MojoExecutionException
    {
        if ( !sourceDirectory.exists() )
        {
            return;
        }

        List<File> files = FileUtils.getFiles( sourceDirectory, includes, excludes );

        for ( File file : files )
        {

            getLogger().debug( "Copying " + file + " to " + destinationDirectory );

            String path = file.getAbsolutePath().substring( sourceDirectory.getAbsolutePath().length() + 1 );

            File destDir = new File( destinationDirectory, path );

            getLogger().debug( "Copying " + file + " to " + destDir );

            if ( file.isDirectory() )
            {
                makeDirectoryIfNecessary( destDir );
            }
            else
            {
                FileUtils.copyFileToDirectory( file, destDir.getParentFile() );
            }
        }
    }

    private String concat( String[] array, String delim )
    {
        StringBuilder buffer = new StringBuilder();
        for ( int i = 0; i < array.length; i++ )
        {
            if ( i > 0 )
            {
                buffer.append( delim );
            }
            String s = array[i];
            buffer.append( s ).append( delim );
        }
        return buffer.toString();
    }
}
