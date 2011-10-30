package org.codehaus.mojo.webstart;

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
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.shared.jarsigner.JarSignerRequest;
import org.apache.maven.shared.jarsigner.JarSignerSignRequest;
import org.apache.maven.shared.jarsigner.JarSignerVerifyRequest;
import org.codehaus.mojo.keytool.GenkeyMojo;

import java.io.File;

/**
 * Bean that represents the JarSigner configuration.
 * <p/>
 * Specific to the JarSignMojo
 *
 * @author <a href="jerome@coffeebreaks.org">Jerome Lacoste</a>
 * @version $Id$
 */
public class JarSignMojoConfig
    implements SignConfig
{

    private Log log;

    private File workDirectory;

    private boolean verbose;

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

    /**
     * Keystore configuration
     */
    public static class KeystoreConfig
    {
        private boolean delete;

        private boolean gen;

        public boolean isDelete()
        {
            return delete;
        }

        public void setDelete( boolean delete )
        {
            this.delete = delete;
        }

        public boolean isGen()
        {
            return gen;
        }

        public void setGen( boolean gen )
        {
            this.gen = gen;
        }
    }

    public void init( Log log, File workDirectory, boolean verbose )
        throws MojoExecutionException
    {
        this.log = log;
        this.workDirectory = workDirectory;
        this.verbose = verbose;

        if ( keystoreConfig != null && keystoreConfig.isGen() )
        {
            if ( keystoreConfig.isDelete() )
            {
                deleteKeyStore();
            }
            genKeyStore();
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
        request.setVerbose( verbose );
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
        request.setVerbose( verbose );
        request.setArchive( jarFile );
        return request;
    }

    public boolean isVerbose()
    {
        return verbose;
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
            buffer.append( prefix ).append( "=" );
            buffer.append( property );
        }
    }

    /**
     * Used by init()
     *
     * @throws MojoExecutionException
     */
    private void genKeyStore()
        throws MojoExecutionException
    {
        GenkeyMojo genKeystore = new GenkeyMojo();
        genKeystore.setAlias( getAlias() );
        genKeystore.setDname( getDname() );
        genKeystore.setKeyalg( getKeyalg() );
        genKeystore.setKeypass( getKeypass() );
        genKeystore.setKeysize( getKeysize() );
        genKeystore.setKeystore( getKeystore() );
        genKeystore.setSigalg( getSigalg() );
        genKeystore.setStorepass( getStorepass() );
        genKeystore.setStoretype( getStoretype() );
        genKeystore.setValidity( getValidity() );
        genKeystore.setVerbose( verbose );
        genKeystore.setWorkingDir( workDirectory );
        genKeystore.setLog( log );

        genKeystore.execute();
    }

    private void deleteKeyStore()
    {
        File keyStore = null;
        if ( getKeystore() != null )
        {
            keyStore = new File( getKeystore() );
        }
        else
        {
            // FIXME decide if we really want this.
            // keyStore = new File( System.getProperty( "user.home") + File.separator + ".keystore" );
        }

        if ( keyStore == null )
        {
            return;
        }
        if ( keyStore.exists() )
        {
            if ( keyStore.delete() )
            {
                log.debug( "deleted keystore from: " + keyStore.getAbsolutePath() );
            }
            else
            {
                log.warn( "Couldn't delete keystore from: " + keyStore.getAbsolutePath() );
            }
        }
        else
        {
            log.debug( "Skipping deletion of non existing keystore: " + keyStore.getAbsolutePath() );
        }
    }
}
