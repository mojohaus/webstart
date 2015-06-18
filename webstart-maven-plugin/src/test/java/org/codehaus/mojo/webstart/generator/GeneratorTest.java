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

import junit.framework.TestCase;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.artifact.versioning.VersionRange;
import org.codehaus.mojo.webstart.JnlpConfig;
import org.codehaus.mojo.webstart.dependency.filenaming.DependencyFilenameStrategy;
import org.codehaus.mojo.webstart.dependency.filenaming.FullDependencyFilenameStrategy;
import org.codehaus.mojo.webstart.dependency.filenaming.SimpleDependencyFilenameStrategy;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author <a href="jerome@coffeebreaks.org">Jerome Lacoste</a>
 * @version $Id$
 */
public class GeneratorTest
    extends TestCase
{
    protected Artifact artifact1;

    protected Artifact artifact2;

    private List<Artifact> artifacts;

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
        artifacts = new ArrayList<Artifact>();
        artifacts.add( artifact1 );
        artifacts.add( artifact2 );
    }

    public void testGetDependenciesText()
        throws Exception
    {

        DependencyFilenameStrategy dependencyFilenameStrategy = new SimpleDependencyFilenameStrategy();

        String codebase = null;
        JnlpConfig jnlp = null;

        GeneratorConfig generatorConfigEmpty =
            new GeneratorConfig( null, false, false, artifact1, dependencyFilenameStrategy,
                                 Collections.<Artifact>emptyList(), null, codebase, jnlp );

        assertEquals( "", Generator.getDependenciesText( generatorConfigEmpty ) );

        GeneratorConfig generatorConfig =
            new GeneratorConfig( null, false, false, artifact1, dependencyFilenameStrategy, artifacts, null, codebase,
                                 jnlp );

        assertEquals(
            "\n<jar href=\"artifact1-classifier-1.0.jar\" main=\"true\"/>" + "\n<jar href=\"artifact2-1.5.jar\"/>\n",
            Generator.getDependenciesText( generatorConfig ) );

        GeneratorConfig generatorConfig2 =
            new GeneratorConfig( null, false, true, artifact1, dependencyFilenameStrategy, artifacts, null, codebase,
                                 jnlp );

        assertEquals( "\n<property name=\"jnlp.versionEnabled\" value=\"true\" />" +
                          "\n<jar href=\"artifact1-classifier.jar\" version=\"1.0\" main=\"true\"/>" +
                          "\n<jar href=\"artifact2.jar\" version=\"1.5\"/>\n",
                      Generator.getDependenciesText( generatorConfig2 ) );
    }

    public void testGetDependenciesTextWithFullNaming()
        throws Exception
    {

        DependencyFilenameStrategy dependencyFilenameStrategy = new FullDependencyFilenameStrategy();

        String codebase = null;
        JnlpConfig jnlp = null;

        GeneratorConfig generatorConfigEmpty =
            new GeneratorConfig( null, false, false, artifact1, dependencyFilenameStrategy,
                                 Collections.<Artifact>emptyList(), null, codebase, jnlp );

        assertEquals( "", Generator.getDependenciesText( generatorConfigEmpty ) );

        GeneratorConfig generatorConfig =
            new GeneratorConfig( null, false, false, artifact1, dependencyFilenameStrategy, artifacts, null, codebase,
                                 jnlp );

        assertEquals( "\n<jar href=\"groupId-artifact1-classifier-1.0.jar\" main=\"true\"/>" +
                          "\n<jar href=\"groupId-artifact2-1.5.jar\"/>\n",
                      Generator.getDependenciesText( generatorConfig ) );

        GeneratorConfig generatorConfig2 =
            new GeneratorConfig( null, false, true, artifact1, dependencyFilenameStrategy, artifacts, null, codebase,
                                 jnlp );

        assertEquals( "\n<property name=\"jnlp.versionEnabled\" value=\"true\" />" +
                          "\n<jar href=\"groupId-artifact1-classifier.jar\" version=\"1.0\" main=\"true\"/>" +
                          "\n<jar href=\"groupId-artifact2.jar\" version=\"1.5\"/>\n",
                      Generator.getDependenciesText( generatorConfig2 ) );
    }

    public void testGetDependenciesTextWithPack200()
        throws Exception
    {

        DependencyFilenameStrategy dependencyFilenameStrategy = new SimpleDependencyFilenameStrategy();

        String codebase = null;
        JnlpConfig jnlp = null;

        GeneratorConfig generatorConfigEmpty =
            new GeneratorConfig( null, true, false, artifact1, dependencyFilenameStrategy,
                                 Collections.<Artifact>emptyList(), null, codebase, jnlp );

        assertEquals( "", Generator.getDependenciesText( generatorConfigEmpty ) );

        GeneratorConfig generatorConfig =
            new GeneratorConfig( null, true, false, artifact1, dependencyFilenameStrategy, artifacts, null, codebase,
                                 jnlp );

        assertEquals( "\n<property name=\"jnlp.packEnabled\" value=\"true\" />" +
                          "\n<jar href=\"artifact1-classifier-1.0.jar\" main=\"true\"/>" +
                          "\n<jar href=\"artifact2-1.5.jar\"/>\n", Generator.getDependenciesText( generatorConfig ) );

        GeneratorConfig generatorConfig2 =
            new GeneratorConfig( null, true, true, artifact1, dependencyFilenameStrategy, artifacts, null, codebase,
                                 jnlp );

        assertEquals( "\n<property name=\"jnlp.packEnabled\" value=\"true\" />" +
                          "\n<property name=\"jnlp.versionEnabled\" value=\"true\" />" +
                          "\n<jar href=\"artifact1-classifier.jar\" version=\"1.0\" main=\"true\"/>" +
                          "\n<jar href=\"artifact2.jar\" version=\"1.5\"/>\n",
                      Generator.getDependenciesText( generatorConfig2 ) );
    }

    public void testGetDependenciesTextWithLibPath()
        throws Exception
    {

        DependencyFilenameStrategy dependencyFilenameStrategy = new SimpleDependencyFilenameStrategy();

        String codebase = null;
        JnlpConfig jnlp = null;

        GeneratorConfig generatorConfigEmpty =
            new GeneratorConfig( "lib", false, false, artifact1, dependencyFilenameStrategy,
                                 Collections.<Artifact>emptyList(), null, codebase, jnlp );

        assertEquals( "", Generator.getDependenciesText( generatorConfigEmpty ) );

        GeneratorConfig generatorConfig =
            new GeneratorConfig( "lib", false, false, artifact1, dependencyFilenameStrategy, artifacts, null, codebase,
                                 jnlp );

        assertEquals( "\n<jar href=\"lib/artifact1-classifier-1.0.jar\" main=\"true\"/>" +
                          "\n<jar href=\"lib/artifact2-1.5.jar\"/>\n",
                      Generator.getDependenciesText( generatorConfig ) );

        GeneratorConfig generatorConfig2 =
            new GeneratorConfig( "lib", false, true, artifact1, dependencyFilenameStrategy, artifacts, null, codebase,
                                 jnlp );

        assertEquals( "\n<property name=\"jnlp.versionEnabled\" value=\"true\" />" +
                          "\n<jar href=\"lib/artifact1-classifier.jar\" version=\"1.0\" main=\"true\"/>" +
                          "\n<jar href=\"lib/artifact2.jar\" version=\"1.5\"/>\n",
                      Generator.getDependenciesText( generatorConfig2 ) );
    }
}
