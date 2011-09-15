package org.codehaus.mojo.webstart;

import org.apache.tools.ant.Project;
import org.jdesktop.deployment.ant.pack200.Pack200Task;
import org.jdesktop.deployment.ant.pack200.Unpack200Task;

import java.io.File;
import java.io.FileFilter;

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
    public void packJars( File directory, FileFilter jarFileFilter, boolean gzip )
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
            packTask.setSegmentLimit( -1 );

            packTask.execute();
            pack200Jar.setLastModified( jarFiles[i].lastModified() );
        }
    }

    public void unpackJars( File directory, FileFilter pack200FileFilter )
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
