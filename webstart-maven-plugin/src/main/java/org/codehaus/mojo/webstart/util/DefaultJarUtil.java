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
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.jar.Attributes.Name;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

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
     * {@inheritDoc}
     */
    public void updateManifestEntries(File jar, Map<String, String> manifestentries, boolean overrideDuplicateKeys)
        throws MojoExecutionException
    {

        Manifest manifest = createManifest(jar, manifestentries, overrideDuplicateKeys);

        File updatedUnprocessedJarFile = new File( jar.getParent(), jar.getName() + "_updateManifestEntriesJar" );

        ZipFile originalJar = null;
        JarOutputStream targetJar = null;

        try
        {
            originalJar = new ZipFile( jar );
            targetJar = new JarOutputStream( new FileOutputStream( updatedUnprocessedJarFile ), manifest );

            copyJarEntries(originalJar, targetJar, new ManifestZipEntry());
            targetJar.close();
            originalJar.close();
        }
        catch (IOException e)
        {
            throw new MojoExecutionException("Error while updating manifest of " + jar.getName(), e);
        } finally
        {
            org.apache.maven.shared.utils.io.IOUtil.close(targetJar);
            ioUtil.close(originalJar);
        }

        // delete incoming jar file
        ioUtil.deleteFile(jar);

        // rename patched jar to incoming jar file
        ioUtil.renameTo(updatedUnprocessedJarFile, jar);
    }

    abstract class ZipEntryCheck
    {

        abstract boolean skipEntry(ZipFile zipFile, ZipEntry entry)
            throws IOException;
    }

    class ManifestZipEntry
        extends ZipEntryCheck
    {

        @Override
        boolean skipEntry(ZipFile zipFile, ZipEntry entry)
        {
            return JarFile.MANIFEST_NAME.equals(entry.getName());
        }
    }

    private void copyJarEntries(ZipFile originalJar, JarOutputStream targetJar, ZipEntryCheck checkCallBack)
        throws IOException
    {
            // add all other entries from the original jar file
            Enumeration<? extends ZipEntry> entries = originalJar.entries();
            while ( entries.hasMoreElements() )
            {
                ZipEntry entry = entries.nextElement();

                // skip the original manifest
            if (checkCallBack.skipEntry(originalJar, entry))
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
    }

    /**
     * Create the new manifest from the existing jar file and the new entries
     *
     * @param jar
     * @param manifestentries
     * @return Manifest
     * @throws MojoExecutionException
     */
    protected Manifest createManifest(File jar, Map<String, String> manifestentries, boolean overrideDuplicateKeys)
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
                manifest.getMainAttributes().putValue( Name.MANIFEST_VERSION.toString(), "1.0" );
            }

            // add or overwrite entries
            Set<Entry<String, String>> entrySet = manifestentries.entrySet();
            for ( Entry<String, String> entry : entrySet )
            {
                if (!overrideDuplicateKeys && manifest.getMainAttributes().containsKey(new Name(entry.getKey())))
                {
                    continue;
                }
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

    abstract class SignedJnlpFileCheck
        extends ZipEntryCheck
    {

        protected boolean same = false;

        public boolean isSame()
        {
            return same;
        }
    };

    public boolean appendFileToJar(File jar, final File file, final String pathInJar)
        throws MojoExecutionException
    {

        File updatedUnprocessedJarFile = new File(jar.getParent(), jar.getName() + "_appendJnlpForSigning");

        final String jnlpEntryPath = pathInJar + "/" + file.getName();

        ZipFile originalJar = null;
        JarOutputStream targetJar = null;

        try
        {
            originalJar = new ZipFile(jar);

            targetJar = new JarOutputStream(new FileOutputStream(updatedUnprocessedJarFile));

            final FileInputStream fis = new FileInputStream(file);
            try
            {
                SignedJnlpFileCheck jnlpFileCheck = new SignedJnlpFileCheck()
                {

                    @Override
                    boolean skipEntry(ZipFile zipFile, ZipEntry entry)
                        throws IOException
                    {
                        if (jnlpEntryPath.equals(entry.getName()))
                        {
                            same = org.apache.maven.shared.utils.io.IOUtil.contentEquals(fis,
                                zipFile.getInputStream(entry));

                            return !same;
                        }
                        return false;
                    }
                };

                copyJarEntries(originalJar, targetJar, jnlpFileCheck);

                if (jnlpFileCheck.isSame())
                {
                    return false;
                }
            } finally
            {
                fis.close();
            }

            ZipEntry newEntry = new ZipEntry(jnlpEntryPath);
            targetJar.putNextEntry(newEntry);
            InputStream inputStream = null;

            try
            {
                inputStream = new FileInputStream(file);
                org.codehaus.plexus.util.IOUtil.copy(inputStream, targetJar);
                inputStream.close();
            } finally
            {
                org.apache.maven.shared.utils.io.IOUtil.close(inputStream);
            }
            targetJar.closeEntry();

            targetJar.close();
            originalJar.close();
        }
        catch (IOException e)
        {
            throw new MojoExecutionException("Error while adding entry to " + jar.getName(), e);
        } finally
        {
            org.apache.maven.shared.utils.io.IOUtil.close(targetJar);
            ioUtil.close(originalJar);
        }

        // delete incoming jar file
        ioUtil.deleteFile(jar);

        // rename patched jar to incoming jar file
        ioUtil.renameTo(updatedUnprocessedJarFile, jar);

        return true;

    }
}
