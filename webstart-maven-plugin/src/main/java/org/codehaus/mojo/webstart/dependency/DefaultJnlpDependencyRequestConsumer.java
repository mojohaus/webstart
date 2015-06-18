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

import org.codehaus.mojo.webstart.dependency.task.JnlpDependencyTask;
import org.codehaus.mojo.webstart.util.IOUtil;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.logging.Logger;

import java.io.File;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

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

    /**
     * {@inheritDoc}
     */
    public JnlpDependencyResults execute( JnlpDependencyRequestConsumerConfig config, JnlpDependencyRequests requests )
    {

        getLogger().info( "Process " + requests.getNbRequests() + " dependencies." );

        RequestExecutor executor = new RequestExecutor( getLogger(), ioUtil, config );

        executor.registerRequests( requests.getRequests() );

        JnlpDependencyResults results = executor.terminatesAndWaits();

        return results;
    }

    private static class RequestExecutor
        extends ThreadPoolExecutor
    {

        private final JnlpDependencyRequestConsumerConfig config;

        private final Logger logger;

        private final IOUtil ioUtil;

        private final JnlpDependencyResults results;

        public RequestExecutor( Logger logger, IOUtil ioUtil, JnlpDependencyRequestConsumerConfig config )
        {
            super( config.getMaxThreads(), config.getMaxThreads(), 1L, TimeUnit.SECONDS,
                   new LinkedBlockingQueue<Runnable>() );
            this.logger = logger;
            this.ioUtil = ioUtil;
            this.config = config;
            this.results = new JnlpDependencyResults();
        }


        @Override
        protected void afterExecute( Runnable r, Throwable t )
        {
            super.afterExecute( r, t );
            RequestTask task = (RequestTask) r;

            JnlpDependencyResult result = task.result;

            results.registerResult( task.request, result );

            boolean withError = t != null;

            if ( withError )
            {
                result.setError( t );

                if ( config.isFailFast() )
                {
                    logger.warn( "Fail fast after first dependency processing error." );

                    //TODO Stop the executor
                    shutdownNow();
                }
            }


        }

        /**
         * Ask the thread to stop.
         * <p/>
         * It will finish all incoming files (but will not accept more files to
         * parse)
         * <p/>
         * <b>Note:</b> The method does not return until all files are not consumed.
         */
        public JnlpDependencyResults terminatesAndWaits()
        {
            // ask executor to terminate
            shutdown();

            try
            {
                // wait until all submited jobs are terminated
                // i don't want timeout, i think 2 days is good :)
                awaitTermination( 2 * 60 * 60 * 24, TimeUnit.SECONDS );
            }
            catch ( InterruptedException e )
            {
                logger.error( "Could not stop the executor after two days...", e );
            }

            return results;
        }

        public void registerRequests( List<JnlpDependencyRequest> dependencyRequests )
        {

            for ( JnlpDependencyRequest dependencyRequest : dependencyRequests )
            {
                RequestTask newtask = new RequestTask( logger, ioUtil, dependencyRequest );

                JnlpDependencyResult result = newtask.result;
                if ( result.isUptodate() )
                {
                    if ( config.isVerbose() )
                    {
                        logger.info(
                            "Skip up-to-date dependency: " + dependencyRequest.getConfig().getArtifact().getId() );
                    }
                    results.registerResult( newtask.request, result );
                }
                else
                {
                    if ( config.isVerbose() )
                    {
                        logger.info( "Process dependency: " + dependencyRequest.getConfig().getArtifact().getId() );
                    }
                    execute( newtask );
                }
            }
        }
    }


    private static class RequestTask
        implements Runnable
    {

        private final Logger logger;

        private final IOUtil ioUtil;

        private final JnlpDependencyRequest request;

        private JnlpDependencyResult result;

        private RequestTask( Logger logger, IOUtil ioUtil, JnlpDependencyRequest request )
        {
            this.logger = logger;
            this.ioUtil = ioUtil;
            this.request = request;
            this.result = new JnlpDependencyResult( request );
        }

        /**
         * {@inheritDoc}
         */
        public void run()
        {
            JnlpDependencyConfig config = request.getConfig();

            File workingFile = request.getOriginalFile();

            try
            {
                // copy artifact file to original file
                ioUtil.copyFile( config.getArtifact().getFile(), workingFile );

                File workingDirectory = config.getWorkingDirectory();

                JnlpDependencyTask[] tasks = request.getTasks();

                for ( int i = 0, length = tasks.length; i < length; i++ )
                {

                    JnlpDependencyTask task = tasks[i];

                    // copy previous file to a new task isolated directory
                    File newDirectory = new File( workingDirectory, i + "_" + task.getClass().getSimpleName() );
                    ioUtil.copyFileToDirectoryIfNecessary( workingFile, newDirectory );

                    workingFile = new File( newDirectory, workingFile.getName() );

                    logger.debug( String.format( "[task %s] (%s): workingFile: %s", i, task, workingFile ) );

                    workingFile = task.execute( config, workingFile );
                }

                // copy to final destination
                ioUtil.copyFile( workingFile, request.getFinalFile() );
            }
            catch ( Exception e )
            {
                result.setError( e );
            }

            logger.info( "Dependency " + config.getArtifact().getId() + " treated." );
        }
    }
}
