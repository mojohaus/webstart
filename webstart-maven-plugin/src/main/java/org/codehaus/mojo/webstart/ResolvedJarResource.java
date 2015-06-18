package org.codehaus.mojo.webstart;

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

import org.apache.maven.artifact.Artifact;

/**
 * Represents a resolved jarResource.
 * <p/>
 * Created on 10/29/13.
 *
 * @author Tony Chemit <chemit@codelutin.com>
 * @since 1.0-beta-4
 */
public class ResolvedJarResource
{
    /**
     * The underlying jar resource configuration (from pom).
     */
    private final JarResource jarResource;

    /**
     * The resolved artifact.
     */
    private final Artifact artifact;

    /**
     * The hrefValue to fill in JarResource file.
     */
    private String hrefValue;

    public ResolvedJarResource( Artifact artifact )
    {
        this( new JarResource(), artifact );
    }

    public ResolvedJarResource( JarResource jarResource, Artifact artifact )
    {
        if ( artifact == null )
        {
            throw new IllegalArgumentException( "artifact must not be null" );
        }
        if ( jarResource == null )
        {
            throw new IllegalArgumentException( "jarResource must not be null" );
        }
        this.jarResource = jarResource;
        this.artifact = artifact;
        setHrefValue( jarResource.getHrefValue() );
    }

    public String getArtifactId()
    {
        return artifact.getArtifactId();
    }

    public String getType()
    {
        return artifact.getType();
    }

    public String getClassifier()
    {
        return artifact.getClassifier();
    }

    public String getGroupId()
    {
        return artifact.getGroupId();
    }

    public String getVersion()
    {
        return artifact.getVersion();
    }

    public String getMainClass()
    {
        return jarResource.getMainClass();
    }

    public boolean isOutputJarVersion()
    {
        return jarResource.isOutputJarVersion();
    }

    public boolean isIncludeInJnlp()
    {
        return jarResource.isIncludeInJnlp();
    }

    /**
     * Returns the underlying artifact that this instance represents.
     *
     * @return Returns the value of the artifact field.
     */
    public Artifact getArtifact()
    {
        return artifact;
    }

    /**
     * Returns the value that should be output for this jar in the href attribute of the
     * jar resource element in the generated JNLP file. If not set explicitly, this defaults
     * to the file name of the underlying artifact.
     *
     * @return The href attribute to be output for this jar resource in the generated JNLP file.
     */
    public String getHrefValue()
    {
        String result;
        if ( hrefValue == null && getArtifact() != null )
        {
            // use default value
            result = getArtifact().getFile().getName();
        }
        else
        {
            // use customized value
            result = hrefValue;
        }
        return result;
    }

    /**
     * Sets the value that should be output for this jar in the href attribute of the
     * jar resource element in the generated JNLP file. If not set explicitly, this defaults
     * to the file name of the underlying artifact.
     *
     * @param hrefValue new value for field {@link #hrefValue}
     */
    public void setHrefValue( String hrefValue )
    {
        this.hrefValue = hrefValue;
    }

    /**
     * Returns true if the given object is a JarResource and has the same
     * combination of <code>groupId</code>, <code>artifactId</code>,
     * <code>version</code> and <code>classifier</code>.
     *
     * @return {@code true} if equals to given other object.
     */
    @Override
    public boolean equals( Object obj )
    {

        if ( obj == this )
        {
            return true;
        }

        if ( !( obj instanceof ResolvedJarResource ) )
        {
            return false;
        }

        ResolvedJarResource other = (ResolvedJarResource) obj;

        if ( fieldsAreNotEqual( getGroupId(), other.getGroupId() ) )
        {
            return false;
        }

        if ( fieldsAreNotEqual( getArtifactId(), other.getArtifactId() ) )
        {
            return false;
        }

        if ( fieldsAreNotEqual( getVersion(), other.getVersion() ) )
        {
            return false;
        }

        if ( fieldsAreNotEqual( getType(), other.getType() ) )
        {
            return false;
        }

        if ( fieldsAreNotEqual( getClassifier(), other.getClassifier() ) )
        {
            return false;
        }

        return true;

    }

    private boolean fieldsAreNotEqual( Object field1, Object field2 )
    {

        if ( field1 == null )
        {
            return field2 != null;
        }
        else
        {
            return !field1.equals( field2 );
        }

    }

    /**
     * {@inheritDoc}
     */
    public int hashCode()
    {
        final int offset = 17;
        final int multiplier = 37;
        int result = offset;
        result += multiplier * fieldHashCode( getGroupId() );
        result += multiplier * fieldHashCode( getArtifactId() );
        result += multiplier * fieldHashCode( getVersion() );
        result += multiplier * fieldHashCode( getType() );
        result += multiplier * fieldHashCode( getClassifier() );
        return result;

    }

    private int fieldHashCode( Object field )
    {
        return field == null ? 0 : field.hashCode();
    }
}

