package org.codehaus.mojo.webstart.util;

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

import org.apache.commons.collections.CollectionUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.mojo.webstart.JarResource;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.AbstractLogEnabled;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Default implementation of {@link ArtifactUtil}.
 *
 * @author tchemit <chemit@codelutin.com>
 * @since 1.0-beta-4
 */
@Component( role = ArtifactUtil.class, hint = "default" )
public class DefaultArtifactUtil
    extends AbstractLogEnabled
    implements ArtifactUtil
{

    /**
     */
    @Requirement
    private ArtifactFactory artifactFactory;

    /**
     * Artifact resolver, needed to download source jars for inclusion in classpath.
     */
    @Requirement
    private ArtifactResolver artifactResolver;

    /**
     * The project's artifact metadata source, used to resolve transitive dependencies.
     */
    @Requirement
    private ArtifactMetadataSource artifactMetadataSource;

    /**
     * {@inheritDoc}
     */
    public Artifact createArtifact( JarResource jarResource )
    {

        if ( jarResource.getClassifier() == null )
        {
            return artifactFactory.createArtifact( jarResource.getGroupId(), jarResource.getArtifactId(),
                                                   jarResource.getVersion(), Artifact.SCOPE_RUNTIME, "jar" );
        }
        else
        {
            return artifactFactory.createArtifactWithClassifier( jarResource.getGroupId(), jarResource.getArtifactId(),
                                                                 jarResource.getVersion(), "jar",
                                                                 jarResource.getClassifier() );
        }
    }

    /**
     * {@inheritDoc}
     */
    public MavenProject resolveFromReactor( Artifact artifact, MavenProject mp, List<MavenProject> reactorProjects )
        throws MojoExecutionException
    {
        MavenProject result = null;

        String artifactId = artifact.getArtifactId();
        String groupId = artifact.getGroupId();

        if ( CollectionUtils.isNotEmpty( reactorProjects ) )
        {
            for ( MavenProject reactorProject : reactorProjects )
            {
                if ( reactorProject.getArtifactId().equals( artifactId ) &&
                    reactorProject.getGroupId().equals( groupId ) )
                {
                    result = reactorProject;
                    break;
                }
            }
        }

        return result;
    }

    /**
     * {@inheritDoc}
     */
    public void resolveFromRepositories( Artifact artifact, List remoteRepositories,
                                         ArtifactRepository localRepository )
        throws MojoExecutionException
    {
        try
        {
            artifactResolver.resolve( artifact, remoteRepositories, localRepository );
        }
        catch ( ArtifactResolutionException e )
        {
            throw new MojoExecutionException( "Could not resolv artifact: " + artifact, e );
        }
        catch ( ArtifactNotFoundException e )
        {
            throw new MojoExecutionException( "Could not find artifact: " + artifact, e );
        }
    }

    /**
     * {@inheritDoc}
     */
    public Set<Artifact> resolveTransitively( Set<Artifact> jarResourceArtifacts, Set<MavenProject> siblingProjects,
                                              Artifact originateArtifact, ArtifactRepository localRepository,
                                              List<ArtifactRepository> remoteRepositories,
                                              ArtifactFilter artifactFilter )
        throws MojoExecutionException
    {

        Set<Artifact> resultArtifacts = new LinkedHashSet<Artifact>();

        if ( CollectionUtils.isNotEmpty( siblingProjects ) )
        {

            // getting transitive dependencies from project
            for ( MavenProject siblingProject : siblingProjects )
            {
                Set<Artifact> artifacts = siblingProject.getArtifacts();
                for ( Artifact artifact : artifacts )
                {
                    if ( artifactFilter.include( artifact ) )
                    {

                        resultArtifacts.add( artifact );
                    }
                }
            }
        }
        try
        {
            ArtifactResolutionResult result =
                artifactResolver.resolveTransitively( jarResourceArtifacts, originateArtifact, null,
                                                      //managedVersions
                                                      localRepository, remoteRepositories, this.artifactMetadataSource,
                                                      artifactFilter );

            resultArtifacts.addAll( result.getArtifacts() );

            return resultArtifacts;
        }
        catch ( ArtifactResolutionException e )
        {
            throw new MojoExecutionException( "Could not resolv transitive dependencies", e );
        }
        catch ( ArtifactNotFoundException e )
        {
            throw new MojoExecutionException( "Could not find transitive dependencies ", e );
        }
    }

    /**
     * Tests if the given fully qualified name exists in the given artifact.
     *
     * @param artifact  artifact to test
     * @param mainClass the fully qualified name to find in artifact
     * @return {@code true} if given artifact contains the given fqn, {@code false} otherwise
     * @throws MojoExecutionException if artifact file url is mal formed
     */

    public boolean artifactContainsClass( Artifact artifact, final String mainClass )
        throws MojoExecutionException
    {
        boolean containsClass = true;

        // JarArchiver.grabFilesAndDirs()
        URL url;
        try
        {
            url = artifact.getFile().toURI().toURL();
        }
        catch ( MalformedURLException e )
        {
            throw new MojoExecutionException( "Could not get artifact url: " + artifact.getFile(), e );
        }
        ClassLoader cl = new java.net.URLClassLoader( new URL[]{ url } );
        Class<?> c = null;
        try
        {
            c = Class.forName( mainClass, false, cl );
        }
        catch ( ClassNotFoundException e )
        {
            getLogger().debug( "artifact " + artifact + " doesn't contain the main class: " + mainClass );
            containsClass = false;
        }
        catch ( Throwable t )
        {
            getLogger().info( "artifact " + artifact + " seems to contain the main class: " + mainClass +
                                  " but the jar doesn't seem to contain all dependencies " + t.getMessage() );
        }

        if ( c != null )
        {
            getLogger().debug( "Checking if the loaded class contains a main method." );

            try
            {
                c.getMethod( "main", String[].class );
            }
            catch ( NoSuchMethodException e )
            {
                getLogger().warn(
                    "The specified main class (" + mainClass + ") doesn't seem to contain a main method... " +
                        "Please check your configuration." + e.getMessage() );
            }
            catch ( NoClassDefFoundError e )
            {
                // undocumented in SDK 5.0. is this due to the ClassLoader lazy loading the Method
                // thus making this a case tackled by the JVM Spec (Ref 5.3.5)!
                // Reported as Incident 633981 to Sun just in case ...
                getLogger().warn( "Something failed while checking if the main class contains the main() method. " +
                                      "This is probably due to the limited classpath we have provided to the class loader. " +
                                      "The specified main class (" + mainClass +
                                      ") found in the jar is *assumed* to contain a main method... " + e.getMessage() );
            }
            catch ( Throwable t )
            {
                getLogger().error( "Unknown error: Couldn't check if the main class has a main method. " +
                                       "The specified main class (" + mainClass +
                                       ") found in the jar is *assumed* to contain a main method...", t );
            }
        }

        return containsClass;
    }
}
