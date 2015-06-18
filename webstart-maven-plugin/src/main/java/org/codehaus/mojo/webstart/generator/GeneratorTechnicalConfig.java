package org.codehaus.mojo.webstart.generator;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with work for additional information
 * regarding copyright ownership.  The ASF licenses file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use file except in compliance
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

import org.apache.maven.project.MavenProject;

import java.io.File;

/**
 * Created on 1/6/14.
 *
 * @author Tony Chemit <chemit@codelutin.com>
 * @since 1.0-beta-5
 */
public class GeneratorTechnicalConfig
{

    private final MavenProject mavenProject;

    private final File outputFile;

    private final String mainClass;

    private final String encoding;

    private final File resourceLoaderPath;

    private final String defaultTemplateResourceName;

    private final String inputFileTemplatePath;

    private final String webstartJarURL;

    public GeneratorTechnicalConfig( MavenProject mavenProject, File resourceLoaderPath,
                                     String defaultTemplateResourceName, File outputFile, String inputFileTemplatePath,
                                     String mainClass, String webstartJarURL, String encoding )
    {
        if ( mavenProject == null )
        {
            throw new IllegalArgumentException( "mavenProject must not be null" );
        }

        if ( resourceLoaderPath == null )
        {
            throw new IllegalArgumentException( "resourceLoaderPath must not be null" );
        }

        if ( outputFile == null )
        {
            throw new IllegalArgumentException( "outputFile must not be null" );
        }

//        if ( mainClass == null )
//        {
//            throw new IllegalArgumentException( "mainClass must not be null" );
//        }

        this.mavenProject = mavenProject;
        this.outputFile = outputFile;
        this.mainClass = mainClass;
        this.encoding = encoding;
        this.resourceLoaderPath = resourceLoaderPath;
        this.defaultTemplateResourceName = defaultTemplateResourceName;
        this.inputFileTemplatePath = inputFileTemplatePath;
        this.webstartJarURL = webstartJarURL;
    }

    public MavenProject getMavenProject()
    {
        return mavenProject;
    }

    public File getOutputFile()
    {
        return outputFile;
    }

    public String getMainClass()
    {
        return mainClass;
    }

    public String getEncoding()
    {
        return encoding;
    }

    public File getResourceLoaderPath()
    {
        return resourceLoaderPath;
    }

    public String getDefaultTemplateResourceName()
    {
        return defaultTemplateResourceName;
    }

    public String getInputFileTemplatePath()
    {
        return inputFileTemplatePath;
    }

    public String getWebstartJarURL()
    {
        return webstartJarURL;
    }
}
