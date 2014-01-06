package org.codehaus.mojo.webstart.dependency.filenaming;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with work for additional information
 * regarding copyright ownership.  The ASF licenses file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use file except in compliance
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

import org.apache.commons.lang.StringUtils;
import org.apache.maven.artifact.Artifact;
import org.codehaus.plexus.component.annotations.Component;

/**
 * Provide a safe naming strategy that avoid any colision name.
 * <p/>
 * The syntax of this naming strategy:
 * <pre>groupdId-artifactId[-classifier][(__V|.)version].extension</pre>
 * <p/>
 * Created on 1/6/14.
 *
 * @author Tony Chemit <chemit@codelutin.com>
 * @since 1.0-beta-5
 */
@Component(role = DependencyFilenameStrategy.class, hint = FullDependencyFilenameStrategy.ROLE_HINT)
public class FullDependencyFilenameStrategy
    extends AbstractDependencyFilenameStrategy
    implements DependencyFilenameStrategy
{

    public static final String ROLE_HINT = "full";

    /**
     * {@inheritDoc}
     */
    public String getDependencyFileBasename( Artifact artifact, Boolean outputJarVersion )
    {
        String filename = artifact.getGroupId() + "-" + artifact.getArtifactId();

        if ( StringUtils.isNotEmpty( artifact.getClassifier() ) )
        {
            filename += "-" + artifact.getClassifier();
        }

        if ( outputJarVersion != null )
        {

            if ( outputJarVersion )
            {
                filename += "__V";
            }
            else
            {
                filename += "-";
            }
            filename += artifact.getVersion();
        }
        return filename;
    }
}
