package org.codehaus.mojo.webstart;

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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.ArtifactRepositoryFactory;
import org.apache.maven.artifact.repository.ArtifactRepositoryPolicy;
import org.apache.maven.artifact.repository.layout.DefaultRepositoryLayout;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.apache.maven.profiles.DefaultProfileManager;
import org.apache.maven.profiles.ProfileManager;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectBuilder;

/**
 * @author <a href="jerome@coffeebreaks.org">Jerome Lacoste</a>
 * @version $Id$
 */
public class AbstractJnlpMojoTest
    extends AbstractMojoTestCase
{
    public static void main( String[] args )
    {
        junit.textui.TestRunner.run( suite() );
    }

    public static Test suite()
    {
        TestSuite suite = new TestSuite( AbstractJnlpMojoTest.class );

        return suite;
    }
  
    /*
    public void setUp()
    {
    }
  
    public void tearDown()
    {
    }
    */

    public void testFailWhenSomeDependenciesDoNotExist() throws Exception
    {
        JnlpInlineMojo mojo = new JnlpInlineMojo();

        File pom = new File( getBasedir(), "src/test/projects/project4/pom.xml" );

        setUpProject( pom, mojo );

        // -- TODO why can't this be read/set from the pom.xml file?
        AbstractJnlpMojo.Dependencies deps = new AbstractJnlpMojo.Dependencies();
        List includes = new ArrayList();
        includes.add( "tatatata" );
        includes.add( "titititi" );
        List excludes = new ArrayList();
        excludes.add( "commons-lang:commons-lang" );
        excludes.add( "totototo" );
        deps.setIncludes( includes );
        deps.setExcludes( excludes );
        setVariableValueToObject( mojo, "dependencies", deps );
        // -- 

        assertTrue( "dependencies not null", mojo.getDependencies() != null );
        assertEquals( "2 includes", 2, mojo.getDependencies().getIncludes().size() );
        assertEquals( "2 excludes", 2, mojo.getDependencies().getExcludes().size() );
        
        try
        {
            mojo.checkDependencies();
            fail( "Should have detected invalid webstart <dependencies>" );
        }
        catch ( MojoExecutionException e )
        {
        }
    }

    public void testAllDependenciesExist() throws Exception
    {
        JnlpInlineMojo mojo = new JnlpInlineMojo();

        File pom = new File( getBasedir(), "src/test/projects/project3/pom.xml" );

        setUpProject( pom, mojo );

        // -- TODO why can't this be read/set from the pom.xml file?
        AbstractJnlpMojo.Dependencies deps = new AbstractJnlpMojo.Dependencies();
        List excludes = new ArrayList();
        excludes.add( "commons-lang:commons-lang" );
        deps.setExcludes( excludes );
        setVariableValueToObject( mojo, "dependencies", deps );
        // -- 

        assertTrue( "dependencies not null", mojo.getDependencies() != null );
        assertNull( "no include", mojo.getDependencies().getIncludes() );
        assertEquals( "1 exclude", 1, mojo.getDependencies().getExcludes().size() );
        
        mojo.checkDependencies();
    }


    private void setUpProject( File pomFile, AbstractMojo mojo )
        throws Exception
    {
        MavenProjectBuilder projectBuilder = (MavenProjectBuilder) lookup( MavenProjectBuilder.ROLE );

        ArtifactRepositoryFactory artifactRepositoryFactory =
            (ArtifactRepositoryFactory) lookup( ArtifactRepositoryFactory.ROLE );

        ArtifactRepositoryPolicy policy = new ArtifactRepositoryPolicy( true, "never", "never" );

        String localRepoUrl = "file://" + System.getProperty( "user.home" ) + "/.m2/repository";

        ArtifactRepository localRepository = artifactRepositoryFactory.createArtifactRepository( "local", localRepoUrl, new DefaultRepositoryLayout(), policy, policy );

        ProfileManager profileManager = new DefaultProfileManager( getContainer() );

        MavenProject project = projectBuilder.buildWithDependencies( pomFile,
                                                                     localRepository, profileManager );

        //this gets the classes for these tests of this mojo (exec plugin) onto the project classpath for the test
        project.getBuild().setOutputDirectory( new File("target/test-classes").getAbsolutePath() );
        setVariableValueToObject( mojo, "project", project );
    }
}
