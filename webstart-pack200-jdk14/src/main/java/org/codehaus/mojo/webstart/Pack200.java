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

import java.io.File;
import java.io.FileFilter;

/**
 * Handles pack200 operations for SDK 1.4.
 *
 * @author <a href="jerome@coffeebreaks.org">Jerome Lacoste</a>
 */
public class Pack200
{

    public static void packJars( File directory, FileFilter jarFileFilter, boolean gzip )
    {
        throw new IllegalStateException( "Pack200 doesn't exist for SDK 1.4. Should never be called. " 
                                         + "Compilation stub only" );
    }

    public static void unpackJars( File directory, FileFilter pack200FileFilter )
    {
        throw new IllegalStateException( "Pack200 doesn't exist for SDK 1.4. Should never be called. " 
                                         + "Compilation stub only" );
    }
}
