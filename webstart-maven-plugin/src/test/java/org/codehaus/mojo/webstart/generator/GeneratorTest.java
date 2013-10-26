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
import org.apache.maven.artifact.versioning.VersionRange;
import org.codehaus.mojo.webstart.JnlpMojo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="jerome@coffeebreaks.org">Jerome Lacoste</a>
 * @version $Id$
 */
public class GeneratorTest
    extends TestCase
{

    public void testGetDependenciesText()
        throws Exception
    {
        final Artifact artifact1 =
            new DefaultArtifact( "groupId", "artifactId1", VersionRange.createFromVersion( "1.0" ), "scope", "jar",
                                 "classifier", null );
        artifact1.setFile( new File( "artifact1-1.0.jar" ) );
        final Artifact artifact2 =
            new DefaultArtifact( "groupId", "artifactId2", VersionRange.createFromVersion( "1.5" ), null, "jar", "",
                                 null );
        artifact2.setFile( new File( "artifact2-1.5.jar" ) );

        final List<Artifact> artifacts = new ArrayList<Artifact>();

        JnlpMojo mojo = new JnlpMojo()
        {
            /**
             * {@inheritDoc}
             */
            public List<Artifact> getPackagedJnlpArtifacts()
            {
                return artifacts;
            }

            /**
             * {@inheritDoc}
             */
            public boolean isArtifactWithMainClass( Artifact artifact )
            {
                return artifact == artifact1;
            }
        };

        assertEquals( "", Generator.getDependenciesText( mojo ) );

        artifacts.add( artifact1 );
        artifacts.add( artifact2 );

        assertEquals( "\n<jar href=\"artifact1-1.0.jar\" main=\"true\"/>" + "\n<jar href=\"artifact2-1.5.jar\"/>\n",
                      Generator.getDependenciesText( mojo ) );

        mojo.setOutputJarVersions( true );

        assertEquals( "\n<jar href=\"artifact1-1.0.jar\" version=\"1.0\" main=\"true\"/>" +
                          "\n<jar href=\"artifact2-1.5.jar\" version=\"1.5\"/>\n",
                      Generator.getDependenciesText( mojo ) );


    }

    public void testGetDependenciesTextWithPack200()
        throws Exception
    {
        final Artifact artifact1 =
            new DefaultArtifact( "groupId", "artifactId1", VersionRange.createFromVersion( "1.0" ), "scope", "jar",
                                 "classifier", null );
        artifact1.setFile( new File( "artifact1-1.0.jar" ) );
        final Artifact artifact2 =
            new DefaultArtifact( "groupId", "artifactId2", VersionRange.createFromVersion( "1.5" ), null, "jar", "",
                                 null );
        artifact2.setFile( new File( "artifact2-1.5.jar" ) );

        final List<Artifact> artifacts = new ArrayList<Artifact>();

        JnlpMojo mojo = new JnlpMojo()
        {
            /**
             * {@inheritDoc}
             */
            public List<Artifact> getPackagedJnlpArtifacts()
            {
                return artifacts;
            }

            /**
             * {@inheritDoc}
             */
            public boolean isArtifactWithMainClass( Artifact artifact )
            {
                return artifact == artifact1;
            }

            /**
             * {@inheritDoc}
             */
            public boolean isPack200()
            {
                return true;
            }
        };

        assertEquals( "", Generator.getDependenciesText( mojo ) );

        artifacts.add( artifact1 );
        artifacts.add( artifact2 );

        assertEquals( "\n<property name=\"jnlp.packEnabled\" value=\"true\" />" +
                          "\n<jar href=\"artifact1-1.0.jar\" main=\"true\"/>" +
                          "\n<jar href=\"artifact2-1.5.jar\"/>\n", Generator.getDependenciesText( mojo ) );

        mojo.setOutputJarVersions( true );

        assertEquals( "\n<property name=\"jnlp.packEnabled\" value=\"true\" />" +
                          "\n<jar href=\"artifact1-1.0.jar\" version=\"1.0\" main=\"true\"/>" +
                          "\n<jar href=\"artifact2-1.5.jar\" version=\"1.5\"/>\n",
                      Generator.getDependenciesText( mojo ) );


    }

    public void testGetDependenciesTextWithLibPath()
        throws Exception
    {
        final Artifact artifact1 =
            new DefaultArtifact( "groupId", "artifactId1", VersionRange.createFromVersion( "1.0" ), "scope", "jar",
                                 "classifier", null );
        artifact1.setFile( new File( "artifact1-1.0.jar" ) );
        final Artifact artifact2 =
            new DefaultArtifact( "groupId", "artifactId2", VersionRange.createFromVersion( "1.5" ), null, "jar", "",
                                 null );
        artifact2.setFile( new File( "artifact2-1.5.jar" ) );

        final List<Artifact> artifacts = new ArrayList<Artifact>();

        JnlpMojo mojo = new JnlpMojo()
        {
            /**
             * {@inheritDoc}
             */
            public List<Artifact> getPackagedJnlpArtifacts()
            {
                return artifacts;
            }

            /**
             * {@inheritDoc}
             */
            public boolean isArtifactWithMainClass( Artifact artifact )
            {
                return artifact == artifact1;
            }

            /**
             * {@inheritDoc}
             */
            public String getLibPath()
            {
                return "lib";
            }
        };

        assertEquals( "", Generator.getDependenciesText( mojo ) );

        artifacts.add( artifact1 );
        artifacts.add( artifact2 );

        assertEquals(
            "\n<jar href=\"lib/artifact1-1.0.jar\" main=\"true\"/>" + "\n<jar href=\"lib/artifact2-1.5.jar\"/>\n",
            Generator.getDependenciesText( mojo ) );

        mojo.setOutputJarVersions( true );

        assertEquals( "\n<jar href=\"lib/artifact1-1.0.jar\" version=\"1.0\" main=\"true\"/>" +
                          "\n<jar href=\"lib/artifact2-1.5.jar\" version=\"1.5\"/>\n",
                      Generator.getDependenciesText( mojo ) );


    }
}
