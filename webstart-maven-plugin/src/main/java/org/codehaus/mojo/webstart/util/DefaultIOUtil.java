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
import org.codehaus.plexus.archiver.Archiver;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.zip.ZipFile;

/**
 * Helper for all IO operations.
 *
 * @author tchemit <chemit@codelutin.com>
 * @since 1.0-beta-4
 */
@Component( role = IOUtil.class, hint = "default" )
public class DefaultIOUtil
    extends AbstractLogEnabled
    implements IOUtil
{

    /**
     * The Zip archiver.
     */
    @Requirement( hint = "zip" )
    private Archiver zipArchiver;

    /**
     * {@inheritDoc}
     */
    public void copyResources( File sourceDirectory, File targetDirectory )
        throws MojoExecutionException
    {
        if ( !sourceDirectory.exists() )
        {
            getLogger().info( "Directory does not exist " + sourceDirectory.getAbsolutePath() );
        }
        else
        {
            if ( !sourceDirectory.isDirectory() )
            {
                getLogger().debug( "Not a directory: " + sourceDirectory.getAbsolutePath() );
            }
            else
            {
                getLogger().debug( "Copying resources from " + sourceDirectory.getAbsolutePath() );

                // this may needs to be parametrized somehow
                String excludes = concat( DirectoryScanner.DEFAULTEXCLUDES, ", " );
                copyDirectoryStructure( sourceDirectory, targetDirectory, "**", excludes );
            }

        }
    }

    /**
     * {@inheritDoc}
     */
    public void copyFile( File sourceFile, File targetFile )
        throws MojoExecutionException
    {
        makeDirectoryIfNecessary( targetFile.getParentFile() );
        try
        {
            FileUtils.copyFile( sourceFile, targetFile );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Could not copy file " + sourceFile + " to " + targetFile, e );
        }
    }


    /**
     * {@inheritDoc}
     */
    public void copyDirectoryStructure( File sourceDirectory, File targetDirectory )
        throws MojoExecutionException
    {

        makeDirectoryIfNecessary( targetDirectory );

        // hopefully available from FileUtils 1.0.5-SNAPSHOT
        try
        {
            FileUtils.copyDirectoryStructure( sourceDirectory, targetDirectory );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException(
                "Could not copy directory structure from " + sourceDirectory + " to " + targetDirectory, e );
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean shouldCopyFile( File sourceFile, File targetFile )
    {
        boolean shouldCopy = !targetFile.exists() || ( targetFile.lastModified() < sourceFile.lastModified() );
        return shouldCopy;
    }

    /**
     * {@inheritDoc}
     */
    public boolean copyFileToDirectoryIfNecessary( File sourceFile, File targetDirectory )
        throws MojoExecutionException
    {

        if ( sourceFile == null )
        {
            throw new IllegalArgumentException( "sourceFile is null" );
        }

        File targetFile = new File( targetDirectory, sourceFile.getName() );

        boolean shouldCopy = shouldCopyFile( sourceFile, targetFile );

        if ( shouldCopy )
        {
            try
            {
                FileUtils.copyFileToDirectory( sourceFile, targetDirectory );
            }
            catch ( IOException e )
            {
                throw new MojoExecutionException(
                    "Could not copy file " + sourceFile + " to directory " + targetDirectory, e );
            }
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

    /**
     * {@inheritDoc}
     */
    public void copyResources( URI uri, ClassLoader classLoader, File target )
        throws MojoExecutionException
    {
        URL url;

        String scheme = uri.getScheme();
        if ( "classpath".equals( scheme ) )
        {

            // get resource from class-path
            String path = uri.getPath();

            if ( path == null )
            {
                // can happen when using classpath:myFile
                path = uri.toString().substring( scheme.length() + 1 );
            }

            if ( path.startsWith( "/" ) )
            {
                // remove first car
                path = path.substring( 1 );
            }
            url = classLoader.getResource( path );
        }
        else
        {
            // classic url from uri
            try
            {
                url = uri.toURL();
            }
            catch ( MalformedURLException e )
            {
                throw new MojoExecutionException( "Bad uri syntax " + uri, e );
            }
        }

        InputStream inputStream;

        try
        {
            inputStream = url.openStream();
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Could not open resource " + url, e );
        }

        if ( inputStream == null )
        {
            throw new MojoExecutionException( "Could not find resource " + url );
        }
        try
        {
            OutputStream outputStream = null;

            try
            {
                outputStream = new FileOutputStream( target );
                org.codehaus.plexus.util.IOUtil.copy( inputStream, outputStream );
                outputStream.close();
                inputStream.close();
            }
            catch ( IOException e )
            {
                throw new MojoExecutionException( "Could not copy resource from " + url + " to " + target, e );
            }
            finally
            {
                if ( outputStream != null )
                {
                    org.codehaus.plexus.util.IOUtil.close( outputStream );
                }
            }
        }

        finally
        {
            org.codehaus.plexus.util.IOUtil.close( inputStream );
        }

    }

    /**
     * {@inheritDoc}
     */
    public void close( ZipFile closeable )
    {
        try
        {
            if ( closeable != null )
            {
                closeable.close();
            }
        }
        catch ( IOException ignore )
        {
        }
    }

    /**
     * {@inheritDoc}
     */
    public void createArchive( File directory, File archive )
        throws MojoExecutionException
    {

        // package the zip. Note this is very simple. Look at the JarMojo which does more things.
        // we should perhaps package as a war when inside a project with war packaging ?

        makeDirectoryIfNecessary( archive.getParentFile() );

        deleteFile( archive );

        zipArchiver.addDirectory( directory );
        zipArchiver.setDestFile( archive );

        try
        {
            zipArchiver.createArchive();
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Could not create zip archive: " + archive, e );
        }
    }

    private void copyDirectoryStructure( File sourceDirectory, File destinationDirectory, String includes,
                                         String excludes )
        throws MojoExecutionException
    {
        if ( !sourceDirectory.exists() )
        {
            return;
        }

        List<File> files;
        try
        {
            files = FileUtils.getFiles( sourceDirectory, includes, excludes );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Could not obtain files from " + sourceDirectory, e );
        }

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
                try
                {
                    FileUtils.copyFileToDirectory( file, destDir.getParentFile() );
                }
                catch ( IOException e )
                {
                    throw new MojoExecutionException( "Could not copy file " + file + " to directory" + destDir, e );
                }
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
