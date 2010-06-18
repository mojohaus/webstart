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
 * This class represents a &lt;jarResource&gt; configuration element from the
 * pom.xml file. It identifies an artifact that is to be processed by the plugin
 * for inclusion in the JNLP bundle. 
 *
 * @author Kevin Stembridge
 * @author $LastChangedBy$
 * @since 19 May 2007
 * @version $Revision$
 *
 */
public class JarResource
{
    
    private String groupId;
    
    private String artifactId;
    
    private String version;
    
    private String classifier;
    
    private String mainClass;
    
    private boolean outputJarVersion = true;
    
    private Artifact artifact;
    
    private String hrefValue;
    
    private boolean includeInJnlp = true;
    
    /**
     * Creates a new uninitialized {@code JarResource}.
     */
    public JarResource()
    {
        // do nothing
    }
    
    /**
     * Creates a new {@code JarResource} that wraps the given artifact.
     *
     * @param artifact The artifact that this instance represents.
     * 
     * @throws IllegalArgumentException if {@code artifact} is null.
     */
    public JarResource( Artifact artifact )
    {
        setArtifact( artifact );
    }

    /**
     * Returns true if the given object is a JarResource and has the same
     * combination of <code>groupId</code>, <code>artifactId</code>, 
     * <code>version</code> and <code>classifier</code>.
     */
    public boolean equals( Object obj ) 
    {
        
        if ( obj == this )
        {
            return true;
        }
        
        if ( ! ( obj instanceof JarResource ) )
        {
            return false;
        }
        
        JarResource other = ( JarResource ) obj;
        
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
            return ! field1.equals( field2 );
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
        result += multiplier * fieldHashCode( getClassifier() );
        return result;
        
    }
    
    private int fieldHashCode( Object field )
    {
        return field == null ? 0 : field.hashCode();
    }
    
    /**
     * Returns the value of the artifactId field.
     * @return Returns the value of the artifactId field.
     */
    public String getArtifactId()
    {
        return this.artifactId;
    }

    /**
     * Returns the value of the classifier field.
     * @return Returns the value of the classifier field.
     */
    public String getClassifier()
    {
        return this.classifier;
    }

    /**
     * Returns the value of the groupId field.
     * @return Returns the value of the groupId field.
     */
    public String getGroupId()
    {
        return this.groupId;
    }

    /**
     * Returns the value of the version field.
     * @return Returns the value of the version field.
     */
    public String getVersion()
    {
        return this.version;
    }

    /**
     * Returns the fully qualified class name of the JNLP application's 'main' class but
     * only if it is contained in the jar represented by this instance. Only one jarResource per 
     * plugin configuration can be declared with a main class. This is the value that will be 
     * populated in the generated JNLP file. 
     * @return Returns the value of the mainClass field, or null if the jar represented 
     * by this instance is not the one that contains the application's main class.
     */
    public String getMainClass()
    {
        return this.mainClass;
    }

    /**
     * Sets the flag that indicates whether or not the jar resource 
     * element in the generated JNLP file should include a version attribute.
     * Default is true.
     * @param outputJarVersion 
     */
    protected void setOutputJarVersion( boolean outputJarVersion )
    {
        this.outputJarVersion = outputJarVersion;
    }

    /**
     * Returns the flag that indicates whether or not the jar resource 
     * element in the generated JNLP file should include a version attribute.
     * Default is true.
     * @return Returns the value of the outputJarVersion field.
     */
    public boolean isOutputJarVersion()
    {
        return this.outputJarVersion;
    }

    /**
     * Returns the underlying artifact that this instance represents.
     * @return Returns the value of the artifact field.
     */
    public Artifact getArtifact()
    {
        return this.artifact;
    }

    /**
     * Sets the underlying artifact that this instance represents.
     * @param artifact 
     * @throws IllegalArgumentException if {@code artifact} is null.
     */
    public void setArtifact( Artifact artifact )
    {
        if ( artifact == null )
        {
            throw new IllegalArgumentException( "artifact must not be null" );
        }
        this.artifact = artifact;
        this.artifactId = artifact.getArtifactId();
        this.classifier = artifact.getClassifier();
        this.groupId = artifact.getGroupId();
        this.version = artifact.getVersion();
    }

    /**
     * Sets the value that should be output for this jar in the href attribute of the 
     * jar resource element in the generated JNLP file. If not set explicitly, this defaults 
     * to the file name of the underlying artifact.
     * @param hrefValue 
     */
    protected void setHrefValue( String hrefValue )
    {
        this.hrefValue = hrefValue;
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
        if ( this.hrefValue == null && this.artifact != null ) 
        {
            return this.artifact.getFile().getName();
        }
        return this.hrefValue;
    }
    
    /**
     * Returns the flag that indicates whether or not this resource should be included 
     * in the generated JNLP file. The default is true, but you may want to exclude jars
     * from the JNLP in cases where multiple versions of a jar are included in the JNLP bundle.
     * 
     * @return Returns the value of the includeInJnlp field.
     */
    public boolean isIncludeInJnlp()
    {
        return this.includeInJnlp;
    }

    /**
     * {@inheritDoc}
     */
    public String toString() 
    {
        StringBuffer sbuf = new StringBuffer();
        sbuf.append( "JarResource[ groupId='" )
            .append( this.groupId )
            .append( "', artifactId='" )
            .append( this.artifactId )
            .append( "', version='" )
            .append( this.version )
            .append( "', classifier='" )
            .append( this.classifier )
            .append( "', mainClass='" )
            .append( this.mainClass )
            .append( "', outputJarVersion='" )
            .append( this.outputJarVersion )
            .append( "', hrefValue='" )
            .append( this.hrefValue )
            .append( "' ]" );
        return sbuf.toString();
    }
}
