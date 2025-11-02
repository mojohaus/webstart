package org.codehaus.mojo.webstart.generator;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.versioning.VersionRange;
import org.codehaus.mojo.webstart.ResolvedJarResource;
import org.codehaus.plexus.util.ReaderFactory;
import org.custommonkey.xmlunit.Diff;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Tests the {@link VersionXmlGenerator} class.
 *
 * @author Kevin Stembridge
 * @author $LastChangedBy$
 * @version $Revision$
 * @since 7 Jun 2007
 */
public class VersionXmlGeneratorTest
{

    private final File outputDir;

    private File expectedFile;

    /**
     * Creates a new {@code VersionXmlGeneratorTest}.
     */
    public VersionXmlGeneratorTest()
    {

        this.outputDir = new File( System.getProperty( "java.io.tmpdir" ), "versionXmlDir" );
        this.outputDir.deleteOnExit();
        this.outputDir.mkdir();

    }

    /**
     * {@inheritDoc}
     */
    @BeforeEach
    void setUp()
    {
        this.expectedFile = new File( this.outputDir, "version.xml" );
        this.expectedFile.deleteOnExit();

        if ( this.expectedFile.exists() )
        {
            if ( !this.expectedFile.delete() )
            {
                throw new RuntimeException( "Unable to delete a file from a previous test run [" + expectedFile + "]" );
            }

        }

    }

    /**
     * {@inheritDoc}
     */
    @AfterEach
    void tearDown()
    {
        this.expectedFile.delete();
    }

    @Test
    void withNullOutputDir()
        throws Exception
    {

        try
        {
            new VersionXmlGenerator( "utf-8" ).generate( null, new ArrayList() );
            Assertions.fail( "Should have thrown an IllegalArgumentException" );
        }
        catch ( IllegalArgumentException e )
        {
            //do nothing, test succeeded
        }

    }

    @Test
    void withEmptyJarResourcesList()
        throws Exception
    {

        List<ResolvedJarResource> jarResources = new ArrayList<>();
        new VersionXmlGenerator( "utf-8" ).generate( this.outputDir, jarResources );

        Assertions.assertTrue( this.expectedFile.exists(), "Assert expectedFile exists" );

        String expectedXml = "<jnlp-versions></jnlp-versions>";
        String actualXml = readFileContents( this.expectedFile );

        Diff diff = new Diff( expectedXml, actualXml );
        Assertions.assertTrue( diff.similar(), diff.toString() );

    }

    @Test
    void withMultiJarResources()
        throws Exception
    {

        Artifact artifact1 =
            new DefaultArtifact( "groupId", "artifactId1", VersionRange.createFromVersion( "1.0" ), "scope", "jar",
                                 "classifier", null );
        artifact1.setFile( new File( "bogus1.txt" ) );

        Artifact artifact2 =
            new DefaultArtifact( "groupId", "artifactId2", VersionRange.createFromVersion( "1.0" ), "scope", "jar",
                                 "classifier", null );
        artifact2.setFile( new File( "bogus2.txt" ) );

        ResolvedJarResource jar1 = new ResolvedJarResource( artifact1 );
        ResolvedJarResource jar2 = new ResolvedJarResource( artifact2 );

//        jar1.setArtifact( artifact1 );
//        jar2.setArtifact( artifact2 );

        List<ResolvedJarResource> jarResources = new ArrayList<>( 2 );
        jarResources.add( jar1 );
        jarResources.add( jar2 );

        new VersionXmlGenerator( "utf-8" ).generate( this.outputDir, jarResources );

        String actualXml = readFileContents( this.expectedFile );

        String expected = "<?xml version=\"1.0\"?><jnlp-versions>" + "  <resource>" + "    <pattern>" + "      <name>bogus1.txt</name>" +
                "      <version-id>1.0</version-id>" + "    </pattern>" +
                "    <file>artifactId1-1.0-classifier.jar</file>" + "  </resource>" + "  <resource>" + "    <pattern>" +
                "      <name>bogus2.txt</name>" + "      <version-id>1.0</version-id>" +
                "    </pattern>" + "    <file>artifactId2-1.0-classifier.jar</file>" + "  </resource>" +
                "</jnlp-versions>";
        Assertions.assertEquals( actualXml, expected );
        Diff diff = new Diff( expected, actualXml );
        Assertions.assertTrue( diff.similar(), diff.toString() );

    }

    private String readFileContents( File file )
        throws IOException
    {

        BufferedReader reader = null;
        StringBuilder fileContents = new StringBuilder();

        try
        {
            reader = new BufferedReader( ReaderFactory.newXmlReader( file ) );

            String line = null;

            while ( ( line = reader.readLine() ) != null )
            {
                fileContents.append( line );
            }

            return fileContents.toString();

        }
        finally
        {
            if ( reader != null )
            {
                reader.close();
            }
        }

    }

}
