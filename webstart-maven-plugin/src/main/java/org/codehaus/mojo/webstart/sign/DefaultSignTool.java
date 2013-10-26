package org.codehaus.mojo.webstart.sign;

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
import org.apache.maven.shared.jarsigner.JarSigner;
import org.apache.maven.shared.jarsigner.JarSignerException;
import org.apache.maven.shared.jarsigner.JarSignerRequest;
import org.apache.maven.shared.jarsigner.JarSignerResult;
import org.apache.maven.shared.jarsigner.JarSignerUtil;
import org.codehaus.mojo.keytool.KeyTool;
import org.codehaus.mojo.keytool.KeyToolException;
import org.codehaus.mojo.keytool.KeyToolResult;
import org.codehaus.mojo.keytool.requests.KeyToolGenerateKeyPairRequest;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.cli.CommandLineException;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Default implementation of the {@link SignTool}.
 *
 * @author tchemit <chemit@codelutin.com>
 * @plexus.component role-hint="default"
 * @since 1.0-beta-3
 */
public class DefaultSignTool
    extends AbstractLogEnabled
    implements SignTool
{

    /**
     * The component to invoke jarsigner command.
     *
     * @plexus.requirement role="org.apache.maven.shared.jarsigner.JarSigner"
     */
    private JarSigner jarSigner;

    /**
     * The component to invoke keyTool command.
     *
     * @plexus.requirement role="org.codehaus.mojo.keytool.KeyTool"
     */
    private KeyTool keyTool;

    /**
     * {@inheritDoc}
     */
    public void generateKey( SignConfig config, File keystoreFile )
        throws MojoExecutionException
    {
        KeyToolGenerateKeyPairRequest request = config.createKeyGenRequest( keystoreFile );

        try
        {
            KeyToolResult result = keyTool.execute( request );

            CommandLineException exception = result.getExecutionException();
            if ( exception != null )
            {
                throw new MojoExecutionException( "Could not sign jar " + keystoreFile, exception );
            }
        }
        catch ( KeyToolException e )
        {
            throw new MojoExecutionException( "Could not find keytool", e );
        }
    }

    /**
     * {@inheritDoc}
     */
    public void sign( SignConfig config, File jarFile, File signedJar )
        throws MojoExecutionException
    {

        JarSignerRequest request = config.createSignRequest( jarFile, signedJar );

        try
        {
            JarSignerResult result = jarSigner.execute( request );

            CommandLineException exception = result.getExecutionException();
            if ( exception != null )
            {
                throw new MojoExecutionException( "Could not sign jar " + jarFile, exception );
            }
        }
        catch ( JarSignerException e )
        {
            throw new MojoExecutionException( "Could not find jarSigner", e );
        }
    }

    /**
     * {@inheritDoc}
     */
    public void verify( SignConfig config, File jarFile, boolean certs )
        throws MojoExecutionException
    {

        JarSignerRequest request = config.createVerifyRequest( jarFile, certs );

        try
        {
            JarSignerResult result = jarSigner.execute( request );

            CommandLineException exception = result.getExecutionException();
            if ( exception != null )
            {
                throw new MojoExecutionException( "Could not verify jar " + jarFile, exception );
            }
        }
        catch ( JarSignerException e )
        {
            throw new MojoExecutionException( "Could not find jarSigner", e );
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean isJarSigned( File jarFile )
        throws MojoExecutionException
    {
        try
        {
//            return JarSignerUtil.isArchiveSigned( jarFile );
            return isArchiveSigned( jarFile );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Could not verifiy that jar is signed or not", e );
        }

    }

    /**
     * {@inheritDoc}
     */
    public void unsign( File jarFile, boolean verbose )
        throws MojoExecutionException
    {

        if ( isJarSigned( jarFile ) )
        {

            // unsign jar

            verboseLog( verbose, "Unsign jar " + jarFile );
            try
            {
                JarSignerUtil.unsignArchive( jarFile );
            }
            catch ( IOException e )
            {

                throw new MojoExecutionException( "Could not find unsign jar " + jarFile, e );
            }
        }
        else
        {

            // not signed jar do nothing
            verboseLog( verbose, "Jar " + jarFile + " is not signed." );
        }
    }

    /**
     * {@inheritDoc}
     */
    public void deleteKeyStore( File keystore, boolean verbose )
    {
        if ( keystore.exists() )
        {
            if ( keystore.delete() )
            {
                infoOrDebug( verbose, "deleted keystore from: " + keystore.getAbsolutePath() );
            }
            else
            {
                getLogger().warn( "Couldn't delete keystore from: " + keystore.getAbsolutePath() );
            }
        }
        else
        {
            infoOrDebug( verbose, "Skipping deletion of non existing keystore: " + keystore.getAbsolutePath() );
        }
    }

    /**
     * {@inheritDoc}
     */
    public File getKeyStoreFile( String keystore, File workingKeystore, ClassLoader classLoader )
        throws IOException
    {
        File result = new File( keystore );

        if ( !result.exists() )
        {
            result = null;

            URL url;

            if ( keystore.startsWith( "classpath:" ) )
            {

                // detects it in classpath
                String path = keystore.substring( "classpath:".length() );

                if ( path.startsWith( "/" ) )
                {
                    // remove first car
                    path = path.substring( 1 );
                }
                url = classLoader.getResource( path );
            }
            else
            {
                url = new URL( keystore );

            }

            if ( url != null )
            {
                InputStream inputStream = url.openStream();

                try
                {
                    if ( inputStream != null )
                    {

                        // copy stream to a file
                        result = workingKeystore;

                        File parentFile = result.getParentFile();
                        boolean mkdirs = parentFile.exists() || parentFile.mkdirs();
                        if ( !mkdirs )
                        {
                            throw new IOException( "Could not create directory " + parentFile );
                        }
                        OutputStream outputStream = new FileOutputStream( result );
                        try
                        {
                            IOUtil.copy( inputStream, outputStream );
                        }
                        finally
                        {
                            IOUtil.close( outputStream );
                        }
                    }
                }
                catch ( IOException e )
                {
                    result = null;
                    throw e;
                }
                finally
                {
                    IOUtil.close( inputStream );
                }

            }
        }
        return result;
    }

    /**
     * Log as info when verbose or info is enabled, as debug otherwise.
     *
     * @param verbose verbose level
     * @param msg     message to log
     */
    protected void verboseLog( boolean verbose, String msg )
    {
        infoOrDebug( verbose || getLogger().isInfoEnabled(), msg );
    }

    /**
     * Log a message as info or debug.
     *
     * @param info if set to true, log as info(), otherwise as debug()
     * @param msg  message to log
     */
    private void infoOrDebug( boolean info, String msg )
    {
        if ( info )
        {
            getLogger().info( msg );
        }
        else
        {
            getLogger().debug( msg );
        }
    }

    // TODO-tchemit-2012-07-01 : Replace this by JarSignerUtil code when using maven-jarsigner 1.1

    /**
     * Scans an archive for existing signatures.
     *
     * @param jarFile The archive to scan, must not be <code>null</code>.
     * @return <code>true</code>, if the archive contains at least one signature file; <code>false</code>, if the
     * archive does not contain any signature files.
     * @throws IOException if scanning <code>jarFile</code> fails.
     */
    public static boolean isArchiveSigned( final File jarFile )
        throws IOException
    {
        if ( jarFile == null )
        {
            throw new NullPointerException( "jarFile" );
        }

        ZipInputStream in = null;
        boolean suppressExceptionOnClose = true;

        try
        {
            boolean signed = false;
            in = new ZipInputStream( new BufferedInputStream( new FileInputStream( jarFile ) ) );

            for ( ZipEntry ze = in.getNextEntry(); ze != null; ze = in.getNextEntry() )
            {
                if ( isSignatureFile( ze.getName() ) )
                {
                    signed = true;
                    break;
                }
            }

            suppressExceptionOnClose = false;
            return signed;
        }
        finally
        {
            try
            {
                if ( in != null )
                {
                    in.close();
                }
            }
            catch ( final IOException e )
            {
                if ( !suppressExceptionOnClose )
                {
                    throw e;
                }
            }
        }
    }

    /**
     * Checks whether the specified JAR file entry denotes a signature-related file, i.e. matches
     * <code>META-INF/*.SF</code>, <code>META-INF/*.DSA</code> or <code>META-INF/*.RSA</code>.
     *
     * @param entryName The name of the JAR file entry to check, must not be <code>null</code>.
     * @return <code>true</code> if the entry is related to a signature, <code>false</code> otherwise.
     */
    private static boolean isSignatureFile( String entryName )
    {
        if ( entryName.regionMatches( true, 0, "META-INF", 0, 8 ) )
        {
            entryName = entryName.replace( '\\', '/' );

            if ( entryName.indexOf( '/' ) == 8 && entryName.lastIndexOf( '/' ) == 8 )
            {
                if ( entryName.regionMatches( true, entryName.length() - 3, ".SF", 0, 3 ) )
                {
                    return true;
                }
                if ( entryName.regionMatches( true, entryName.length() - 4, ".DSA", 0, 4 ) )
                {
                    return true;
                }
                if ( entryName.regionMatches( true, entryName.length() - 4, ".RSA", 0, 4 ) )
                {
                    return true;
                }
                if ( entryName.regionMatches( true, entryName.length() - 3, ".EC", 0, 3 ) )
                {
                    return true;
                }
            }
        }
        return false;
    }

}
