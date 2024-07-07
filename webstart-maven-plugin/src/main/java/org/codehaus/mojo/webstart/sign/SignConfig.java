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
import org.apache.maven.shared.jarsigner.JarSignerRequest;
import org.apache.maven.shared.jarsigner.JarSignerSignRequest;
import org.apache.maven.shared.jarsigner.JarSignerVerifyRequest;
import org.apache.maven.shared.utils.StringUtils;
import org.codehaus.mojo.keytool.requests.KeyToolGenerateKeyPairRequest;
import org.sonatype.plexus.components.sec.dispatcher.SecDispatcher;
import org.sonatype.plexus.components.sec.dispatcher.SecDispatcherException;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Bean that represents the JarSigner configuration.
 *
 * @author <a href="jerome@coffeebreaks.org">Jerome Lacoste</a>
 * @version $Id$
 */
public class SignConfig
{

    /**
     *
     */
    private File workDirectory;

    /**
     *
     */
    private boolean verbose;

    /**
     *
     */
    private KeystoreConfig keystoreConfig;

    /**
     */
    private String keystore;

    /**
     */
    private File workingKeystore;

    private File certchain;

    /**
     */
    private String keyalg;

    /**
     */
    private String keysize;

    /**
     */
    private String sigalg;

    /**
     */
    private String sigfile;

    /**
     */
    private String storetype;

    /**
     */
    private String storepass;

    /**
     */
    private String keypass;

    /**
     */
    private String validity;

    /**
     */
    private String dnameCn;

    /**
     */
    private String dnameOu;

    /**
     */
    private String dnameL;

    /**
     */
    private String dnameSt;

    /**
     */
    private String dnameO;

    /**
     */
    private String dnameC;

    /**
     */
    private String alias;

    /**
     * Whether we want to auto-verify the signed jars.
     */
    private boolean verify;

    /**
     * Optinal max memory to use.
     */
    private String maxMemory;

    /**
     * To use tsa location.
     *
     * @since 1.0-beta-5
     */
    private String tsaLocation;

    /**
     * @since 1.0-beta-7
     */
    private SecDispatcher securityDispatcher;

    /**
     * Provides custom arguements to pass to the signtool.
     */
    private List<String> arguments;


    /**
     * Optional host name of the HTTP proxy host used for accessing the
     * {@link #tsaLocation trusted timestamping server}.
     *
     * @since 1.0-beta-7
     */
    private String httpProxyHost;

    /**
     * Optional port of the HTTP proxy host used for accessing the
     * {@link #tsaLocation trusted timestamping server}.
     *
     * @since 1.0-beta-7
     */
    private String httpProxyPort;

    /**
     * Optional host name of the HTTPS proxy host used for accessing the
     * {@link #tsaLocation trusted timestamping server}.
     *
     * @since 1.0-beta-7
     */
    private String httpsProxyHost;

    /**
     * Optional port of the HTTPS proxy host used for accessing the
     * {@link #tsaLocation trusted timestamping server}.
     *
     * @since 1.0-beta-7
     */
    private String httpsProxyPort;

    /**
     * Specify number of threads to use for signing process
     */
    private int parallel = 1;

    /**
     * Specify digest algorithm to use
     */
    private String digestalg;

    /**
     * ConfigFilePath option
     *
     */
    private String providerArg;

    /**
     * Name of cryptographic service provider's master class file
     * Ex: sun.security.pkcs11.SunPKCS11
     */
    private String providerClass;

