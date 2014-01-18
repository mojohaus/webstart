package org.codehaus.mojo.webstart.generator;

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

import junit.framework.Assert;
import junit.framework.TestCase;

import org.apache.maven.plugin.logging.SystemStreamLog;
import org.apache.maven.plugin.testing.stubs.ArtifactStub;
import org.apache.maven.project.MavenProject;
import org.codehaus.mojo.webstart.ResolvedJarResource;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Tests the {@link JarResourcesGenerator} class.
 *
 * @author Kevin Stembridge
 * @version $Revision$
 */
public class JarResourcesGeneratorTest
    extends TestCase
{
    public static final String EOL = System.getProperty( "line.separator" );

    public void testGetDependenciesText()
        throws Exception
    {

        MavenProject mavenProject = new MavenProject();
        File resourceLoaderPath = new File( System.getProperty( "java.io.tmpdir" ) );
        File outputFile = File.createTempFile( "bogus", "jnlp" );
        outputFile.deleteOnExit();

        File templateFile = File.createTempFile( "bogusTemplate", ".vm" );
        templateFile.deleteOnExit();

        List<ResolvedJarResource> jarResources = new ArrayList<ResolvedJarResource>();
        String mainClass = "fully.qualified.ClassName";

        GeneratorTechnicalConfig generatorTechnicalConfig =
            new GeneratorTechnicalConfig( mavenProject, resourceLoaderPath, "default-jnlp-template.vm",
                                          outputFile, templateFile.getName(), mainClass,
                                          "jar:file:/tmp/path/to/webstart-plugin.jar", "utf-8" );
        JarResourceGeneratorConfig jarResourceGeneratorConfig = new JarResourceGeneratorConfig( jarResources, null, null, null );
        JarResourcesGenerator generator  =
            new JarResourcesGenerator( new SystemStreamLog(), generatorTechnicalConfig, jarResourceGeneratorConfig );

//        JarResourcesGenerator generator =
//            new JarResourcesGenerator( new SystemStreamLog(), mavenProject, resourceLoaderPath,
//                                       "default-jnlp-template.vm", outputFile, templateFile.getName(), jarResources,
//                                       mainClass, "jar:file:/tmp/path/to/webstart-plugin.jar", null, "utf-8" );

        //The list of jarResources is empty so the output text should be an empty string
        assertEquals( "", generator.getDependenciesText() );

        //Add some JarResources and confirm the correct output
        ResolvedJarResource jarResource1 = buildJarResource( "href1", "1.1", "bogus.Class", true, true );
        ResolvedJarResource jarResource2 = buildJarResource( "href2", "1.2", null, true, true );
        ResolvedJarResource jarResource3 = buildJarResource( "href3", "1.3", null, false, true );
        ResolvedJarResource jarResource4 = buildJarResource( "href4", "1.4", null, false, false );

        jarResources.add( jarResource1 );
        jarResources.add( jarResource2 );
        jarResources.add( jarResource3 );
        jarResources.add( jarResource4 );

        String expectedText =EOL + "<jar href=\"href1\" version=\"1.1\" main=\"true\"/>" + 
        		EOL + "<jar href=\"href2\" version=\"1.2\"/>" +
        		EOL + "<jar href=\"href3\"/>" + EOL;

        String actualText = generator.getDependenciesText();

        Assert.assertEquals( expectedText, actualText );

        JarResourceGeneratorConfig jarResourceGeneratorConfig2 = new JarResourceGeneratorConfig( jarResources, "myLib", null, null );
        JarResourcesGenerator generator2  =
            new JarResourcesGenerator( new SystemStreamLog(), generatorTechnicalConfig, jarResourceGeneratorConfig2 );

//        JarResourcesGenerator generator2 =
//            new JarResourcesGenerator( new SystemStreamLog(), mavenProject, resourceLoaderPath,
//                                       "default-jnlp-template.vm", outputFile, templateFile.getName(), jarResources,
//                                       mainClass, "jar:file:/tmp/path/to/webstart-plugin.jar", "myLib", "utf-8" );

        String expectedText2 = EOL + "<jar href=\"myLib/href1\" version=\"1.1\" main=\"true\"/>" +
        		EOL + "<jar href=\"myLib/href2\" version=\"1.2\"/>" + 
        		EOL + "<jar href=\"myLib/href3\"/>" + EOL;

        String actualText2 = generator2.getDependenciesText();

        Assert.assertEquals( expectedText2, actualText2 );

    }

    private ResolvedJarResource buildJarResource( final String hrefValue, final String version, final String mainClass,
                                                  final boolean outputJarVersion, final boolean includeInJnlp )
    {

        return new ResolvedJarResource( new ArtifactStub() )
        {

            /**
             * {@inheritDoc}
             */
            public String getHrefValue()
            {
                return hrefValue;
            }

            /**
             * {@inheritDoc}
             */
            public String getMainClass()
            {
                return mainClass;
            }

            /**
             * {@inheritDoc}
             */
            public String getVersion()
            {
                return version;
            }

            /**
             * {@inheritDoc}
             */
            public boolean isIncludeInJnlp()
            {
                return includeInJnlp;
            }

            /**
             * {@inheritDoc}
             */
            public boolean isOutputJarVersion()
            {
                return outputJarVersion;
            }

        };

    }

}
