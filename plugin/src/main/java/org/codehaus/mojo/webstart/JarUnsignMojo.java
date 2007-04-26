package org.codehaus.mojo.webstart;

/*
 * Copyright 2001-2007 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License" );
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProjectHelper;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.UnArchiver;
import org.codehaus.plexus.archiver.jar.JarArchiver;
import org.codehaus.plexus.archiver.manager.ArchiverManager;
import org.codehaus.plexus.archiver.manager.NoSuchArchiverException;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Unsigns a JAR, removing signatures.
 *
 * This code will hopefully be moved into the jar plugin when stable enough.
 * 
 * @author <a href="mailto:jerome@coffeebreaks.org">Jerome Lacoste</a>, <a href="mailto:andrius@pivotcapital.com">Andrius Šabanas</a>
 * @version $Id: JarSignMojo.java 393237 2006-04-11 14:47:48Z kenney $
 * @goal unsign
 * @phase package
 * @requiresProject
 * @todo refactor the common code with javadoc plugin
 */
public class JarUnsignMojo
    extends AbstractMojo
{
    /**
     * Set this to <code>true</code> to disable signing.
     * Useful to speed up build process in development environment.
     *
     * @parameter expression="${maven.jar.unsign.skip}" default-value="false"
     */
    private boolean skip;

    /**
     * The directory location used for temporary storage of files used by this mojo.
     * @parameter expression="${tempdir}" default-value="${basedir}"
     * @required
     */
    private File tempDirectory;

    /**
     * Path of the jar to unsign. When specified, the finalName is ignored.
     *
     * @parameter alias="jarpath" default-value="${project.build.directory}/${project.build.finalName}.${project.packaging}"
     */
    private File jarPath;

    /**
     * See <a href="http://java.sun.com/j2se/1.4.2/docs/tooldocs/windows/jarsigner.html#Options">options</a>.
     *
     * Not specifying this argument will unsign the jar in-place (your original jar is going to be overwritten).
     *
     * @parameter expression="${unsignedjar}"
     */
    // private File unsignedjar;

    /**
     * Automatically verify a jar after unsigning it.
     * <p/>
     * See <a href="http://java.sun.com/j2se/1.4.2/docs/tooldocs/windows/jarsigner.html#Options">options</a>.
     *
     * @parameter expression="${verify}" default-value="false"
     */
//    private boolean verify;

    /**
     * Enable verbose
     * See <a href="http://java.sun.com/j2se/1.4.2/docs/tooldocs/windows/jarsigner.html#Options">options</a>.
     *
     * @parameter expression="${verbose}" default-value="false"
     */
    private boolean verbose;

    /**
     * @component
     */
    private MavenProjectHelper projectHelper;

    /**
     * To look up Archiver/UnArchiver implementations
     *
     * @parameter expression="${component.org.codehaus.plexus.archiver.manager.ArchiverManager}"
     * @required
     */
    protected ArchiverManager archiverManager;    

    private FileFilter removeSignatureFileFilter = new FileFilter() {
        private final String[] EXT_ARRAY = { "DSA", "RSA", "SF" };
        private final List EXT_TO_REMOVE = Arrays.asList(EXT_ARRAY);
        
        public boolean accept(File file) {
            String extension = FileUtils.getExtension(file.getAbsolutePath());
            return (EXT_TO_REMOVE.contains(extension));
        }
    };
    
    public void execute()
        throws MojoExecutionException
    {
        if ( skip )
        {
            getLog().info( "Skipping JAR unsigning for file: " + jarPath.getAbsolutePath() );
            return;
        }

        File jarFile = this.jarPath;
        File tempDirParent = this.tempDirectory;

        String archiveExt = FileUtils.getExtension( jarFile.getAbsolutePath() ).toLowerCase();
        
        // create temp dir
        File tempDir = new File( tempDirParent, jarFile.getName() );

        if ( !tempDir.mkdirs() ) {
            throw new MojoExecutionException( "Error creating temporary directory: " + tempDir );
        }
        // FIXME we probably want to be more security conservative here. 
        // it's very easy to guess where the directory will be and possible 
        // to access/change its contents before the file is rejared..
        
        // extract jar into temporary directory
        try {
            UnArchiver unArchiver = this.archiverManager.getUnArchiver( archiveExt );
            unArchiver.setSourceFile( jarFile );
            unArchiver.setDestDirectory( tempDir );
            unArchiver.extract();            
        } catch ( IOException ex ) {
            throw new MojoExecutionException( "Error unpacking file: " + jarFile + "to: " + tempDir, ex );
        } catch ( ArchiverException ex)  {
            throw new MojoExecutionException( "Error unpacking file: " + jarFile + "to: " + tempDir, ex );            
        } catch ( NoSuchArchiverException ex ) {
            throw new MojoExecutionException( "Error acquiring unarchiver for extension: " + archiveExt, ex );
        }
        
        // create and check META-INF directory
        File metaInf = new File( tempDir, "META-INF" );
        if ( !metaInf.isDirectory() ) {
            return;
        }
        
        // filter signature files and remove them
        File[] filesToRemove = metaInf.listFiles( this.removeSignatureFileFilter );                
        if ( filesToRemove.length == 0 ) {
            return;
        }                
        for ( int i = 0; i < filesToRemove.length; i++ ) {
            if ( !filesToRemove[i].delete() ) {
                throw new MojoExecutionException( "Error removing signature file: " + filesToRemove[i] );
            }
        }
        
        // recreate archive
        try {
            JarArchiver jarArchiver = (JarArchiver) this.archiverManager.getArchiver( "jar" );
            jarArchiver.setUpdateMode( false );
            jarArchiver.addDirectory( tempDir );
            jarArchiver.setDestFile( jarFile );
            jarArchiver.createArchive();
            
        } catch ( ArchiverException ex ) {
            throw new MojoExecutionException( "Error packing directory: " + tempDir + "to: " + jarFile, ex );
        } catch ( IOException ex ) {
            throw new MojoExecutionException( "Error packing directory: " + tempDir + "to: " + jarFile, ex );
        } catch ( NoSuchArchiverException ex ) {
            throw new MojoExecutionException( "Error acquiring archiver for extension: jar", ex );
        }

        try {
            FileUtils.deleteDirectory( tempDir );
        } catch ( IOException ex ) {
            throw new MojoExecutionException( "Error cleaning up temporary directory file: " + tempDir, ex );
        }
    }
    

    public void setTempDir( File tempDirectory )
    {
        this.tempDirectory = tempDirectory;
    }
/*
    public void setBasedir( File basedir )
    {
        this.basedir = basedir;
    }
*/
    public void setJarPath( File jarPath )
    {
        this.jarPath = jarPath;
    }
/*
    public void setUnsignedJar( File unsignedjar )
    {
        this.unsignedjar = unsignedjar;
    }
*/
    public void setVerbose( boolean verbose )
    {
        this.verbose = verbose;
    }

    public void setArchiverManager( ArchiverManager archiverManager )
    {
        this.archiverManager = archiverManager;
    }
/*
    public void setVerify( boolean verify )
    {
        this.verify = verify;
    }
*/
}
