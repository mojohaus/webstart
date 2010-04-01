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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.mojo.webstart.JarResource;
import org.codehaus.plexus.util.ReaderFactory;
import org.custommonkey.xmlunit.Diff;
import org.xml.sax.SAXException;

/**
 * 
 * Tests the {@link VersionXmlGenerator} class.
 *
 * @author Kevin Stembridge
 * @author $LastChangedBy$
 * @since 7 Jun 2007
 * @version $Revision$
 *
 */
public class VersionXmlGeneratorTest extends TestCase
{
    
    private final File outputDir;
    private File expectedFile;
  
    /**
     * Creates a new {@code VersionXmlGeneratorTest}.
     */
    public VersionXmlGeneratorTest()
    {
        super();
       
        this.outputDir = new File(System.getProperty( "java.io.tmpdir" ), "versionXmlDir" );
        this.outputDir.deleteOnExit();
        this.outputDir.mkdir();
        
    }
    
    public void setUp() 
    {
        this.expectedFile = new File( this.outputDir, "version.xml" );
        this.expectedFile.deleteOnExit();
        
        if ( this.expectedFile.exists() )
        {
            if ( ! this.expectedFile.delete() )
            {
                throw new RuntimeException( "Unable to delete a file from a previous test run [" + expectedFile + "]" );
            }
            
        }
        
    }
    
    public void tearDown()
    {
        this.expectedFile.delete();
    }
   
    public void testWithNullOutputDir() throws MojoExecutionException 
    {
        
        try 
        {
            new VersionXmlGenerator().generate( null, new ArrayList() );
            Assert.fail( "Should have thrown an IllegalArgumentException");
        }
        catch (IllegalArgumentException e)
        {
            //do nothing, test succeeded
        }
        
    }
    
    public void testWithEmptyJarResourcesList() throws MojoExecutionException, IOException, SAXException, ParserConfigurationException
    {
        
        List jarResources = new ArrayList();
        new VersionXmlGenerator().generate( this.outputDir, jarResources );
        
        Assert.assertTrue( "Assert expectedFile exists", this.expectedFile.exists() );
        
        String expectedXml = "<jnlp-versions></jnlp-versions>";
        String actualXml = readFileContents( this.expectedFile );
        
        Diff diff = new Diff( expectedXml, actualXml );
        Assert.assertTrue( diff.toString(), diff.similar() );
        
    }
    
    public void testWithMultiJarResources() throws IOException, SAXException, ParserConfigurationException, MojoExecutionException
    {
        
        Artifact artifact1 = new DefaultArtifact( "groupId", 
                                                  "artifactId1", 
                                                  VersionRange.createFromVersion("1.0"),
                                                  "scope", 
                                                  "jar", 
                                                  "classifier", 
                                                  null);
        artifact1.setFile( new File( "bogus1.txt" ) );
        
        Artifact artifact2 = new DefaultArtifact( "groupId", 
                                                  "artifactId2", 
                                                  VersionRange.createFromVersion("1.0"),
                                                  "scope", 
                                                  "jar", 
                                                  "classifier", 
                                                  null);
        artifact2.setFile( new File( "bogus2.txt" ) );
        
        JarResource jar1 = new JarResource();
        JarResource jar2 = new JarResource();
        
        jar1.setArtifact( artifact1 );
        jar2.setArtifact( artifact2 );
        
        List jarResources = new ArrayList( 2 );
        jarResources.add( jar1 );
        jarResources.add( jar2 );
        
        StringBuffer expectedXml = new StringBuffer();
        expectedXml.append( "<jnlp-versions>" )
                   .append( "  <resource>" )
                   .append( "    <pattern>" )
                   .append( "      <name>bogus1.txt</name>" )
                   .append( "      <version-id>1.0</version-id>" )
                   .append( "    </pattern>" )
                   .append( "    <file>bogus1.txt</file>")
                   .append( "  </resource>" )
                   .append( "  <resource>" )
                   .append( "    <pattern>" )
                   .append( "      <name>bogus2.txt</name>" )
                   .append( "      <version-id>1.0</version-id>" )
                   .append( "    </pattern>" )
                   .append( "    <file>bogus2.txt</file>" )
                   .append( "  </resource>" )
                   .append( "</jnlp-versions>" );

        new VersionXmlGenerator().generate( this.outputDir, jarResources );
        
        String actualXml = readFileContents( this.expectedFile );
        
        Diff diff = new Diff( expectedXml.toString(), actualXml );
        Assert.assertTrue( diff.toString(), diff.similar() );
        
    }
    
    private String readFileContents( File file ) throws IOException
    {
        
        BufferedReader reader = null;
        StringBuffer fileContents = new StringBuffer();
        
        try 
        {
            reader = new BufferedReader( ReaderFactory.newXmlReader( file ) );
            
            String line = null;
            
            while ( (line = reader.readLine()) != null ) 
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
