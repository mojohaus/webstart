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

import java.io.File;

/**
 * Bean to host part of the JnlpMojo configuration.
 *
 * @author <a href="jerome@coffeebreaks.org">Jerome Lacoste</a>
 * @version $Id$
 */
public class JnlpConfig
{

    private String inputTemplateResourcePath;

    private String inputTemplate;

    private String outputFile;

    private String spec;

    private String version;

    private String j2seVersion;

    private String allPermissions;

    private String offlineAllowed;

    // private String codebase;

    private String href;

    private String mainClass;

    /**
     * The path containing any resources which will be added to the webstart artifact
     * Obsolete. Will be removed after 1.0-alpha- series.
     */
    private File resources;

    public void setInputTemplateResourcePath( String inputTemplateResourcePath )
    {
        this.inputTemplateResourcePath = inputTemplateResourcePath;
    }

    public void setInputTemplate( String inputTemplate )
    {
        this.inputTemplate = inputTemplate;
    }

    public void setOutputFile( String outputFile )
    {
        this.outputFile = outputFile;
    }

    public void setSpec( String spec )
    {
        this.spec = spec;
    }

    public void setVersion( String version )
    {
        this.version = version;
    }

    public void setJ2seVersion( String j2seVersion )
    {
        this.j2seVersion = j2seVersion;
    }

    public void setOfflineAllowed( String offlineAllowed )
    {
        this.offlineAllowed = offlineAllowed;
    }

    public void setAllPermissions( String allPermissions )
    {
        this.allPermissions = allPermissions;
    }
    /*
    public void setCodebase( String codebase )
    {
        this.codebase = codebase;
    }*/

    public void setHref( String href )
    {
        this.href = href;
    }

    public void setMainClass( String mainClass )
    {
        this.mainClass = mainClass;
    }

    public String getInputTemplateResourcePath()
    {
        return inputTemplateResourcePath;
    }

    public String getInputTemplate()
    {
        return inputTemplate;
    }

    public void setResources( File resources )
    {
        this.resources = resources;
    }

    public String getOutputFile()
    {
        return outputFile;
    }

    public String getSpec()
    {
        return spec;
    }

    public String getVersion()
    {
        return version;
    }

    public String getJ2seVersion()
    {
        return j2seVersion;
    }

    public String getAllPermissions()
    {
        return allPermissions;
    }

    public String getOfflineAllowed()
    {
        return offlineAllowed;
    }

    public File getResources()
    {
        return resources;
    }

    /*
    public String getCodebase()
    {
        return codebase;
    }
    */

    public String getHref()
    {
        return href;
    }

    public String getMainClass()
    {
        return mainClass;
    }
}
