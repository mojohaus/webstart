package org.codehaus.mojo.webstart.test.mysigner;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License" );
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.logging.Log;

import org.codehaus.mojo.webstart.JarSignerMojo;

import java.io.File;

/**
 * Bean that represents the JarSigner configuration.
 * 
 * Specific to the JarSignMojo
 *
 * @author <a href="jerome@coffeebreaks.org">Jerome Lacoste</a>
 * @version $Id$
 */
public class MySignerMojo extends AbstractMojo implements JarSignerMojo {

    /**
     *
     * @parameter expression="${param}"
     * @required
     */
    String param;

    /**
     * Sets the location of the unsigned jar file.
     * @param jarPath
     */
    public void setJarPath(File jarPath) {
        getLog().info( "ignoring jar path " + jarPath );
    }
    
    /**
     * Sets the output filename for the signed jar.
     * This may be the same location as specified in setJarPath(). If this
     * is the case, the unsigned jar file will be overwritten with the
     * signed jar file.
     * @param signedJar
     */
    public void setSignedJar(File signedJar) {
        getLog().info( "ignoring signed jar " + signedJar );
    }
    
    /**
     * Executes the jar signing process.
     * 
     * Same throws declaration as AbstractMojo.execute() 
     * @throws MojoExecutionException
     * @throws MojoFailureException
     */
    public void execute() {
        getLog().info( "executing fake signer: param: <" + param + ">" );
    }
}