    /**
     * Called before any Jars get signed or verified.
     * <p>
     * This method allows you to create any keys or perform any initialisation that the
     * method of signature that you're implementing requires.
     *
     * @param workDirectory      working directory
     * @param verbose            verbose flag coming from the mojo configuration
     * @param signTool           the sign tool used eventually to create or delete key store
     * @param securityDispatcher component to decrypt a string, passed to it
     * @param classLoader        classloader where to find keystore (if not generating a new one)
     * @throws MojoExecutionException if something wrong occurs while init (mainly when preparing keys)
     */
    public void init( File workDirectory, boolean verbose, SignTool signTool, SecDispatcher securityDispatcher,
                      ClassLoader classLoader )
            throws MojoExecutionException
    {
        this.workDirectory = workDirectory;
        this.securityDispatcher = securityDispatcher;
        setVerbose( verbose );

        if ( workingKeystore == null )
        {
            // use a default workingKeystore file
            workingKeystore = new File( workDirectory, "workingKeystore" );
        }

        if ( keystoreConfig != null && keystoreConfig.isGen() )
        {
            File keystoreFile = new File( getKeystore() );

            if ( keystoreConfig.isDelete() )
            {
                signTool.deleteKeyStore( keystoreFile, isVerbose() );
            }

            signTool.generateKey( this, keystoreFile );
        }
        else
        {
            // try to locate key store from any location
            File keystoreFile = signTool.getKeyStoreFile( getKeystore(), workingKeystore, classLoader );


            if( !keystore.equals("NONE") )
            {
                // now we will use this key store path
                setKeystore(keystoreFile.getAbsolutePath());
            }
        }

        // at the end keystore file must exists
        File keystoreFile = new File( getKeystore() );

        if( !keystore.equals("NONE") )
	{
            if (!keystoreFile.exists())
	    {
                throw new MojoExecutionException("Could not obtain key store location at " + keystore);
            }
        }

        // reset arguments
        arguments = new ArrayList<>();

        generateArguments();
    }

    private void generateArguments()
    {
        // TODO: add support for proxy parameters to JarSigner / JarSignerSignRequest
        // instead of using implementation-specific additional arguments
        if ( httpProxyHost != null )
        {
            arguments.add( "-J-Dhttp.proxyHost=" + httpProxyHost );
        }

        if ( httpProxyPort != null )
        {
            arguments.add( "-J-Dhttp.proxyPort=" + httpProxyPort );
        }

        if ( httpsProxyHost != null )
        {
            arguments.add( "-J-Dhttps.proxyHost=" + httpsProxyHost );
        }

        if ( httpsProxyPort != null )
        {
            arguments.add( "-J-Dhttps.proxyPort=" + httpsProxyPort );
        }

        if ( !StringUtils.isEmpty( this.digestalg ) )
        {
            arguments.add( "-digestalg" );
            arguments.add( this.digestalg );
        }
    }


    /**
     * Creates a jarsigner request to do a sign operation.
     *
     * @param jarToSign the location of the jar to sign
     * @param signedJar the optional location of the signed jar to produce (if not set, will use the original location)
     * @return the jarsigner request
     * @throws MojoExecutionException if something wrong occurs
     */
    public JarSignerRequest createSignRequest( File jarToSign, File signedJar )
            throws MojoExecutionException
    {
        JarSignerSignRequest request = new JarSignerSignRequest();
        request.setAlias( getAlias() );
        request.setKeystore( getKeystore() );
        request.setSigfile( getSigfile() );
        request.setStoretype( getStoretype() );
        request.setWorkingDirectory( workDirectory );
        request.setMaxMemory( getMaxMemory() );
        request.setVerbose( isVerbose() );
        request.setArchive( jarToSign );
        request.setSignedjar( signedJar );
        request.setTsaLocation( getTsaLocation() );
        request.setProviderArg( getProviderArg() );
        request.setProviderClass( getProviderClass() );
        request.setCertchain( getCertchain() );

        // Special handling for passwords through the Maven Security Dispatcher
        request.setKeypass( decrypt( keypass ) );
        request.setStorepass( decrypt( storepass ) );

        if ( !arguments.isEmpty() )
        {
            request.setArguments( arguments.toArray( new String[arguments.size()] ) );
        }

        return request;
    }

