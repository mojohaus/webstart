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

import org.apache.maven.plugin.MojoExecutionException;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

/**
 * Helper for all IO operations.
 *
 * @author tchemit <chemit@codelutin.com>
 * @since 1.0-beta-4
 */
public interface IOUtil
{

    void copyResources( File resourcesDir, File workDirectory )
        throws IOException, MojoExecutionException;

    /**
     * Conditionally copy the file into the target directory.
     * The operation is not performed when the target file exists and is up to date.
     * The target file name is taken from the <code>sourceFile</code> name.
     *
     * @param sourceFile      source file to copy
     * @param targetDirectory location of the target directory where to copy file
     * @return <code>true</code> when the file was copied, <code>false</code> otherwise.
     * @throws IllegalArgumentException if sourceFile is <code>null</code> or
     *                                  <code>sourceFile.getName()</code> is <code>null</code>
     * @throws IOException              if an error occurs attempting to copy the file.
     */
    boolean copyFileToDirectoryIfNecessary( File sourceFile, File targetDirectory )
        throws IOException;

    /**
     * Delete the specified directory.
     *
     * @param dir the directory to delete
     * @throws MojoExecutionException if could not delete directory
     */
    public void removeDirectory( File dir )
        throws MojoExecutionException;

    public void makeDirectoryIfNecessary( File dir )
        throws MojoExecutionException;

    /**
     * @param directory  location of directory where to delete some files
     * @param fileFilter filter to select files to delete
     * @return the number of deleted files
     * @throws MojoExecutionException if could not delete files
     */
    public int deleteFiles( File directory, FileFilter fileFilter )
        throws MojoExecutionException;

    public void deleteFile( File file )
        throws MojoExecutionException;

    public void renameTo( File source, File target )
        throws MojoExecutionException;

}
