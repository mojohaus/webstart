package org.codehaus.mojo.webstart;

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

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.shared.jarsigner.JarSignerUtil;
import org.codehaus.mojo.webstart.sign.SignTool;

import java.io.File;

/**
 * Unsigns a JAR, removing signatures.
 * <p/>
 * This code will hopefully be moved into the jar plugin when stable enough.
 *
 * @author <a href="mailto:jerome@coffeebreaks.org">Jerome Lacoste</a>
 * @author <a href="mailto:andrius@pivotcapital.com">Andrius Šabanas</a>
 * @version $Id$
 */
@Mojo( name = "unsign", defaultPhase = LifecyclePhase.PACKAGE, requiresProject = false )
public class JarUnsignMojo
    extends AbstractMojo
{
    // ----------------------------------------------------------------------
    // Mojo Parameters
    // ----------------------------------------------------------------------

    /**
     * Set this to <code>true</code> to disable signing.
     * Useful to speed up build process in development environment.
     */
    @Parameter( property = "maven.jar.unsign.skip", defaultValue = "false" )
    private boolean skip;

    /**
     * The directory location used for temporary storage of files used by this mojo.
     *
     * @deprecated since 1.0-beta-4, no more used to unsign jars.
     */
    @Parameter( property = "tempdir", defaultValue = "${basedir}", required = true )
    private File tempDirectory;

    /**
     * Path of the jar to unsign. Will unsign all archives in case folder was specified here.
     * When specified, the finalName is ignored.
     */
    @Parameter( alias = "jarpath", property = "maven.jar.unsign.jarpath",
                defaultValue = "${project.build.directory}/${project.build.finalName}.${project.packaging}" )
    private File jarPath;

    /**
     * Enable verbose mode.
     */
    @Parameter( property = "verbose", defaultValue = "false" )
    private boolean verbose;

    // ----------------------------------------------------------------------
    // Components
    // ----------------------------------------------------------------------

    /**
     * Sign tool.
     */
    @Component
    private SignTool signTool;

    // ----------------------------------------------------------------------
    // Mojo Implementation
    // ----------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    public void execute()
        throws MojoExecutionException
    {
        if ( skip )
        {
            getLog().info( "Skipping JAR unsigning for file: " + jarPath.getAbsolutePath() );
            return;
        }
        if ( jarPath.isDirectory() )
        {
            for ( File jar : jarPath.listFiles() ) {
                if ( JarSignerUtil.isZipFile( jar ) ) {
                    signTool.unsign( jar, verbose );
                }
                else
                {
                    getLog().info( "Skipping JAR unsigning for file: " + jar.getAbsolutePath() );
                }
            }
        }
        else
        {
            signTool.unsign( this.jarPath, verbose );
        }
    }

    // ----------------------------------------------------------------------
    // Public Methods
    // ----------------------------------------------------------------------

    public void setJarPath( File jarPath )
    {
        this.jarPath = jarPath;
    }

    public void setVerbose( boolean verbose )
    {
        this.verbose = verbose;
    }

    public void setSignTool( SignTool signTool )
    {
        this.signTool = signTool;
    }

}