    /**
     * Creates a jarsigner request to do a verify operation.
     *
     * @param jarFile the location of the jar to sign
     * @param certs   flag to show certificates details
     * @return the jarsigner request
     */
    public JarSignerRequest createVerifyRequest( File jarFile, boolean certs )
    {
        JarSignerVerifyRequest request = new JarSignerVerifyRequest();
        request.setCerts( certs );
        request.setWorkingDirectory( workDirectory );
        request.setMaxMemory( getMaxMemory() );
        request.setVerbose( isVerbose() );
        request.setArchive( jarFile );
        return request;
    }

    /**
     * Creates a keytool request to do a key store generation operation.
     *
     * @param keystoreFile the location of the key store file to generate
     * @return the keytool request
     */
    public KeyToolGenerateKeyPairRequest createKeyGenRequest( File keystoreFile )
    {
        KeyToolGenerateKeyPairRequest request = new KeyToolGenerateKeyPairRequest();
        request.setAlias( getAlias() );
        request.setDname( getDname() );
        request.setKeyalg( getKeyalg() );
        request.setKeypass( getKeypass() );
        request.setKeysize( getKeysize() );
        request.setKeystore( getKeystore() );
        request.setSigalg( getSigalg() );
        request.setStorepass( getStorepass() );
        request.setStoretype( getStoretype() );
        request.setValidity( getValidity() );
        request.setVerbose( isVerbose() );
        request.setWorkingDirectory( workDirectory );
        return request;
    }


    /**
     * Gets the verbose state of the configuration.
     *
     * @return {@code true} if configuration state is on, {@code false} otherwise.
     */
    public boolean isVerbose()
    {
        return verbose;
    }

    public void setWorkDirectory( File workDirectory )
    {
        this.workDirectory = workDirectory;
    }

    public void setVerbose( boolean verbose )
    {
        this.verbose = verbose;
    }

    public void setMaxMemory( String maxMemory )
    {
        this.maxMemory = maxMemory;
    }

    public void setKeystoreConfig( KeystoreConfig keystoreConfig )
    {
        this.keystoreConfig = keystoreConfig;
    }

    public void setKeystore( String keystore )
    {
        this.keystore = keystore;
    }

    public void setWorkingKeystore( File workingKeystore )
    {
        this.workingKeystore = workingKeystore;
    }

    public void setKeyalg( String keyalg )
    {
        this.keyalg = keyalg;
    }

    public void setKeysize( String keysize )
    {
        this.keysize = keysize;
    }

    public void setSigalg( String sigalg )
    {
        this.sigalg = sigalg;
    }

    public void setSigfile( String sigfile )
    {
        this.sigfile = sigfile;
    }

    public void setStoretype( String storetype )
    {
        this.storetype = storetype;
    }

    public void setStorepass( String storepass )
    {
        this.storepass = storepass;
    }

    public void setKeypass( String keypass )
    {
        this.keypass = keypass;
    }

    public void setValidity( String validity )
    {
        this.validity = validity;
    }

    public void setDnameCn( String dnameCn )
    {
        this.dnameCn = dnameCn;
    }

    public void setDnameOu( String dnameOu )
    {
        this.dnameOu = dnameOu;
    }

    public void setDnameL( String dnameL )
    {
        this.dnameL = dnameL;
    }

    public void setDnameSt( String dnameSt )
    {
        this.dnameSt = dnameSt;
    }

    public void setDnameO( String dnameO )
    {
        this.dnameO = dnameO;
    }

    public void setDnameC( String dnameC )
    {
        this.dnameC = dnameC;
    }

    public void setAlias( String alias )
    {
        this.alias = alias;
    }

    public void setVerify( boolean verify )
    {
        this.verify = verify;
    }

    public void setTsaLocation( String tsaLocation )
    {
        this.tsaLocation = tsaLocation;
    }

    public void setArguments( String[] arguments )
    {
        Collections.addAll( this.arguments, arguments );
    }

    public String getKeystore()
    {
        return keystore;
    }

    public String getKeyalg()
    {
        return keyalg;
    }

    public String getKeysize()
    {
        return keysize;
    }

