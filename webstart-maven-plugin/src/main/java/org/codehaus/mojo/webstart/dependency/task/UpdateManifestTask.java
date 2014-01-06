package org.codehaus.mojo.webstart.dependency.task;

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
import org.codehaus.mojo.webstart.dependency.JnlpDependencyConfig;
import org.codehaus.mojo.webstart.util.IOUtil;
import org.codehaus.mojo.webstart.util.JarUtil;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Map;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Created on 1/4/14.
 *
 * @author Tony Chemit <chemit@codelutin.com>
 * @since 1.0-beta-5
 */
@Component(role = JnlpDependencyTask.class, hint = UpdateManifestTask.ROLE_HINT, instantiationStrategy = "per-lookup")
public class UpdateManifestTask
    extends AbstractJnlpTask
{
    public static final String ROLE_HINT = "UpdateManifestTask";

    @Requirement
    private IOUtil ioUtil;

    /**
     * {@inheritDoc}
     */
    public void check( JnlpDependencyConfig config )
    {
        if ( config == null )
        {
            throw new NullPointerException( "config can't be null" );
        }
        if ( config.getArtifact() == null )
        {
            throw new NullPointerException( "config.artifact can't be null" );
        }
        if ( config.getArtifact().getFile() == null )
        {
            throw new NullPointerException( "config.artifact.file can't be null" );
        }
        if ( !config.isUpdateManifest() )
        {
            throw new IllegalStateException( "Can't update manifest if config.isUpdateManifest is false" );
        }
    }

    /**
     * {@inheritDoc}
     */
    public File execute( JnlpDependencyConfig config, File file )
        throws JnlpDependencyTaskException
    {

        verboseLog( config, "Update manifest " + file.getName() );

        try
        {
            File updatedFile = updateManifestEntries( file, config.getUpdateManifestEntries() );
            return updatedFile;
        }
        catch ( MojoExecutionException e )
        {
            throw new JnlpDependencyTaskException( "Could not update manifest of file: " + file, e );
        }
    }

    private File updateManifestEntries( File jar, Map<String, String> manifestentries )
        throws MojoExecutionException
    {

        Manifest manifest = createManifest( jar, manifestentries );

        File updatedUnprocessedJarFile = new File( jar.getParent(), jar.getName() + "_updateManifestEntriesJar" );

        ZipFile originalJar = null;
        JarOutputStream targetJar = null;

        try
        {
            originalJar = new ZipFile( jar );
            targetJar = new JarOutputStream( new FileOutputStream( updatedUnprocessedJarFile ), manifest );

            // add all other entries from the original jar file
            Enumeration<? extends ZipEntry> entries = originalJar.entries();
            while ( entries.hasMoreElements() )
            {
                ZipEntry entry = entries.nextElement();

                // skip the original manifest
                if ( JarFile.MANIFEST_NAME.equals( entry.getName() ) )
                {
                    continue;
                }

                ZipEntry newEntry = new ZipEntry( entry.getName() );
                targetJar.putNextEntry( newEntry );

                // write content to stream if it is a file
                if ( !entry.isDirectory() )
                {
                    InputStream inputStream = null;
                    try
                    {
                        inputStream = originalJar.getInputStream( entry );
                        org.codehaus.plexus.util.IOUtil.copy( inputStream, targetJar );
                        inputStream.close();
                    }
                    finally
                    {
                        org.apache.maven.shared.utils.io.IOUtil.close( inputStream );
                    }
                }
                targetJar.closeEntry();
            }
            targetJar.close();
            originalJar.close();
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Error while updating manifest of " + jar.getName(), e );
        }
        finally
        {
            org.apache.maven.shared.utils.io.IOUtil.close( targetJar );
            ioUtil.close( originalJar );
        }

        return updatedUnprocessedJarFile;
    }

    /**
     * Create the new manifest from the existing jar file and the new entries.
     *
     * @param jar
     * @param manifestentries
     * @return Manifest
     * @throws MojoExecutionException
     */
    private Manifest createManifest( File jar, Map<String, String> manifestentries )
        throws MojoExecutionException
    {
        JarFile jarFile = null;
        try
        {
            jarFile = new JarFile( jar );

            // read manifest from jar
            Manifest manifest = jarFile.getManifest();

            if ( manifest == null || manifest.getMainAttributes().isEmpty() )
            {
                manifest = new Manifest();
                manifest.getMainAttributes().putValue( Attributes.Name.MANIFEST_VERSION.toString(), "1.0" );
            }

            // add or overwrite entries
            Set<Map.Entry<String, String>> entrySet = manifestentries.entrySet();
            for ( Map.Entry<String, String> entry : entrySet )
            {
                manifest.getMainAttributes().putValue( entry.getKey(), entry.getValue() );
            }

            return manifest;
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Error while reading manifest from " + jar.getAbsolutePath(), e );
        }
        finally
        {
            ioUtil.close( jarFile );
        }
    }
}
