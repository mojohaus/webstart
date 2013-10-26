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

import org.apache.maven.artifact.Artifact;
import org.codehaus.mojo.webstart.JarResource;
import org.codehaus.mojo.webstart.JnlpConfig;
import org.codehaus.plexus.logging.AbstractLogEnabled;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Default implementation of {@link ArtifactUtil}.
 *
 * @author tchemit <chemit@codelutin.com>
 * @plexus.component role-hint="default"
 * @since 1.0-beta-4
 */

public class DefaultArtifactUtil
    extends AbstractLogEnabled
    implements ArtifactUtil
{

    /**
     * {@inheritDoc}
     */
    public boolean artifactContainsMainClass( Artifact artifact, JnlpConfig jnlp )
        throws MalformedURLException
    {
        boolean result = false;
        if ( jnlp != null )
        {
            result = artifactContainsClass( artifact, jnlp.getMainClass() );
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    public boolean artifactContainsMainClass( Artifact artifact, JarResource jnlp )
        throws MalformedURLException
    {
        boolean result = false;
        if ( jnlp != null )
        {
            result = artifactContainsClass( artifact, jnlp.getMainClass() );
        }
        return result;
    }

    /**
     * Tests if the given fully qualified name exists in the given artifact.
     *
     * @param artifact  artifact to test
     * @param mainClass the fully qualified name to find in artifact
     * @return {@code true} if given artifact contains the given fqn, {@code false} otherwise
     * @throws java.net.MalformedURLException if artifact file url is mal formed
     */
    protected boolean artifactContainsClass( Artifact artifact, final String mainClass )
        throws MalformedURLException
    {
        boolean containsClass = true;

        // JarArchiver.grabFilesAndDirs()
        ClassLoader cl = new java.net.URLClassLoader( new URL[]{ artifact.getFile().toURI().toURL() } );
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
                c.getMethod( "main", new Class[]{ String[].class } );
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
