package org.codehaus.mojo.webstart.util;

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
