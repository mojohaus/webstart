package org.codehaus.mojo.webstart.sign;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author <a href="jerome@coffeebreaks.org">Jerome Lacoste</a>
 * @version $Id$
 */
class SignConfigTest
{

    @Test
    void getDname()
    {
        SignConfig signConfig = new SignConfig();
        signConfig.setDnameCn( "www.example.com" );
        signConfig.setDnameOu( "None" );
        signConfig.setDnameL( "Seattle" );
        signConfig.setDnameSt( "Washington" );
        signConfig.setDnameO( "ExampleOrg" );
        signConfig.setDnameC( "US" );
        assertEquals( "CN=www.example.com, OU=None, L=Seattle, ST=Washington, O=ExampleOrg, C=US",
                      signConfig.getDname() );
    }

    @Test
    void getDnameMissing()
    {
        SignConfig signConfig = new SignConfig();
        signConfig.setDnameCn( "www.example.com" );
        signConfig.setDnameL( "Seattle" );
        signConfig.setDnameO( "ExampleOrg" );
        signConfig.setDnameC( "US" );
        assertEquals( "CN=www.example.com, L=Seattle, O=ExampleOrg, C=US", signConfig.getDname() );
    }

    @Test
    void getDnameWithCommaInOrganization()
    {
        SignConfig signConfig = new SignConfig();
        signConfig.setDnameCn( "www.example.com" );
        signConfig.setDnameOu( "None" );
        signConfig.setDnameL( "Seattle" );
        signConfig.setDnameSt( "Washington" );
        signConfig.setDnameO( "Some Company, Inc." );
        signConfig.setDnameC( "US" );
        assertEquals( "CN=www.example.com, OU=None, L=Seattle, ST=Washington, O=Some Company\\, Inc., C=US",
                      signConfig.getDname() );
    }
}
