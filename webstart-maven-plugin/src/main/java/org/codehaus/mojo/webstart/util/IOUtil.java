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

import java.io.File;
import java.io.FileFilter;
import java.net.URI;
import java.util.zip.ZipFile;

import org.apache.maven.plugin.MojoExecutionException;

/**
 * Helper for all IO operations.
 *
 * @author tchemit <chemit@codelutin.com>
 * @since 1.0-beta-4
 */
public interface IOUtil
{

    /**
     * Plexus component role.
     */
    String ROLE = IOUtil.class.getName();

    /**
     * Copy the content of a directory to another one recursively.
     *
     * @param sourceDirectory directory to copy
     * @param targetDirectory where to copy
     * @throws MojoExecutionException if could not perform operation
     */
    void copyResources( File sourceDirectory, File targetDirectory )
        throws MojoExecutionException;

    /**
     * Copy directory structure from {@code sourceDirectory} to {@code targetDirectory}.
     *
     * @param sourceDirectory source of copy
     * @param targetDirectory target of copy
     * @throws MojoExecutionException if could not perform operation
     */
    void copyDirectoryStructure( File sourceDirectory, File targetDirectory )
        throws MojoExecutionException;

    /**
     * @param sourceFile source file
     * @param targetFile target file
     * @return {@code true} if source file should be copy to target location
     */
    boolean shouldCopyFile( File sourceFile, File targetFile );

    /**
     * Conditionally copy the file into the target directory.
     * The operation is not performed when the target file exists and is up to date.
     * The target file name is taken from the <code>sourceFile</code> name.
     *
     * @param sourceFile      source file to copy
     * @param targetDirectory location of the target directory where to copy file
     * @return <code>true</code> when the file was copied, <code>false</code> otherwise.
     * @throws MojoExecutionException if an error occurs attempting to copy the file.
     */
    boolean copyFileToDirectoryIfNecessary( File sourceFile, File targetDirectory )
        throws MojoExecutionException;

    void copyFile( File sourceFile, File targetFile )
    throws MojoExecutionException;

    /**
     * Delete the specified directory.
     *
     * @param dir the directory to delete
     * @throws MojoExecutionException if could not delete directory
     */
    void removeDirectory( File dir )
        throws MojoExecutionException;

    /**
     * Create the given directory if it does not exist.
     * <p/>
     * will throw an exception if could not perform the operation.
     *
     * @param dir the dir to create if it does not exist
     * @throws MojoExecutionException if could not create directory
     */
    void makeDirectoryIfNecessary( File dir )
        throws MojoExecutionException;

    /**
     * @param directory  location of directory where to delete some files
     * @param fileFilter filter to select files to delete
     * @return the number of deleted files
     * @throws MojoExecutionException if could not delete files
     */
    int deleteFiles( File directory, FileFilter fileFilter )
        throws MojoExecutionException;

    /**
     * Delete a file.
     * <p/>
     * will throw an exception if could not perform the operation.
     *
     * @param file the file to delete
     * @throws MojoExecutionException if could not delete file
     */
    void deleteFile( File file )
        throws MojoExecutionException;

    /**
     * Rename a file.
     * <p/>
     * will throw an exception if could not perform the operation.
     *
     * @param source original file to renmae
     * @param target target file
     * @throws MojoExecutionException if could not rename file
     */
    void renameTo( File source, File target )
        throws MojoExecutionException;

    /**
     * Copy a resource from the given uri to {@code target} file.
     * <p/>
     * The resource can come from class-path is the scheme is {@code classpath}, otherwise will try to get incoming
     * resource from the url obtained from the uri.
     *
     * @param uri         uri to copy
     * @param classLoader classloader used to find resource in from classpaht
     * @param target      where to copy
     * @throws MojoExecutionException if something wrong happen
     */
    void copyResources( URI uri, ClassLoader classLoader, File target )
        throws MojoExecutionException;

    /**
     * Silently closes the resource
     *
     * @param closeable
     */
    void close( ZipFile closeable );

    void createArchive(File directory, File archive)
        throws MojoExecutionException;
}
