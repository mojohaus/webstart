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

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.mojo.webstart.JarResource;
import org.codehaus.plexus.util.WriterFactory;

/**
 * This class generates a <code>version.xml</code> file for a given collection of
 * <code>JarResource</code> objects in the format expected by the <code>JnlpDownloadServlet</code>.
 * 
 * <p>
 * For a full description of the version.xml syntax, refer to the
 * <a href="http://java.sun.com/javase/6/docs/technotes/guides/javaws/developersguide/downloadservletguide.html">
 * JnlpDownloadServlet Guide</a>
 * </p>
 *
 * @author Kevin Stembridge
 * @since 1.0-alpha-2
 * @version $Revision$
 *
 */
public class VersionXmlGenerator
{
    
    /**
     * Creates a new {@code VersionXmlGenerator}.
     */
    public VersionXmlGenerator()
    {
        //do nothing
    }
    
    /**
     * Generates a file named <code>version.xml</code> in the given <code>outputDir</code>.
     * The generated file will contain resource elements for each of the JarResource
     * objects in the given collection.
     *
     * @param outputDir The directory in which the file will be generated. Must not be null.
     * @param jarResources The collection of JarResources for which a resource
     * element will be created in the generated file.
     * @throws MojoExecutionException if an error occurs generating the file.
     */
    public void generate( File outputDir, Collection/*JarResource*/ jarResources ) throws MojoExecutionException 
    {
        
        if ( outputDir == null )
        {
            throw new IllegalArgumentException( "outputDir must not be null" );
        }
        
        BufferedWriter writer = null;
        
        try 
        {
            File versionXmlFile = new File( outputDir, "version.xml" );
            writer = new BufferedWriter( WriterFactory.newXmlWriter( versionXmlFile ) );
            
            generateXml( writer, jarResources );
            
        } 
        catch ( IOException e ) 
        {
            throw new MojoExecutionException( "Unable to create the version.xml file", e ); 
        }
        finally 
        {
            if ( writer != null ) 
            {
                try 
                {
                    writer.close();
                }
                catch ( IOException e )
                {
                    // do nothing
                }
            }
        }
        
    }
        
    private void generateXml( BufferedWriter writer, Collection jarResources ) throws IOException
    {
        
        writer.write( "<?xml version=\"1.0\"?>" );
        writer.newLine();
        writer.write( "<jnlp-versions>" );
        writer.newLine();
        
        for ( Iterator itr = jarResources.iterator(); itr.hasNext(); ) 
        {
            
            JarResource jarResource = (JarResource) itr.next();
            writer.write( "  <resource>" );
            writer.newLine();
            writer.write( "    <pattern>" );
            writer.newLine();
            writer.write( "      <name>" );
            writer.write( jarResource.getHrefValue() );
            writer.write( "</name>" );
            writer.newLine();
            writer.write( "      <version-id>" );
            writer.write( jarResource.getVersion() );
            writer.write( "</version-id>" );
            writer.newLine();
            writer.write( "    </pattern>" );
            writer.newLine();
            writer.write( "    <file>" );
            writer.write( jarResource.getArtifact().getFile().getName() );
            writer.write( "</file>" );
            writer.newLine();
            writer.write( "  </resource>" );
            writer.newLine();
        }
        
        writer.write( "</jnlp-versions>" );
        writer.newLine();
    }
}
