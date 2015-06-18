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

import org.codehaus.mojo.webstart.dependency.JnlpDependencyConfig;
import org.codehaus.plexus.logging.AbstractLogEnabled;

/**
 * Created on 1/4/14.
 *
 * @author Tony Chemit <chemit@codelutin.com>
 * @since XXX
 */
public abstract class AbstractJnlpTask
    extends AbstractLogEnabled
    implements JnlpDependencyTask
{

    protected void verboseLog( JnlpDependencyConfig config, String msg )
    {
        if ( config.isVerbose() )
        {
            getLogger().info( msg );
        }
        else
        {
            getLogger().debug( msg );
        }
    }

}
