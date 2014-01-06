package org.codehaus.mojo.webstart.dependency;

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

import org.apache.commons.collections.MapUtils;
import org.codehaus.mojo.webstart.Pack200Config;
import org.codehaus.mojo.webstart.dependency.filenaming.DependencyFilenameStrategy;
import org.codehaus.mojo.webstart.sign.SignConfig;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created on 1/4/14.
 *
 * @author Tony Chemit <chemit@codelutin.com>
 * @since 1.0-beta-5
 */
public class JnlpDependencyGlobalConfig
{

    private final ClassLoader loader;

    private final File workingDirectory;

    private final File finalDirectory;

    private final Pack200Config pack200;

    private final SignConfig sign;

    private final Map<String, String> updateManifestEntries;

    private final boolean gzip;

    private final boolean verbose;

    private final boolean unsignAlreadySignedJars;

    private final boolean canUnsign;

    private final DependencyFilenameStrategy dependencyFilenameStrategy;

    public JnlpDependencyGlobalConfig( ClassLoader loader, DependencyFilenameStrategy dependencyFilenameStrategy,
                                       File workingDirectory, File finalDirectory, Pack200Config pack200,
                                       SignConfig sign, Map<String, String> updateManifestEntries, boolean gzip,
                                       boolean verbose, boolean unsignAlreadySignedJars, boolean canUnsign )
    {
        this.loader = loader;
        this.dependencyFilenameStrategy = dependencyFilenameStrategy;
        this.workingDirectory = workingDirectory;
        this.finalDirectory = finalDirectory;
        this.pack200 = pack200;
        this.sign = sign;
        this.updateManifestEntries = Collections.unmodifiableMap( updateManifestEntries );
        this.gzip = gzip;
        this.verbose = verbose;
        this.unsignAlreadySignedJars = unsignAlreadySignedJars;
        this.canUnsign = canUnsign;
    }


    public DependencyFilenameStrategy getDependencyFilenameStrategy()
    {
        return dependencyFilenameStrategy;
    }

    public File getWorkingDirectory()
    {
        return workingDirectory;
    }

    public File getFinalDirectory()
    {
        return finalDirectory;
    }

    /**
     * Returns the files to be passed without pack200 compression.
     *
     * @return Returns the list value of the pack200.passFiles.
     */
    public List<String> getPack200PassFiles()
    {
        return isPack200() ? pack200.getPassFiles() : null;
    }

    public SignConfig getSign()
    {
        return sign;
    }

    public Map<String, String> getUpdateManifestEntries()
    {
        return updateManifestEntries;
    }

    public boolean isGzip()
    {
        return gzip;
    }

    public boolean isVerbose()
    {
        return verbose;
    }

    public boolean isUnsignAlreadySignedJars()
    {
        return unsignAlreadySignedJars;
    }

    public boolean isCanUnsign()
    {
        return canUnsign;
    }

    public boolean isSign()
    {
        return sign != null;
    }

    public ClassLoader getLoader()
    {
        return loader;
    }

    public boolean isUpdateManifest()
    {
        return MapUtils.isNotEmpty( updateManifestEntries );
    }

    public boolean isPack200()
    {
        return pack200 != null && pack200.isEnabled();
    }
}
