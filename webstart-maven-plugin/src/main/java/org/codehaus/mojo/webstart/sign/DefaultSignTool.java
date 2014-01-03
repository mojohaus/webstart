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
import org.codehaus.mojo.keytool.KeyTool;
import org.codehaus.mojo.keytool.requests.KeyToolGenerateKeyPairRequest;
import org.codehaus.mojo.webstart.util.IOUtil;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.AbstractLogEnabled;

import java.io.File;
import java.io.IOException;
import java.net.URI;

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

}
