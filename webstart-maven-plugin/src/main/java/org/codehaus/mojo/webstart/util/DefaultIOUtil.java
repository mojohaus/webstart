package org.codehaus.mojo.webstart.util;

import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Iterator;
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
                getLogger().info( "Deleting directory " + dir.getAbsolutePath() );
                try
                {
                    FileUtils.deleteDirectory( dir );
                }
                catch ( IOException e )
                {
                    throw new MojoExecutionException( "Could not delete directory " + dir, e );
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void makeDirectoryIfNecessary( File dir, String errorMessage )
        throws MojoExecutionException
    {

        if ( !dir.exists() && !dir.mkdirs() )
        {
            throw new MojoExecutionException(
                ( errorMessage == null ? "Failed to create directory: " : errorMessage ) + dir );
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

        for ( int i = 0; i < files.length; i++ )
        {
            deleteFile( files[i], "Couldn't delete file: " );
        }
        return files.length;
    }

    /**
     * {@inheritDoc}
     */
    public void deleteFile( File file, String errorMessage )
    {
        if ( file.exists() && !file.delete() )
        {
            throw new IllegalStateException( errorMessage + file.getAbsolutePath() );
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

        List files = FileUtils.getFiles( sourceDirectory, includes, excludes );

        for ( Iterator i = files.iterator(); i.hasNext(); )
        {
            File file = (File) i.next();

            getLogger().debug( "Copying " + file + " to " + destinationDirectory );

            String path = file.getAbsolutePath().substring( sourceDirectory.getAbsolutePath().length() + 1 );

            File destDir = new File( destinationDirectory, path );

            getLogger().debug( "Copying " + file + " to " + destDir );

            if ( file.isDirectory() )
            {
                makeDirectoryIfNecessary( destDir, null );
            }
            else
            {
                FileUtils.copyFileToDirectory( file, destDir.getParentFile() );
            }
        }
    }

    private String concat( String[] array, String delim )
    {
        StringBuffer buffer = new StringBuffer();
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
