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

import org.apache.maven.plugin.logging.Log;

import org.codehaus.mojo.webstart.SignConfig;

import org.codehaus.mojo.webstart.JarSignerMojo;
/*
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.artifact.resolver.filter.AndArtifactFilter;
import org.apache.maven.artifact.resolver.filter.IncludesArtifactFilter;
import org.apache.maven.artifact.resolver.filter.ExcludesArtifactFilter;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.PluginManager;
import org.apache.maven.plugin.jar.JarSignMojo;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.apache.maven.settings.Settings;

import org.codehaus.mojo.keytool.GenkeyMojo;
import org.codehaus.mojo.webstart.generator.Generator;
import org.codehaus.plexus.archiver.zip.ZipArchiver;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.DirectoryScanner;

import org.apache.commons.lang.SystemUtils;


import java.io.FileFilter;
import java.io.IOException;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

*/

import java.io.File;

/**
 * Bean that represents the JarSigner configuration.
 * 
 * Specific to the JarSignMojo
 *
 * @author <a href="jerome@coffeebreaks.org">Jerome Lacoste</a>
 * @version $Id$
 */
public class MyJarSignMojoConfig implements SignConfig {
    
    /**
     * Returns a fully configured version of a Mojo ready to sign jars.
     * @return
     */
    public JarSignerMojo getJarSignerMojo() {
        MySignerMojo signJar = new MySignerMojo();
        
        signJar.param = getParam();
        
        return signJar;
    }
    
    public void init(Log log, File workDirectory, boolean verbose) {
    }

    /**
     */
    private String param;

    public void setParam( String param ) {
        this.param = param;
    }

    public String getParam() {
        return param;
    }
}
