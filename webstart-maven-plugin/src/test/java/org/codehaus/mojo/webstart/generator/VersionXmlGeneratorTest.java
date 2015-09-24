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
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.mojo.webstart.ResolvedJarResource;
import org.codehaus.plexus.util.ReaderFactory;
import org.custommonkey.xmlunit.Diff;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
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
    extends TestCase
{

    private final File outputDir;

    private File expectedFile;

    /**
     * Creates a new {@code VersionXmlGeneratorTest}.
     */
    public VersionXmlGeneratorTest()
    {
        super();

        this.outputDir = new File( System.getProperty( "java.io.tmpdir" ), "versionXmlDir" );
        this.outputDir.deleteOnExit();
        this.outputDir.mkdir();

    }

    /**
     * {@inheritDoc}
     */
    public void setUp()
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
    public void tearDown()
    {
        this.expectedFile.delete();
    }

    public void testWithNullOutputDir()
        throws MojoExecutionException
    {

        try
        {
            new VersionXmlGenerator( "utf-8" ).generate( null, new ArrayList<ResolvedJarResource>() );
            Assert.fail( "Should have thrown an IllegalArgumentException" );
        }
        catch ( IllegalArgumentException e )
        {
            //do nothing, test succeeded
        }

    }

    public void testWithEmptyJarResourcesList()
        throws MojoExecutionException, IOException, SAXException, ParserConfigurationException
    {

        List<ResolvedJarResource> jarResources = new ArrayList<ResolvedJarResource>();
        new VersionXmlGenerator( "utf-8" ).generate( this.outputDir, jarResources );

        Assert.assertTrue( "Assert expectedFile exists", this.expectedFile.exists() );

        String expectedXml = "<jnlp-versions></jnlp-versions>";
        String actualXml = readFileContents( this.expectedFile );

        Diff diff = new Diff( expectedXml, actualXml );
        Assert.assertTrue( diff.toString(), diff.similar() );

    }

    public void testWithMultiJarResources()
        throws IOException, SAXException, ParserConfigurationException, MojoExecutionException
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

        List<ResolvedJarResource> jarResources = new ArrayList<ResolvedJarResource>( 2 );
        jarResources.add( jar1 );
        jarResources.add( jar2 );

        new VersionXmlGenerator( "utf-8" ).generate( this.outputDir, jarResources );

        String actualXml = readFileContents( this.expectedFile );

        Diff diff = new Diff( "<jnlp-versions>" + "  <resource>" + "    <pattern>" + "      <name>bogus1.txt</name>" +
                                  "      <version-id>1.0</version-id>" + "    </pattern>" +
                                  "    <file>bogus1.txt</file>" + "  </resource>" + "  <resource>" + "    <pattern>" +
                                  "      <name>bogus2.txt</name>" + "      <version-id>1.0</version-id>" +
                                  "    </pattern>" + "    <file>bogus2.txt</file>" + "  </resource>" +
                                  "</jnlp-versions>", actualXml );
        Assert.assertTrue( diff.toString(), diff.similar() );

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
