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

import org.codehaus.mojo.webstart.Pack200Tool;
import org.codehaus.mojo.webstart.dependency.JnlpDependencyConfig;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * To pack200 a dependency.
 * <p/>
 * Created on 1/4/14.
 *
 * @author Tony Chemit <chemit@codelutin.com>
 * @since 1.0-beta-5
 */
@Component( role = JnlpDependencyTask.class, hint = Pack200Task.ROLE_HINT, instantiationStrategy = "per-lookup" )
public class Pack200Task
    extends AbstractJnlpTask
{

    public static final String ROLE_HINT = "Pack200Task";

    /**
     * All available pack200 tools.
     * <p/>
     * We use a plexus list injection instead of a direct component injection since for a jre 1.4, we will at the
     * moment have no implementation of this tool.
     */
    @Requirement( role = Pack200Tool.class )
    private List<Pack200Tool> pack200Tools;

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
        if (!config.isPack200()) {
            throw new IllegalStateException( "Can't unpack200 if config.isPack200 is false" );
        }
    }

    /**
     * {@inheritDoc}
     */
    public File execute( JnlpDependencyConfig config, File file )
        throws JnlpDependencyTaskException
    {

        verboseLog( config, "Pack200 file: " + file );
        try
        {
            File result = getPack200Tool().packJar( file, config.isGzip(), config.getPack200PassFiles() );
            getLogger().debug( "packed200 file: " + result );
            return result;
        }
        catch ( IOException e )
        {
            throw new JnlpDependencyTaskException( "Could not pack200 jars: ", e );
        }
    }

    protected Pack200Tool getPack200Tool()
    {
        return pack200Tools.get( 0 );
    }
}
