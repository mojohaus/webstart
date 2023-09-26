package org.codehaus.mojo.webstart.pack200;

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
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Tool api for pack200 operations.
 *
 * @author Tony Chemit - dev@tchemit.fr
 * @since 1.0-beta-2
 */
public interface Pack200Tool
{

    /**
     * Plexus component role.
     */
    String ROLE = Pack200Tool.class.getName();

    /**
     * Extension of a pack file.
     */
    String PACK_EXTENSION = ".pack";

    /**
     * Extension of a gz pack file.
     */
    String PACK_GZ_EXTENSION = PACK_EXTENSION + ".gz";

    /**
     * Pack a jar.
     *
     * @param source      the source jar
     * @param destination the packed jar
     * @param props       the packing properties
     * @param gzip        true if the destination file
     * @throws IOException TODO
     */
    void pack( File source, File destination, Map<String, String> props, boolean gzip, boolean commonsCompress )
            throws IOException;

    /**
     * Repack a jar.
     *
     * @param source      the source jar
     * @param destination the destination jar (may be the same as the source jar)
     * @param props       the packing properties
     * @throws IOException TODO
     */
    void repack( File source, File destination, Map<String, String> props, boolean commonsCompress )
            throws IOException;

    /**
     * Unpack a jar.
     *
     * @param source      the packed jar
     * @param destination the unpacked jar
     * @param props       the packing properties
     * @throws IOException TODO
     */
    void unpack( File source, File destination, Map<String, String> props, boolean commonsCompress )
            throws IOException;

    /**
     * Packs from the given {@code directory}, all files matched by the filter.
     * <p>
     * If parameter {@code gzip} is setted to {@code true}, then after it gzip packed files.
     *
     * @param directory     the location of the directory containing files to pack
     * @param jarFileFilter the filter to determin which files to pack
     * @param gzip          flag to gzip files after pack them
     * @param passFiles     the list of file names to be passed as not pack200 compressed
     * @throws IOException TODO
     */
    void packJars( File directory, FileFilter jarFileFilter, boolean gzip, List<String> passFiles, boolean commonsCompress )
            throws IOException;

    /**
     * Pack the given jarfile and return the packed file.
     *
     * @param jarFile   jar file to pack
     * @param gzip      flag to enable gzip compression
     * @param passFiles the list of file names to be passed as not pack200 compressed
     * @return the packed file
     * @throws IOException TODO
     */
    File packJar( File jarFile, boolean gzip, List<String> passFiles, boolean commonsCompress )
            throws IOException;

    /**
     * UnPacks from the given {@code directory}, all files matched by the filter.
     *
     * @param directory         the location of the directory containing files to unpack
     * @param pack200FileFilter the fileter to determin which files to unpakc
     * @throws IOException TODO
     */
    void unpackJars( File directory, FileFilter pack200FileFilter, boolean commonsCompress )
            throws IOException;

    /**
     * Unpack the given file and return it.
     *
     * @param packFile the file to unpack
     * @return the unpacked file
     * @throws IOException TODO
     */
    File unpackJar( File packFile, boolean commonsCompress )
            throws IOException;
}
