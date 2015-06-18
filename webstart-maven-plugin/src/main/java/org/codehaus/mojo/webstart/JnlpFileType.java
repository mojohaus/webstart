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

/**
 * Which type of jnlp file to generate.
 * <p/>
 * Created on 1/12/14.
 *
 * @author Tony Chemit <chemit@codelutin.com>
 * @since 1.0-beta-6
 */
public enum JnlpFileType
{
    application( "default-jnlp-template.vm", true ),
    component( "default-jnlp-component-template.vm", false ),
    installer( "default-jnlp-installer-template.vm", false );

    /**
     * Default template to use to generate a jnlp type of this type.
     */
    private final String defaultTemplateName;

    /**
     * Is a jnlp file of this type requires a main class?
     */
    private final boolean requireMainClass;

    JnlpFileType( String defaultTemplateName, boolean requireMainClass )
    {
        this.defaultTemplateName = defaultTemplateName;
        this.requireMainClass = requireMainClass;
    }

    public String getDefaultTemplateName()
    {
        return defaultTemplateName;
    }

    public boolean isRequireMainClass()
    {
        return requireMainClass;
    }
}
