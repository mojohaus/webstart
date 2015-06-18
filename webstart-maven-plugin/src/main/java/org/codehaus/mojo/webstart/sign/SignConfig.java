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
import org.codehaus.mojo.keytool.requests.KeyToolGenerateKeyPairRequest;

import java.io.File;

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
     * Called before any Jars get signed or verified.
     * <p/>
     * This method allows you to create any keys or perform any initialisation that the
     * method of signature that you're implementing requires.
     *
     * @param workDirectory working directory
     * @param verbose       verbose flag coming from the mojo configuration
     * @param signTool      the sign tool used eventually to create or delete key store
     * @param classLoader   classloader where to find keystore (if not generating a new one)
     * @throws MojoExecutionException if something wrong occurs while init (mainly when preparing keys)
     */
    public void init( File workDirectory, boolean verbose, SignTool signTool, ClassLoader classLoader )
        throws MojoExecutionException
    {
        this.workDirectory = workDirectory;
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

            // now we will use this key store path
            setKeystore( keystoreFile.getAbsolutePath() );
        }

        // at the end keystore file must exists
        File keystoreFile = new File( getKeystore() );

        if ( !keystoreFile.exists() )
        {
            throw new MojoExecutionException( "Could not obtain key store location at " + keystore );
        }
    }


    /**
     * Creates a jarsigner request to do a sign operation.
     *
     * @param jarToSign the location of the jar to sign
     * @param signedJar the optional location of the signed jar to produce (if not set, will use the original location)
     * @return the jarsigner request
     */
    public JarSignerRequest createSignRequest( File jarToSign, File signedJar )
    {
        JarSignerSignRequest request = new JarSignerSignRequest();
        request.setAlias( getAlias() );
        request.setKeypass( getKeypass() );
        request.setKeystore( getKeystore() );
        request.setSigfile( getSigfile() );
        request.setStorepass( getStorepass() );
        request.setStoretype( getStoretype() );
        request.setWorkingDirectory( workDirectory );
        request.setMaxMemory( getMaxMemory() );
        request.setVerbose( isVerbose() );
        request.setArchive( jarToSign );
        request.setSignedjar( signedJar );
        request.setTsaLocation( getTsaLocation() );
        return request;
    }

    /**
     * Creates a jarsigner request to do a verify operation.
     *
     * @param jarFile the location of the jar to sign
     * @param certs   flag to show certificats details
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

}
