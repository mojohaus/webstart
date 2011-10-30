package org.codehaus.mojo.webstart;

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
