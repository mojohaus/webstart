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
import org.codehaus.plexus.archiver.UnArchiver;
import org.codehaus.plexus.archiver.jar.JarArchiver;
import org.codehaus.plexus.archiver.jar.Manifest;
import org.codehaus.plexus.archiver.jar.ManifestException;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * Created on 10/26/13.
 *
 * @author Tony Chemit <chemit@codelutin.com>
 * @since 1.0-beta-4
 */
@Component( role = JarUtil.class, hint = "default" )
public class DefaultJarUtil
    implements JarUtil
{

    /**
     * io helper.
     */
    @Requirement
    protected IOUtil ioUtil;

    /**
     * The Jar unarchiver.
     */
    @Requirement(hint = "jar")
    protected UnArchiver jarUnarchiver;

    /**
     * The Jar archiver.
     */
    @Requirement(role = Archiver.class, hint = "jarWithNoLog")
    protected JarArchiver jarArchiver;

    /**
     * {@inheritDoc}
     */
    public void setManifestEntries( Map<String, String> entries )
        throws MojoExecutionException
    {

        Manifest newManifest = new Manifest();
        try
        {
            for ( Map.Entry<String, String> entry : entries.entrySet() )
            {

                newManifest.addConfiguredAttribute( new Manifest.Attribute( entry.getKey(), entry.getValue() ) );
            }

            JarArchiver.FilesetManifestConfig config = new JarArchiver.FilesetManifestConfig();
            config.setValue( "mergewithoutmain" );
            jarArchiver.setFilesetmanifest( config );
            jarArchiver.addConfiguredManifest( newManifest );
        }
        catch ( ManifestException e )
        {
            throw new MojoExecutionException( "Could not create manifest", e );
        }
    }

    /**
     * {@inheritDoc}
     */
    public void updateManifestEntries( File jar )
        throws MojoExecutionException
    {

        File extractDirectory = new File( jar.getParent(), jar.getName() + "_updateManifestEntries" );

        ioUtil.makeDirectoryIfNecessary( extractDirectory );

        jarUnarchiver.setSourceFile( jar );
        jarUnarchiver.setDestDirectory( extractDirectory );
        jarUnarchiver.extract();

        // recreate jar with updated manifest
        File updatedUnprocessedJarFile = new File( jar.getParent(), jar.getName() + "_updateManifestEntriesJar" );

        jarArchiver.addDirectory( extractDirectory );
        jarArchiver.setDestFile( updatedUnprocessedJarFile );
        try
        {
            jarArchiver.createArchive();
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Could not create jar " + updatedUnprocessedJarFile, e );
        }

        // delete incoming jar file
        ioUtil.deleteFile( jar );

        // rename patched jar to incoming jar file
        ioUtil.renameTo( updatedUnprocessedJarFile, jar );

        // delete temp directory
        ioUtil.removeDirectory( extractDirectory );
    }
}
