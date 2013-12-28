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
import org.apache.maven.shared.jarsigner.JarSignerRequest;
import org.apache.maven.shared.jarsigner.JarSignerUtil;
import org.apache.maven.shared.utils.cli.CommandLineException;
import org.apache.maven.shared.utils.cli.javatool.JavaToolException;
import org.apache.maven.shared.utils.cli.javatool.JavaToolResult;
import org.apache.maven.shared.utils.io.FileUtils;
import org.codehaus.mojo.keytool.KeyTool;
import org.codehaus.mojo.keytool.requests.KeyToolGenerateKeyPairRequest;
import org.codehaus.mojo.webstart.util.IOUtil;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.AbstractLogEnabled;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Default implementation of the {@link SignTool}.
 *
 * @author tchemit <chemit@codelutin.com>
 * @since 1.0-beta-3
 */
@Component( role = SignTool.class, hint = "default" )
public class DefaultSignTool
    extends AbstractLogEnabled
    implements SignTool
{

    /**
     * The component to invoke jarsigner command.
     */
    @Requirement
    private JarSigner jarSigner;

    /**
     * The component to invoke keyTool command.
     */
    @Requirement
    private KeyTool keyTool;

    /**
     * io helper.
     */
    @Requirement
    protected IOUtil ioUtil;

    /**
     * {@inheritDoc}
     */
    public void generateKey( SignConfig config, File keystoreFile )
        throws MojoExecutionException
    {
        KeyToolGenerateKeyPairRequest request = config.createKeyGenRequest( keystoreFile );

        try
        {
            JavaToolResult result = keyTool.execute( request );

            CommandLineException exception = result.getExecutionException();
            if ( exception != null )
            {
                throw new MojoExecutionException( "Could not generate key store " + keystoreFile, exception );
            }
            int exitCode = result.getExitCode();
            if ( exitCode != 0 )
            {
                throw new MojoExecutionException(
                    "Could not generate key store " + keystoreFile + ", use -X to have detail of error" );
            }
        }
        catch ( JavaToolException e )
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
            JavaToolResult result = jarSigner.execute( request );

            CommandLineException exception = result.getExecutionException();
            if ( exception != null )
            {
                throw new MojoExecutionException( "Could not sign jar " + jarFile, exception );
            }
            int exitCode = result.getExitCode();
            if ( exitCode != 0 )
            {
                throw new MojoExecutionException(
                    "Could not sign jar " + jarFile + ", use -X to have detail of error" );
            }
        }
        catch ( JavaToolException e )
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
            JavaToolResult result = jarSigner.execute( request );

            CommandLineException exception = result.getExecutionException();
            if ( exception != null )
            {
                throw new MojoExecutionException( "Could not verify jar " + jarFile, exception );
            }
            int exitCode = result.getExitCode();
            if ( exitCode != 0 )
            {
                throw new MojoExecutionException(
                    "Could not verify jar " + jarFile + ", use -X to have detail of error" );
            }
        }
        catch ( JavaToolException e )
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
            return JarSignerUtil.isArchiveSigned( jarFile );
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
                //Fixme reuse this with maven-jarsigner 1.3
                //JarSignerUtil.unsignArchive( jarFile );
                unsignArchive( jarFile );
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
        throws MojoExecutionException
    {

        File result;

        URI keystoreURI = null;
        try
        {
            keystoreURI = URI.create( keystore );
        }
        catch ( IllegalArgumentException e )
        {
            // Windows paths like C:\Users throw an IAE due to the '\'  
        }

        if ( keystoreURI == null || keystoreURI.getScheme() == null )
        {

            // consider it as a simple file
            result = new File( keystore );
        }
        else
        {
            // copy stream to working keystore
            result = workingKeystore;

            // make parent directory if required
            ioUtil.makeDirectoryIfNecessary( result.getParentFile() );

            // copy keystore  to workingKeystore
            ioUtil.copyResources( keystoreURI, classLoader, result );
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

    /**
     * Removes any existing signatures from the specified JAR file. We will stream from the input JAR directly to the
     * output JAR to retain as much metadata from the original JAR as possible.
     *
     * @param jarFile The JAR file to unsign, must not be <code>null</code>.
     * @throws java.io.IOException
     */
    //Fixme remove this when using maven-jarsigner 1.3
    public void unsignArchive( File jarFile )
        throws IOException
    {

        File unsignedFile = new File( jarFile.getAbsolutePath() + ".unsigned" );

        ZipInputStream zis = null;
        ZipOutputStream zos = null;
        try
        {
            zis = new ZipInputStream( new BufferedInputStream( new FileInputStream( jarFile ) ) );
            zos = new ZipOutputStream( new BufferedOutputStream( new FileOutputStream( unsignedFile ) ) );

            for ( ZipEntry ze = zis.getNextEntry(); ze != null; ze = zis.getNextEntry() )
            {
                if ( isSignatureFile( ze.getName() ) )
                {

                    continue;
                }

                zos.putNextEntry( new ZipEntry( ze.getName() ) );

                if (isManifestFile(ze.getName())) {

                    // remove any digest informations

                    getLogger().info("Found manifest: "+ze.getName());

                    Manifest mf = new Manifest( zis );

                    getLogger().info("Manifest: "+mf);

                    Manifest oldManifest = new Manifest( zis );
                    Manifest newManifest = buildUnsignedManifest( oldManifest );
                    newManifest.write( zos );

                    continue;
                }

                org.apache.maven.shared.utils.io.IOUtil.copy( zis, zos );

            }
        }
        finally
        {
            org.apache.maven.shared.utils.io.IOUtil.close( zis );
            org.apache.maven.shared.utils.io.IOUtil.close( zos );
        }

        FileUtils.rename( unsignedFile, jarFile );

    }

    /**
     * Checks whether the specified JAR file entry denotes a signature-related file, i.e. matches
     * <code>META-INF/*.SF</code>, <code>META-INF/*.DSA</code> or <code>META-INF/*.RSA</code>.
     *
     * @param entryName The name of the JAR file entry to check, must not be <code>null</code>.
     * @return <code>true</code> if the entry is related to a signature, <code>false</code> otherwise.
     */
    //Fixme remove this when using maven-jarsigner 1.3
    private boolean isSignatureFile( String entryName )
    {
        boolean result = false;
        if ( entryName.regionMatches( true, 0, "META-INF", 0, 8 ) )
        {
            entryName = entryName.replace( '\\', '/' );

            if ( entryName.indexOf( '/' ) == 8 && entryName.lastIndexOf( '/' ) == 8 )
            {
                if ( entryName.regionMatches( true, entryName.length() - 3, ".SF", 0, 3 ) )
                {
                    result = true;
                }
                else if ( entryName.regionMatches( true, entryName.length() - 4, ".DSA", 0, 4 ) )
                {
                    result = true;
                }
                else if ( entryName.regionMatches( true, entryName.length() - 4, ".RSA", 0, 4 ) )
                {
                    result = true;
                }
                else if ( entryName.regionMatches( true, entryName.length() - 3, ".EC", 0, 3 ) )
                {
                    result = true;
                }
            }
        }
        return result;
    }

    //Fixme remove this when using maven-jarsigner 1.3
    private boolean isManifestFile( String entryName )
    {
        boolean result = false;
        if ( entryName.regionMatches( true, 0, "META-INF", 0, 8 ) )
        {
            entryName = entryName.replace( '\\', '/' );

            if ( entryName.indexOf( '/' ) == 8 && entryName.lastIndexOf( '/' ) == 8 )
            {
                if ( entryName.regionMatches( true, entryName.length() - 11, "MANIFEST.MF", 0, 11 ) )
                {
                    result = true;
                }
            }
        }
        return result;
    }

    /**
     * Build a new manifest from the given one removing any signing information inside it.
     *
     * This is done by removing any attributes containing some digest informations.
     * If a entry has then no more attributes, then it will not be readd in the result manifest.
     *
     * @param manifest manifest to clean
     * @return the build manifest with no digest attributes
     * @since 1.3
     */
    protected Manifest buildUnsignedManifest( Manifest manifest ) {

        Manifest result = new Manifest( manifest );
        result.getMainAttributes().clear();

        for ( Map.Entry<String, Attributes> entry : manifest.getEntries().entrySet() )
        {
            Attributes oldAttributes = entry.getValue();
            Attributes newAttributes = new Attributes();
            for ( Map.Entry<Object, Object> objectEntry : oldAttributes.entrySet() )
            {
                String attributeKey = String.valueOf( objectEntry.getKey() );
                if ( !attributeKey.contains( "-Digest" ) )
                {
                    // can add this attribute
                    newAttributes.put( objectEntry.getKey(), objectEntry.getValue() );
                }
            }
            if ( !newAttributes.isEmpty() )
            {
                // can add this entry
                result.getEntries().put( entry.getKey(), newAttributes );
            }
        }
        return result;
    }

}
