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

import junit.framework.TestCase;

/**
 * @author <a href="jerome@coffeebreaks.org">Jerome Lacoste</a>
 * @version $Id$
 */
public class SignConfigTest
    extends TestCase
{

    public void testGetDname()
    {
        SignConfig signConfig = new DefaultSignConfig();
        signConfig.setDnameCn( "www.example.com" );
        signConfig.setDnameOu( "None" );
        signConfig.setDnameL( "Seattle" );
        signConfig.setDnameSt( "Washington" );
        signConfig.setDnameO( "ExampleOrg" );
        signConfig.setDnameC( "US" );
        assertEquals( "CN=www.example.com, OU=None, L=Seattle, ST=Washington, O=ExampleOrg, C=US",
                      signConfig.getDname() );
    }

    public void testGetDnameMissing()
    {
        SignConfig signConfig = new DefaultSignConfig();
        signConfig.setDnameCn( "www.example.com" );
        //signConfig.setDnameOu( "None" );
        signConfig.setDnameL( "Seattle" );
        //signConfig.setDnameSt( "Washington" );
        signConfig.setDnameO( "ExampleOrg" );
        signConfig.setDnameC( "US" );
        assertEquals( "CN=www.example.com, L=Seattle, O=ExampleOrg, C=US", signConfig.getDname() );
    }
}
