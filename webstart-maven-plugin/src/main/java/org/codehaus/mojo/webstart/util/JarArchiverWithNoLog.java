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

import org.codehaus.plexus.archiver.Archiver;
import org.codehaus.plexus.archiver.jar.JarArchiver;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.logging.console.ConsoleLogger;

/**
 * A jar archiver with no info logs.
 *
 * Created on 10/26/13.
 *
 * @author Tony Chemit <chemit@codelutin.com>
 * @plexus.component role="org.codehaus.plexus.archiver.Archiver" role-hint="jarWithNoLog"
 * @since 1.0-beta-4
 */
public class JarArchiverWithNoLog
    extends JarArchiver implements Archiver
{

    Logger log = new ConsoleLogger( Logger.LEVEL_WARN, "console" );

    @Override
    protected Logger getLogger()
    {
        return log;
    }

}
