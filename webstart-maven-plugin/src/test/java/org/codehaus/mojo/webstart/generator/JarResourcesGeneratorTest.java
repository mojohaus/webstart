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
package org.codehaus.mojo.webstart.generator;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.apache.maven.project.MavenProject;
import org.codehaus.mojo.webstart.JarResource;

/**
 * Tests the {@link JarResourcesGenerator} class.
 * 
 * @author Kevin Stembridge
 * @version $Revision$
 * 
 */
public class JarResourcesGeneratorTest extends TestCase
{
    
    public void testGetDependenciesText() throws Exception
    {
        
        MavenProject mavenProject = new MavenProject();
        File resourceLoaderPath = new File( System.getProperty( "java.io.tmpdir" ) );
        File outputFile = File.createTempFile( "bogus", "jnlp" );
        outputFile.deleteOnExit();
        
        File templateFile = File.createTempFile( "bogusTemplate", ".vm" );
        templateFile.deleteOnExit();
        
        List jarResources = new ArrayList();
        String mainClass = "fully.qualified.ClassName";
        
        
        JarResourcesGenerator generator = new JarResourcesGenerator( mavenProject,
                                                                     resourceLoaderPath,
                                                                     "default-jnlp-template.vm",
                                                                     outputFile,
                                                                     templateFile.getName(),
                                                                     jarResources,
                                                                     mainClass,
                                                                     "jar:file:/tmp/path/to/webstart-plugin.jar",
                                                                     null);
        
        //The list of jarResources is empty so the output text should be an empty string
        assertEquals("", generator.getDependenciesText());
        
        //Add some JarResources and confirm the correct output
        JarResource jarResource1 = buildJarResource( "href1", "1.1", "bogus.Class", true, true );
        JarResource jarResource2 = buildJarResource( "href2", "1.2", null, true, true );
        JarResource jarResource3 = buildJarResource( "href3", "1.3", null, false, true );
        JarResource jarResource4 = buildJarResource( "href4", "1.4", null, false, false );
        
        jarResources.add( jarResource1 );
        jarResources.add( jarResource2 );
        jarResources.add( jarResource3 );
        jarResources.add( jarResource4 );
        
        
        String expectedText = "\n<jar href=\"href1\" version=\"1.1\" main=\"true\"/>\n"
                             + "<jar href=\"href2\" version=\"1.2\"/>\n"
                             + "<jar href=\"href3\"/>\n";

        String actualText = generator.getDependenciesText( );
        
        Assert.assertEquals( expectedText, actualText );
        
        JarResourcesGenerator generator2 = new JarResourcesGenerator( mavenProject,
                                                                     resourceLoaderPath,
                                                                     "default-jnlp-template.vm",
                                                                     outputFile,
                                                                     templateFile.getName(),
                                                                     jarResources,
                                                                     mainClass,
                                                                     "jar:file:/tmp/path/to/webstart-plugin.jar",
                                                                     "myLib");

        String expectedText2 = "\n<jar href=\"myLib/href1\" version=\"1.1\" main=\"true\"/>\n"
                             + "<jar href=\"myLib/href2\" version=\"1.2\"/>\n"
                             + "<jar href=\"myLib/href3\"/>\n";

        String actualText2 = generator2.getDependenciesText( );

        Assert.assertEquals( expectedText2, actualText2 );

    }
    
    private JarResource buildJarResource( final String hrefValue,
                                          final String version, 
                                          final String mainClass,
                                          final boolean outputJarVersion,
                                          final boolean includeInJnlp )
    {
        
        return new JarResource( ) {

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
