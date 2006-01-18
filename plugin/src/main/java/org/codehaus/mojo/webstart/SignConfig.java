package org.codehaus.mojo.webstart;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License" );
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Bean that represents the JarSigner configuration
 *
 * @author <a href="jerome@coffeebreaks.org">Jerome Lacoste</a>
 * @version $Id$
 */
public class SignConfig {
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

    public void setKeystore( String keystore ) {
        this.keystore = keystore;
    }

    public void setKeyalg( String keyalg ) {
        this.keyalg = keyalg;
    }

    public void setKeysize( String keysize ) {
        this.keysize = keysize;
    }

    public void setSigalg( String sigalg ) {
        this.sigalg = sigalg;
    }

    public void setSigfile( String sigfile ) {
        this.sigfile = sigfile;
    }

    public void setStoretype( String storetype ) {
        this.storetype = storetype;
    }

    public void setStorepass( String storepass ) {
        this.storepass = storepass;
    }

    public void setKeypass( String keypass ) {
        this.keypass = keypass;
    }

    public void setValidity( String validity ) {
        this.validity = validity;
    }

    public void setDnameCn( String dnameCn ) {
        this.dnameCn = dnameCn;
    }

    public void setDnameOu( String dnameOu ) {
        this.dnameOu = dnameOu;
    }

    public void setDnameL( String dnameL ) {
        this.dnameL = dnameL;
    }

    public void setDnameSt( String dnameSt ) {
        this.dnameSt = dnameSt;
    }

    public void setDnameO( String dnameO ) {
        this.dnameO = dnameO;
    }

    public void setDnameC( String dnameC ) {
        this.dnameC = dnameC;
    }

    public void setAlias( String alias ) {
        this.alias = alias;
    }

    public void setVerify( boolean verify ) {
        this.verify = verify;
    }

    public String getKeystore() {
        return keystore;
    }

    public String getKeyalg() {
        return keyalg;
    }

    public String getKeysize() {
        return keysize;
    }

    public String getSigalg() {
        return sigalg;
    }

    public String getSigfile() {
        return sigfile;
    }

    public String getStoretype() {
        return storetype;
    }

    public String getStorepass() {
        return storepass;
    }

    public String getKeypass() {
        return keypass;
    }

    public String getValidity() {
        return validity;
    }

    public String getDnameCn() {
        return dnameCn;
    }

    public String getDnameOu() {
        return dnameOu;
    }

    public String getDnameL() {
        return dnameL;
    }

    public String getDnameSt() {
        return dnameSt;
    }

    public String getDnameO() {
        return dnameO;
    }

    public String getDnameC() {
        return dnameC;
    }

    public String getAlias() {
        return alias;
    }

    public boolean getVerify() {
        return verify;
    }

    public String getDname() {
        StringBuffer buffer = new StringBuffer( 128 );

        appendToDnameBuffer( dnameCn, buffer, "CN" );
        appendToDnameBuffer( dnameOu, buffer, "OU" );
        appendToDnameBuffer( dnameL, buffer, "L" );
        appendToDnameBuffer( dnameSt, buffer, "ST" );
        appendToDnameBuffer( dnameO, buffer, "O" );
        appendToDnameBuffer( dnameC, buffer, "C" );

        return buffer.toString();
    }

    private void appendToDnameBuffer( final String property, StringBuffer buffer, final String prefix ) {
        if ( property != null ) {
            if ( buffer.length() > 0)
            {
                buffer.append(", ");
            }
            buffer.append(prefix).append("=");
            buffer.append(property);
        }
    }
}
