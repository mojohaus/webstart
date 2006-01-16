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
 * Bean to host part of the JnlpMojo configuration.
 *
 * @author <a href="jerome@coffeebreaks.org">Jerome Lacoste</a>
 * @version $Id$
 */
public class JnlpConfig
{

    private String inputTemplate;

    private String outputFile;

    private String spec;

    private String version;

    private String codebase;

    private String href;

    private String mainClass;

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

    public void setCodebase( String codebase )
    {
        this.codebase = codebase;
    }

    public void setHref( String href )
    {
        this.href = href;
    }

    public void setMainClass( String mainClass )
    {
        this.mainClass = mainClass;
    }

    public String getInputTemplate()
    {
        return inputTemplate;
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

    public String getCodebase()
    {
        return codebase;
    }

    public String getHref()
    {
        return href;
    }

    public String getMainClass()
    {
        return mainClass;
    }
}
