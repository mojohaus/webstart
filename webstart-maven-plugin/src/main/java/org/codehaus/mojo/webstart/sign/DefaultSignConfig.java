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
import org.codehaus.mojo.shared.keytool.KeyToolGenKeyRequest;

import java.io.File;

/**
 * Default implementation of the {@link SignConfig}.
 *
 * @author tchemit <chemit@codelutin.com>
 * @since 1.0-beta-3
 */
public class DefaultSignConfig
    implements SignConfig
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

    public void init( File workDirectory, boolean verbose, SignTool signTool )
        throws MojoExecutionException
    {
        this.workDirectory = workDirectory;
        setVerbose( verbose );

        if ( keystoreConfig != null && keystoreConfig.isGen() )
        {
            File keystoreFile = new File( getKeystore() );

            if ( keystoreConfig.isDelete() )
            {
                signTool.deleteKeyStore( keystoreFile, isVerbose() );
            }

            signTool.generateKey( this, keystoreFile );
        }
    }

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
        return request;
    }

    public JarSignerVerifyRequest createVerifyRequest( File jarFile, boolean certs )
    {
        JarSignerVerifyRequest request = new JarSignerVerifyRequest();
        request.setCerts( certs );
        request.setWorkingDirectory( workDirectory );
        request.setMaxMemory( getMaxMemory() );
        request.setVerbose( isVerbose() );
        request.setArchive( jarFile );
        return request;
    }

    public KeyToolGenKeyRequest createKeyGenRequest( File keystoreFile )
    {
        KeyToolGenKeyRequest request = new KeyToolGenKeyRequest();
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

    public boolean getVerify()
    {
        return verify;
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
            buffer.append(property.replaceAll(",", "\\\\,"));
        }
    }

}
