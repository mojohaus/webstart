package org.codehaus.mojo.webstart;

import org.apache.maven.plugin.MojoExecutionException;

import java.io.File;

/**
 * Tool api for jarsigner operations.
 *
 * @author tchemit <chemit@codelutin.com>
 * @since 1.0-beta-3
 */
public interface SignTool
{

    /**
     * Plexus component role.
     */
    String ROLE = SignTool.class.getName();

    /**
     * Sign a jar using jarsigner.
     *
     * @param config    sign configuration
     * @param jarFile location of the jar to sign
     * @param signedJar optional location of the signed jar to produce (if not set, will use the original location)
     * @throws MojoExecutionException if something wrong occurs
     */
    void sign( SignConfig config, File jarFile, File signedJar )
        throws MojoExecutionException;

    /**
     * Verify a jar file using jarsigner.
     *
     * @param config  sign configuration
     * @param jarFile location of the jar to sign
     * @param certs   flag to show certificats details
     * @throws MojoExecutionException if something wrong occurs
     */
    void verify( SignConfig config, File jarFile, boolean certs )
    throws MojoExecutionException;

    /**
     * Tests if the given jar is signed.
     *
     * @param config  sign configuration
     * @param jarFile the jar file to test
     * @return {@code true} if jar file is signed, {@code false} otherwise
     * @throws MojoExecutionException if something wrong occurs
     */
    boolean isJarSigned( SignConfig config, File jarFile )
    throws MojoExecutionException;

    /**
     * Unsign a jar.
     *
     * @param jarFile location of the jar to unsign
     * @param tempDirectory temp directory where to unzip the jar
     * @param verbose flag to display verbose logs
     * @throws MojoExecutionException if something wrong occurs
     */
    void unsign( File jarFile , File tempDirectory, boolean verbose)
        throws MojoExecutionException;
}
