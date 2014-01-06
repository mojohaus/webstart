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
import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.mojo.webstart.dependency.task.JnlpDependencyTask;
import org.codehaus.mojo.webstart.dependency.task.JnlpDependencyTaskException;
import org.codehaus.mojo.webstart.util.IOUtil;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.AbstractLogEnabled;

import java.io.File;
import java.util.List;

/**
 * Created on 1/4/14.
 *
 * @author Tony Chemit <chemit@codelutin.com>
 * @since 1.0-beta-5
 */
@Component( role = JnlpDependencyRequestConsumer.class )
public class DefaultJnlpDependencyRequestConsumer
    extends AbstractLogEnabled
    implements JnlpDependencyRequestConsumer
{

    @Requirement
    private IOUtil ioUtil;

    private int maxThreads;

    private boolean failFast;

    /**
     * {@inheritDoc}
     */
    public void setMaxThreads( int maxThreads )
    {
        this.maxThreads = maxThreads;
    }

    /**
     * {@inheritDoc}
     */
    public void setFailFast( boolean failFast )
    {
        this.failFast = failFast;
    }

    /**
     * {@inheritDoc}
     */
    public JnlpDependencyResults execute( JnlpDependencyRequests requests )
    {

        List<JnlpDependencyRequest> dependencyRequests = requests.getRequests();

        getLogger().info( "Process " + dependencyRequests.size() + " requests." );

        JnlpDependencyResults results = new JnlpDependencyResults();

        for ( JnlpDependencyRequest request : dependencyRequests )
        {
            JnlpDependencyResult result = execute( request );
            results.registerResult( request, result );
            if ( failFast && result.isError() )
            {
                getLogger().warn( "Fail fast after first dependency processing error." );
                break;
            }
        }
        return results;
    }

    protected JnlpDependencyResult execute( JnlpDependencyRequest request )
    {

        JnlpDependencyConfig config = request.getConfig();

        JnlpDependencyResult result = prepareResult( config );

        File workingDirectory = config.getWorkingDirectory();

        File workingFile = result.getOriginalfile();

        JnlpDependencyTask[] tasks = request.getTasks();

        for ( int i = 0, length = tasks.length; i < length; i++ )
        {

            JnlpDependencyTask task = tasks[i];

            // copy previous file to a new task isolated directory
            File newDirectory = new File( workingDirectory, i + "_" + task.getClass().getSimpleName() );
            try
            {
                ioUtil.copyFileToDirectoryIfNecessary( workingFile, newDirectory );
            }
            catch ( MojoExecutionException e )
            {
                result.setError( e );
                break;
            }
            workingFile = new File( newDirectory, workingFile.getName() );

            getLogger().debug( String.format( "[task %s] (%s): workingFile: %s", i, task, workingFile ) );
            try
            {
                workingFile = task.execute( config, workingFile );
            }
            catch ( JnlpDependencyTaskException e )
            {
                result.setError( e );
                break;
            }
        }

        // copy to final destination

        finalizeResult( config, workingFile, result );

        getLogger().info( "Dependency " + config.getArtifact().getId() + " treated." );

        return result;
    }

    private JnlpDependencyResult prepareResult( JnlpDependencyConfig config )
    {

        File workingDirectory = config.getWorkingDirectory();

        Artifact artifact = config.getArtifact();

        File incomingFile = artifact.getFile();

        String fileName = config.getDependencyFilenameStrategy().getDependencyFilename( artifact, false );

        File workingFile = new File( workingDirectory, fileName );

        JnlpDependencyResult result = new JnlpDependencyResult( artifact, workingFile );

        copyFile( incomingFile, workingFile, result );
        return result;
    }

    private void finalizeResult( JnlpDependencyConfig config, File workingFile, JnlpDependencyResult result )
    {

        // copy to final destination

        File finalDirectory = config.getFinalDirectory();
        String filename = config.getDependencyFilenameStrategy().getDependencyFilename( config.getArtifact(),
                                                                                        config.isOutputJarVersion() );

        if ( config.isPack200() )
        {
            filename += ".pack";
        }

        if ( config.isGzip() )
        {
            filename += ".gz";
        }

        File finalFile = new File( finalDirectory, filename );

        copyFile( workingFile, finalFile, result );
        result.setFinalFile( finalFile );
    }

    private void copyFile( File source, File destination, JnlpDependencyResult result )
    {
        getLogger().debug( "Copy " + source.getName() + " to " + destination );
        try
        {
            ioUtil.copyFile( source, destination );
        }
        catch ( MojoExecutionException e )
        {
            result.setError( e );
        }
    }
}
