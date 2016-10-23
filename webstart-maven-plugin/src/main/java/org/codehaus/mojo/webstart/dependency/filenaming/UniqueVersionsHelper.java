package org.codehaus.mojo.webstart.dependency.filenaming;

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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.maven.artifact.Artifact;

public class UniqueVersionsHelper {

    private final static Pattern NONUNIQUE_SNAPSHOT_PATTERN = Pattern.compile("^(.*)-SNAPSHOT$");

    public static final String getUniqueVersion( Artifact artifact )
    {
        Matcher m = NONUNIQUE_SNAPSHOT_PATTERN.matcher( artifact.getVersion() );
        if ( m.matches() ) 
        {
            // version is not unique, replace SNAPSHOT with file modification timestamp
            Date d = new Date( artifact.getFile().lastModified() );
            DateFormat df = new SimpleDateFormat( "yyyyMMdd.HHmmss" );
            df.setTimeZone( TimeZone.getTimeZone( "UTC" ) );
            return m.group( 1 ) + "-" + df.format( d ) + "-0";
        }
        else
        {
            return artifact.getVersion();
        }
    }

}
