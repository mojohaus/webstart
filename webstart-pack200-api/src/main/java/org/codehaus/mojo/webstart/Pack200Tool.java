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
 * Tool api for pack200 operations.
 * <p/>
 * <strong>Note: </strong>A such tool can only be used for a jre > 1.4.
 *
 * @author tchemit <chemit@codelutin.com>
 * @since 1.0-beta-2
 */
public interface Pack200Tool
{

    /**
     * Plexus component role.
     */
    String ROLE = Pack200Tool.class.getName();

    /**
     * Packs from the given {@code directory}, all files matched by the filter.
     * <p/>
     * If parameter {@code gzip} is setted to {@code true}, then after it gzip packed files.
     *
     * @param directory     the location of the directory containing files to pack
     * @param jarFileFilter the filter to determin which files to pack
     * @param gzip          flag to gzip files after pack them
     */
    void packJars( File directory, FileFilter jarFileFilter, boolean gzip );

    /**
     * UnPacks from the given {@code directory}, all files matched by the filter.
     *
     * @param directory         the location of the directory containing files to unpack
     * @param pack200FileFilter the fileter to determin which files to unpakc
     */
    void unpackJars( File directory, FileFilter pack200FileFilter );
}
