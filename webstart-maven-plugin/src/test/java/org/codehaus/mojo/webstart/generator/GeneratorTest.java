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

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.artifact.versioning.VersionRange;
import org.codehaus.mojo.webstart.JnlpConfig;
import org.codehaus.mojo.webstart.dependency.filenaming.DependencyFilenameStrategy;
import org.codehaus.mojo.webstart.dependency.filenaming.FullDependencyFilenameStrategy;
import org.codehaus.mojo.webstart.dependency.filenaming.SimpleDependencyFilenameStrategy;

/**
 * @author <a href="jerome@coffeebreaks.org">Jerome Lacoste</a>
 * @version $Id$
 */
public class GeneratorTest
    extends TestCase
{
    protected Artifact artifact1;

    protected Artifact artifact2;

    protected Artifact artifact3;

    private List<Artifact> artifacts;

    public static final String EOL = System.getProperty( "line.separator" );
    
    @Override
    public void setUp()
        throws Exception
    {
        super.setUp();
        DefaultArtifactHandler artifactHandler = new DefaultArtifactHandler( "jar" );
        artifact1 =
            new DefaultArtifact( "groupId", "artifact1", VersionRange.createFromVersion( "1.0" ), "scope", "jar",
                                 "classifier", artifactHandler );
        artifact1.setFile( new File( "artifact1-1.0.jar" ) );
        artifact2 =
            new DefaultArtifact( "groupId", "artifact2", VersionRange.createFromVersion( "1.5" ), null, "jar", "",
                                 artifactHandler );
        artifact2.setFile( new File( "artifact2-1.5.jar" ) );

        // add a SNAPSHOT artifact
        artifact3 =
                new DefaultArtifact( "groupId", "artifact3", VersionRange.createFromVersion( "1.5-SNAPSHOT" ), null, "jar", "",
                                     artifactHandler );
        artifact3.setBaseVersion("1.5-15012014121212");
        artifact3.setFile( new File( "artifact3-1.5-15012014121212.jar" ) );
        artifacts = new ArrayList<Artifact>();
        artifacts.add( artifact1 );
        artifacts.add( artifact2 );
        artifacts.add( artifact3 );
    }

    public void testGetDependenciesText()
        throws Exception
    {

        DependencyFilenameStrategy dependencyFilenameStrategy = new SimpleDependencyFilenameStrategy();

        String codebase = null;
        JnlpConfig jnlp = null;

        GeneratorConfig generatorConfigEmpty =
            new GeneratorConfig( null, false, false, false, artifact1, dependencyFilenameStrategy,
                                 Collections.<Artifact>emptyList(), null, codebase, jnlp );

        assertEquals( "", Generator.getDependenciesText( generatorConfigEmpty ) );

        GeneratorConfig generatorConfig =
            new GeneratorConfig( null, false, false, false, artifact1, dependencyFilenameStrategy, artifacts, null, codebase,
                                 jnlp );

        assertEquals( EOL + "<jar href=\"artifact1-classifier-1.0.jar\" main=\"true\"/>" + 
        				EOL + "<jar href=\"artifact2-1.5.jar\"/>" + 
        				EOL +"<jar href=\"artifact3-1.5-SNAPSHOT.jar\"/>" + EOL,
            Generator.getDependenciesText( generatorConfig ) );

        GeneratorConfig generatorConfig2 =
            new GeneratorConfig( null, false, true, false, artifact1, dependencyFilenameStrategy, artifacts, null, codebase,
                                 jnlp );

        assertEquals( EOL + "<property name=\"jnlp.versionEnabled\" value=\"true\" />" +
        				EOL + "<jar href=\"artifact1-classifier.jar\" version=\"1.0\" main=\"true\"/>" +
        				EOL + "<jar href=\"artifact2.jar\" version=\"1.5\"/>" + 
        				EOL +"<jar href=\"artifact3.jar\" version=\"1.5-SNAPSHOT\"/>" + EOL,
                      Generator.getDependenciesText( generatorConfig2 ) );
    }

    public void testGetDependenciesTextWithFullNaming()
        throws Exception
    {

        DependencyFilenameStrategy dependencyFilenameStrategy = new FullDependencyFilenameStrategy();

        String codebase = null;
        JnlpConfig jnlp = null;

        GeneratorConfig generatorConfigEmpty =
            new GeneratorConfig( null, false, false, false, artifact1, dependencyFilenameStrategy,
                                 Collections.<Artifact>emptyList(), null, codebase, jnlp );

        assertEquals( "", Generator.getDependenciesText( generatorConfigEmpty ) );

        GeneratorConfig generatorConfig =
            new GeneratorConfig( null, false, false, false, artifact1, dependencyFilenameStrategy, artifacts, null, codebase,
                                 jnlp );

        assertEquals( EOL + "<jar href=\"groupId-artifact1-classifier-1.0.jar\" main=\"true\"/>" +
        				EOL +"<jar href=\"groupId-artifact2-1.5.jar\"/>" + 
        				EOL +"<jar href=\"groupId-artifact3-1.5-SNAPSHOT.jar\"/>" + EOL,
                      Generator.getDependenciesText( generatorConfig ) );

        GeneratorConfig generatorConfig2 =
            new GeneratorConfig( null, false, true, false, artifact1, dependencyFilenameStrategy, artifacts, null, codebase,
                                 jnlp );

        assertEquals( EOL + "<property name=\"jnlp.versionEnabled\" value=\"true\" />" +
        				EOL + "<jar href=\"groupId-artifact1-classifier.jar\" version=\"1.0\" main=\"true\"/>" +
        				EOL + "<jar href=\"groupId-artifact2.jar\" version=\"1.5\"/>" + 
        				EOL +"<jar href=\"groupId-artifact3.jar\" version=\"1.5-SNAPSHOT\"/>" + EOL,
                      Generator.getDependenciesText( generatorConfig2 ) );
    }

    public void testGetDependenciesTextWithPack200()
        throws Exception
    {

        DependencyFilenameStrategy dependencyFilenameStrategy = new SimpleDependencyFilenameStrategy();

        String codebase = null;
        JnlpConfig jnlp = null;

        GeneratorConfig generatorConfigEmpty =
            new GeneratorConfig( null, true, false, false, artifact1, dependencyFilenameStrategy,
                                 Collections.<Artifact>emptyList(), null, codebase, jnlp );

        assertEquals( "", Generator.getDependenciesText( generatorConfigEmpty ) );

        GeneratorConfig generatorConfig =
            new GeneratorConfig( null, true, false, false, artifact1, dependencyFilenameStrategy, artifacts, null, codebase,
                                 jnlp );

        assertEquals( EOL + "<property name=\"jnlp.packEnabled\" value=\"true\" />" +
        				EOL + "<jar href=\"artifact1-classifier-1.0.jar\" main=\"true\"/>" +
        				EOL + "<jar href=\"artifact2-1.5.jar\"/>" + 
        				EOL +"<jar href=\"artifact3-1.5-SNAPSHOT.jar\"/>" + EOL, Generator.getDependenciesText( generatorConfig ) );

        GeneratorConfig generatorConfig2 =
            new GeneratorConfig( null, true, true, false, artifact1, dependencyFilenameStrategy, artifacts, null, codebase,
                                 jnlp );

        assertEquals( EOL + "<property name=\"jnlp.packEnabled\" value=\"true\" />" +
        				EOL + "<property name=\"jnlp.versionEnabled\" value=\"true\" />" +
        				EOL + "<jar href=\"artifact1-classifier.jar\" version=\"1.0\" main=\"true\"/>" +
        				EOL + "<jar href=\"artifact2.jar\" version=\"1.5\"/>" + 
        				EOL +"<jar href=\"artifact3.jar\" version=\"1.5-SNAPSHOT\"/>" + EOL,
                      Generator.getDependenciesText( generatorConfig2 ) );
    }

    public void testGetDependenciesTextWithLibPath()
        throws Exception
    {

        DependencyFilenameStrategy dependencyFilenameStrategy = new SimpleDependencyFilenameStrategy();

        String codebase = null;
        JnlpConfig jnlp = null;

        GeneratorConfig generatorConfigEmpty =
            new GeneratorConfig( "lib", false, false, false, artifact1, dependencyFilenameStrategy,
                                 Collections.<Artifact>emptyList(), null, codebase, jnlp );

        assertEquals( "", Generator.getDependenciesText( generatorConfigEmpty ) );

        GeneratorConfig generatorConfig =
            new GeneratorConfig( "lib", false, false, false, artifact1, dependencyFilenameStrategy, artifacts, null, codebase,
                                 jnlp );

        assertEquals( EOL + "<jar href=\"lib/artifact1-classifier-1.0.jar\" main=\"true\"/>" +
        				EOL + "<jar href=\"lib/artifact2-1.5.jar\"/>" + 
        				EOL +"<jar href=\"lib/artifact3-1.5-SNAPSHOT.jar\"/>" + EOL,
                      Generator.getDependenciesText( generatorConfig ) );

        GeneratorConfig generatorConfig2 =
            new GeneratorConfig( "lib", false, true, false, artifact1, dependencyFilenameStrategy, artifacts, null, codebase,
                                 jnlp );

        assertEquals( EOL + "<property name=\"jnlp.versionEnabled\" value=\"true\" />" +
        				EOL + "<jar href=\"lib/artifact1-classifier.jar\" version=\"1.0\" main=\"true\"/>" +
        				EOL + "<jar href=\"lib/artifact2.jar\" version=\"1.5\"/>" + 
        				EOL +"<jar href=\"lib/artifact3.jar\" version=\"1.5-SNAPSHOT\"/>" + EOL,
                      Generator.getDependenciesText( generatorConfig2 ) );
    }
    
    public void testGetDependenciesTextWithUniqueVersions()
            throws Exception
    {

        DependencyFilenameStrategy dependencyFilenameStrategy = new SimpleDependencyFilenameStrategy();

        String codebase = null;
        JnlpConfig jnlp = null;

        GeneratorConfig generatorConfigEmpty =
            new GeneratorConfig( null, false, false, true, artifact1, dependencyFilenameStrategy,
                                 Collections.<Artifact>emptyList(), null, codebase, jnlp );

        assertEquals( "", Generator.getDependenciesText( generatorConfigEmpty ) );

        GeneratorConfig generatorConfig =
            new GeneratorConfig( null, false, false, true, artifact1, dependencyFilenameStrategy, artifacts, null, codebase,
                                 jnlp );

        assertEquals(
        		EOL + "<jar href=\"artifact1-classifier-1.0.jar\" main=\"true\"/>" + EOL + "<jar href=\"artifact2-1.5.jar\"/>" + 
        				EOL + "<jar href=\"artifact3-1.5-15012014121212.jar\"/>" + EOL,
            Generator.getDependenciesText( generatorConfig ) );

        GeneratorConfig generatorConfig2 =
            new GeneratorConfig( null, false, true, true, artifact1, dependencyFilenameStrategy, artifacts, null, codebase,
                                 jnlp );

        assertEquals( EOL + "<property name=\"jnlp.versionEnabled\" value=\"true\" />" +
        				EOL + "<jar href=\"artifact1-classifier.jar\" version=\"1.0\" main=\"true\"/>" +
        				EOL + "<jar href=\"artifact2.jar\" version=\"1.5\"/>"  +
        				EOL + "<jar href=\"artifact3.jar\" version=\"1.5-15012014121212\"/>" + EOL,
                      Generator.getDependenciesText( generatorConfig2 ) );
        
        GeneratorConfig generatorConfig3 =
                new GeneratorConfig( null, false, false, true, artifact1, dependencyFilenameStrategy, artifacts, null, codebase,
                                     jnlp );

            assertEquals( EOL + "<jar href=\"artifact1-classifier-1.0.jar\" main=\"true\"/>" +
            				EOL + "<jar href=\"artifact2-1.5.jar\"/>"  +
            				EOL + "<jar href=\"artifact3-1.5-15012014121212.jar\"/>" + EOL,
                          Generator.getDependenciesText( generatorConfig3 ) );
    }    
}
