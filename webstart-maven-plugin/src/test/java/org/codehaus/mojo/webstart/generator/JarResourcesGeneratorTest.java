package org.codehaus.mojo.webstart.generator;

import org.apache.maven.plugin.logging.SystemStreamLog;
import org.apache.maven.plugin.testing.stubs.ArtifactStub;
import org.apache.maven.project.MavenProject;
import org.codehaus.mojo.webstart.ResolvedJarResource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests the {@link JarResourcesGenerator} class.
 *
 * @author Kevin Stembridge
 * @version $Revision$
 */
public class JarResourcesGeneratorTest
{
    public static final String EOL = System.getProperty( "line.separator" );

    @Test
    void getDependenciesText()
        throws Exception
    {

        MavenProject mavenProject = new MavenProject();
        File resourceLoaderPath = new File( System.getProperty( "java.io.tmpdir" ) );
        File outputFile = File.createTempFile( "bogus", "jnlp" );
        outputFile.deleteOnExit();

        File templateFile = File.createTempFile( "bogusTemplate", ".vm" );
        templateFile.deleteOnExit();

        List<ResolvedJarResource> jarResources = new ArrayList<>();
        String mainClass = "fully.qualified.ClassName";

        GeneratorTechnicalConfig generatorTechnicalConfig =
            new GeneratorTechnicalConfig( mavenProject, resourceLoaderPath, "default-jnlp-template.vm",
                                          outputFile, templateFile.getName(), mainClass,
                                          "jar:file:/tmp/path/to/webstart-plugin.jar", "utf-8" );
        JarResourceGeneratorConfig jarResourceGeneratorConfig = new JarResourceGeneratorConfig( jarResources, null, null, null, null );
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

        Assertions.assertEquals( expectedText, actualText );

        JarResourceGeneratorConfig jarResourceGeneratorConfig2 = new JarResourceGeneratorConfig( jarResources, "myLib", null, null, null );
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

        Assertions.assertEquals( expectedText2, actualText2 );

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