    public String getSigalg()
    {
        return sigalg;
    }

    public String getSigfile()
    {
        return sigfile;
    }

    public String getStoretype()
    {
        return storetype;
    }

    public String getStorepass()
    {
        return storepass;
    }

    public String getKeypass()
    {
        return keypass;
    }

    public String getValidity()
    {
        return validity;
    }

    public String getDnameCn()
    {
        return dnameCn;
    }

    public String getDnameOu()
    {
        return dnameOu;
    }

    public String getDnameL()
    {
        return dnameL;
    }

    public String getDnameSt()
    {
        return dnameSt;
    }

    public String getDnameO()
    {
        return dnameO;
    }

    public String getDnameC()
    {
        return dnameC;
    }

    public String getAlias()
    {
        return alias;
    }

    public boolean isVerify()
    {
        return verify;
    }

    public String getTsaLocation()
    {
        return tsaLocation;
    }

    public String getMaxMemory()
    {
        return maxMemory;
    }

    public String[] getArguments()
    {
        return arguments.toArray( new String[arguments.size()] );
    }

    public String getHttpProxyHost()
    {
        return httpProxyHost;
    }

    public void setHttpProxyHost( String httpProxyHost )
    {
        this.httpProxyHost = httpProxyHost;
    }

    public String getHttpProxyPort()
    {
        return httpProxyPort;
    }

    public void setHttpProxyPort( String httpProxyPort )
    {
        this.httpProxyPort = httpProxyPort;
    }

    public String getHttpsProxyHost()
    {
        return httpsProxyHost;
    }

    public void setHttpsProxyHost( String httpsProxyHost )
    {
        this.httpsProxyHost = httpsProxyHost;
    }

    public String getHttpsProxyPort()
    {
        return httpsProxyPort;
    }

    public void setHttpsProxyPort( String httpsProxyPort )
    {
        this.httpsProxyPort = httpsProxyPort;
    }

    public int getParallel()
    {
        return parallel;
    }

    public void setParallel( int parallel )
    {
        this.parallel = parallel;
    }

    public String getDname()
    {
        StringBuffer buffer = new StringBuffer( 128 );

        appendToDnameBuffer( dnameCn, buffer, "CN" );
        appendToDnameBuffer( dnameOu, buffer, "OU" );
        appendToDnameBuffer( dnameL, buffer, "L" );
        appendToDnameBuffer( dnameSt, buffer, "ST" );
        appendToDnameBuffer( dnameO, buffer, "O" );
        appendToDnameBuffer( dnameC, buffer, "C" );

        return buffer.toString();
    }

    public String getDigestalg()
    {
        return this.digestalg;
    }

    public void setDigestalg( String digestalg )
    {
        this.digestalg = digestalg;
    }

    public String getProviderArg() {
        return providerArg;
    }

    public void setProviderArg(String providerArg) {
        this.providerArg = providerArg;
    }

    public String getProviderClass() {
        return providerClass;
    }

    public void setProviderClass(String providerClass) {
        this.providerClass = providerClass;
    }

    public File getCertchain() {
        return certchain;
    }

    public void setCertchain(File certchain) {
        this.certchain = certchain;
    }
    
    private void appendToDnameBuffer( final String property, StringBuffer buffer, final String prefix )
    {
        if ( property != null )
        {
            if ( buffer.length() > 0 )
            {
                buffer.append( ", " );
            }
            // http://jira.codehaus.org/browse/MWEBSTART-112 : have commas in parts of dName (but them must be espace)
            buffer.append( prefix ).append( "=" );
            buffer.append( property.replaceAll( ",", "\\\\," ) );
        }
    }


    private String decrypt(String encoded )
            throws MojoExecutionException
    {
        try
        {
            return securityDispatcher.decrypt( encoded );
        }
        catch ( SecDispatcherException e )
        {
            throw new MojoExecutionException( "error using security dispatcher: " + e.getMessage(), e );
        }
    }
}
