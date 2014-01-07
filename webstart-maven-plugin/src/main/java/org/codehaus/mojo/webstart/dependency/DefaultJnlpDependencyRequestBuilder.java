package org.codehaus.mojo.webstart.dependency;

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
import org.apache.maven.shared.jarsigner.JarSignerUtil;
import org.codehaus.mojo.webstart.dependency.task.JnlpDependencyTask;
import org.codehaus.mojo.webstart.dependency.task.Pack200Task;
import org.codehaus.mojo.webstart.dependency.task.SignTask;
import org.codehaus.mojo.webstart.dependency.task.UnPack200Task;
import org.codehaus.mojo.webstart.dependency.task.UnsignTask;
import org.codehaus.mojo.webstart.dependency.task.UpdateManifestTask;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * To create some {@link JnlpDependencyRequest}.
 * Created on 1/4/14.
 *
 * @author Tony Chemit <chemit@codelutin.com>
 * @since 1.0-beta-5
 */
@Component( role = JnlpDependencyRequestBuilder.class, instantiationStrategy = "per-lookup" )
public class DefaultJnlpDependencyRequestBuilder
    extends AbstractLogEnabled
    implements JnlpDependencyRequestBuilder, Contextualizable
{

    private PlexusContainer container;

    private JnlpDependencyGlobalConfig globalConfig;

    /**
     * {@inheritDoc}
     */
    public void init( JnlpDependencyGlobalConfig globalConfig )
    {
        if ( globalConfig == null )
        {
            throw new NullPointerException( "Can't use a null *globalConfig*" );
        }
        this.globalConfig = globalConfig;
    }

    /**
     * {@inheritDoc}
     */
    public JnlpDependencyRequests createRequests()
    {
        return new JnlpDependencyRequests( globalConfig );
    }

    /**
     * {@inheritDoc}
     */
    public JnlpDependencyRequest createRequest( Artifact artifact, boolean outputJarVersion )
    {

        if ( globalConfig == null )
        {
            throw new IllegalStateException( "No config found, use init method before creating a request" );
        }

        JnlpDependencyConfig config = createConfig( artifact, outputJarVersion );

        JnlpDependencyTask[] tasks = createTasks( config );

        JnlpDependencyRequest request = new JnlpDependencyRequest( config, tasks );
        return request;
    }

    private JnlpDependencyConfig createConfig( Artifact artifact, boolean outputJarVersion )
    {

        String finalName = globalConfig.getDependencyFilenameStrategy().getDependencyFileBasename( artifact, false );

        return new JnlpDependencyConfig( globalConfig, artifact, finalName, outputJarVersion );
    }

    private JnlpDependencyTask[] createTasks( JnlpDependencyConfig config )
    {
        List<JnlpDependencyTask> tasks = new ArrayList<JnlpDependencyTask>();

        boolean doPack200 = config.isPack200();

        if ( config.isSign() )
        {

            if ( config.isUnsignAlreadySignedJars() )
            {

                boolean signed;
                try
                {
                    signed = JarSignerUtil.isArchiveSigned( config.getArtifact().getFile() );
                }
                catch ( IOException e )
                {
                    throw new RuntimeException( "Could not check if jar is signed", e );
                }

                if ( signed && config.isCanUnsign() )
                {

                    // unsign
                    registerTask( tasks, UnsignTask.ROLE_HINT, config );
                }
            }

            if ( doPack200 )
            {

                // http://java.sun.com/j2se/1.5.0/docs/guide/deployment/deployment-guide/pack200.html
                // we need to pack then unpack the files before signing them

                // pack200
                registerTask( tasks, Pack200Task.ROLE_HINT, config );

                // unpack200
                registerTask( tasks, UnPack200Task.ROLE_HINT, config );
            }

            if ( config.isUpdateManifest() )
            {
                // update manifest
                registerTask( tasks, UpdateManifestTask.ROLE_HINT, config );
            }

            // sign jar
            registerTask( tasks, SignTask.ROLE_HINT, config );
        }

        if ( doPack200 )
        {

            // pack signed jar
            registerTask( tasks, Pack200Task.ROLE_HINT, config );
        }

        return tasks.toArray( new JnlpDependencyTask[tasks.size()] );
    }

    /**
     * {@inheritDoc}
     */
    public void contextualize( final Context context )
        throws ContextException
    {
        container = (PlexusContainer) context.get( PlexusConstants.PLEXUS_KEY );
    }

    protected void setContainer( final PlexusContainer container )
    {
        this.container = container;
    }

    protected <Task extends JnlpDependencyTask> Task registerTask( List<JnlpDependencyTask> tasks, String roleHint,
                                                                   JnlpDependencyConfig config )
    {
        try
        {
            // create task
            Task result = (Task) container.lookup( JnlpDependencyTask.ROLE, roleHint );

            // check configution
            result.check( config );

            // register task
            tasks.add( result );

            return result;
        }
        catch ( ComponentLookupException e )
        {
            throw new RuntimeException( "Could not find task with roleHint: " + roleHint, e );
        }
    }
}
