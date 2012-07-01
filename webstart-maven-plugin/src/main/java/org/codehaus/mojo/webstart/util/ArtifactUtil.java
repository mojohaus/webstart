package org.codehaus.mojo.webstart.util;

import org.apache.maven.artifact.Artifact;
import org.codehaus.mojo.webstart.JarResource;
import org.codehaus.mojo.webstart.JnlpConfig;

import java.net.MalformedURLException;

/**
 * Some usefull methods on artifacts.
 *
 * @author tchemit <chemit@codelutin.com>
 * @since 1.0-beta-4
 */

public interface ArtifactUtil
{

    boolean artifactContainsMainClass( Artifact artifact, JnlpConfig jnlp )
        throws MalformedURLException;

    boolean artifactContainsMainClass( Artifact artifact, JarResource jnlp )
        throws MalformedURLException;
}
