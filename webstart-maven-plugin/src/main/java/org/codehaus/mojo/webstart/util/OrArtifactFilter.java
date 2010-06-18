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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;


/**
 * An 'OR' artifact filter
 *
 * TODO this functionality must be available somewhere else
 *
 * @author jerome@coffeebreaks.org
 */
public class OrArtifactFilter implements ArtifactFilter
{
    private final List filters = new ArrayList();

    public boolean include( final Artifact artifact )
    {
        boolean include = false;
        for ( final Iterator iterator = this.filters.iterator(); iterator.hasNext(); )
        {
            ArtifactFilter filter = (ArtifactFilter) iterator.next();
            if ( filter.include( artifact ) )
            {
                include = true;
                break;
            }
        }
        return include;
    }

    /**
     * Adds the artifact filter to be applied.
     *
     * @param artifactFilter
     */
    public void add( final ArtifactFilter artifactFilter )
    {
        this.filters.add( artifactFilter );
    }
}
