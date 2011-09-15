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

    void packJars( File directory, FileFilter jarFileFilter, boolean gzip );

    void unpackJars( File directory, FileFilter pack200FileFilter );
}
