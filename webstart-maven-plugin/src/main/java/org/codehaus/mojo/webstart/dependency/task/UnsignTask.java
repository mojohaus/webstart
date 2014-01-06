package org.codehaus.mojo.webstart.dependency.task;

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

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.shared.jarsigner.JarSignerUtil;
import org.codehaus.mojo.webstart.dependency.JnlpDependencyConfig;
import org.codehaus.mojo.webstart.sign.SignTool;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;

import java.io.File;
import java.io.IOException;

/**
 * To unsign a already sign
 * Created on 1/4/14.
 *
 * @author Tony Chemit <chemit@codelutin.com>
 * @since 1.0-beta-5
 */
@Component( role = JnlpDependencyTask.class, hint = UnsignTask.ROLE_HINT, instantiationStrategy = "per-lookup" )
public class UnsignTask
    extends AbstractJnlpTask
{
    public static final String ROLE_HINT = "UnsignTask";

    /**
     * Sign tool.
     */
    @Requirement
    private SignTool signTool;

    /**
     * {@inheritDoc}
     */
    public void check( JnlpDependencyConfig config )
    {
        if ( config == null )
        {
            throw new NullPointerException( "config can't be null" );
        }
        if ( config.getArtifact() == null )
        {
            throw new NullPointerException( "config.artifact can't be null" );
        }
        if ( config.getArtifact().getFile() == null )
        {
            throw new NullPointerException( "config.artifact.file can't be null" );
        }
        if ( !config.isSign() )
        {
            throw new IllegalStateException( "Can't sign if config.isSign is false" );
        }

        File file = config.getArtifact().getFile();

        boolean jarSigned;
        try
        {
            jarSigned = signTool.isJarSigned( file );
        }
        catch ( MojoExecutionException e )
        {
            throw new RuntimeException( e.getMessage(), e.getCause() );
        }

        if ( jarSigned && !config.isCanUnsign() )
        {
            throw new IllegalStateException( "Can't unsign the config.artifact.file if config.isCanUsign is false" );
        }
    }

    /**
     * {@inheritDoc}
     */
    public File execute( JnlpDependencyConfig config, File jarFile )
        throws JnlpDependencyTaskException
    {

        boolean jarSigned;
        try
        {
            jarSigned = JarSignerUtil.isArchiveSigned( jarFile );
        }
        catch ( IOException e )
        {
            throw new JnlpDependencyTaskException( "Could not detect if jar signed: " + jarFile, e );
        }

        if ( jarSigned )
        {

            // unsign jar

            verboseLog( config, "Unsign jar " + jarFile );
            try
            {
                JarSignerUtil.unsignArchive( jarFile );
            }
            catch ( IOException e )
            {

                throw new JnlpDependencyTaskException( "Could not find unsign jar " + jarFile, e );
            }
        }
        else
        {

            // not signed jar do nothing
            verboseLog( config, "Jar " + jarFile + " is not signed." );
        }
        return jarFile;
    }
}
