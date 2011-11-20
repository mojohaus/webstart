/*
 *  Copyright 2009 chemit.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */

package org.codehaus.mojo.webstart.generator;

import org.apache.maven.project.MavenProject;
import org.apache.velocity.VelocityContext;
import org.codehaus.mojo.webstart.AbstractJnlpMojo;
import org.codehaus.mojo.webstart.JnlpExtension;

import java.io.File;
import java.util.List;

/**
 * To generate an extension jnlp file.
 *
 * @author chemit
 */
public class ExtensionGenerator
    extends AbstractGenerator
{

    private AbstractJnlpMojo config;

    private JnlpExtension extension;

    /**
     * @param mavenProject
     * @param task
     * @param extension
     * @param defaultTemplateResourceName
     * @param resourceLoaderPath          used to find the template in conjunction to inputFileTemplatePath
     * @param outputFile
     * @param inputFileTemplatePath       relative to resourceLoaderPath
     * @param mainClass
     * @param webstartJarURL
     */
    public ExtensionGenerator( MavenProject mavenProject, AbstractJnlpMojo task, JnlpExtension extension,
                               String defaultTemplateResourceName, File resourceLoaderPath, File outputFile,
                               String inputFileTemplatePath, String mainClass, String webstartJarURL,
                               String encoding)
    {

        super( mavenProject, resourceLoaderPath, defaultTemplateResourceName, outputFile, inputFileTemplatePath,
               mainClass, webstartJarURL, encoding);

        this.config = task;
        this.extension = extension;
    }

    protected VelocityContext createAndPopulateContext()
    {
        VelocityContext context = super.createAndPopulateContext();
        // add the extension in velocity context
        context.put( "extension", extension );
        return context;
    }


    /**
     * {@inheritDoc}
     */
    protected String getDependenciesText()
    {
        List dependencies = (List) config.getExtensionsJnlpArtifacts().get( extension );

        return indentText( 4, Generator.getDependenciesText( config, dependencies ) );
    }

}
