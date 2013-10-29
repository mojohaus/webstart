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

import org.apache.commons.lang.StringUtils;

/**
 * This class represents a &lt;jarResource&gt; configuration element from the
 * pom.xml file. It identifies an artifact that is to be processed by the plugin
 * for inclusion in the JNLP bundle.
 *
 * @author Kevin Stembridge
 * @author $LastChangedBy$
 * @version $Revision$
 * @since 19 May 2007
 */
public class JarResource
{

    private String groupId;

    private String artifactId;

    private String version;

    private String classifier;

    private String mainClass;

    private boolean outputJarVersion = true;

    private boolean includeInJnlp = true;

    private String type;

    /**
     * Returns the value of the artifactId field.
     *
     * @return Returns the value of the artifactId field.
     */
    public String getArtifactId()
    {
        return artifactId;
    }

    /**
     * Returns the value of the type field.
     *
     * @return Returns the value of the type field.
     */
    public String getType()
    {
        return type;
    }

    /**
     * Returns the value of the classifier field.
     *
     * @return Returns the value of the classifier field.
     */
    public String getClassifier()
    {
        return classifier;
    }

    /**
     * Returns the value of the groupId field.
     *
     * @return Returns the value of the groupId field.
     */
    public String getGroupId()
    {
        return groupId;
    }

    /**
     * Returns the value of the version field.
     *
     * @return Returns the value of the version field.
     */
    public String getVersion()
    {
        return version;
    }

    /**
     * Returns the fully qualified class name of the JNLP application's 'main' class but
     * only if it is contained in the jar represented by this instance. Only one jarResource per
     * plugin configuration can be declared with a main class. This is the value that will be
     * populated in the generated JNLP file.
     *
     * @return Returns the value of the mainClass field, or null if the jar represented
     * by this instance is not the one that contains the application's main class.
     */
    public String getMainClass()
    {
        return mainClass;
    }

    public boolean isMandatoryField()
    {
        return StringUtils.isNotBlank( getGroupId() ) &&
            StringUtils.isNotBlank( getArtifactId() ) &&
            StringUtils.isNotBlank( getVersion() );
    }

    /**
     * Sets the flag that indicates whether or not the jar resource
     * element in the generated JNLP file should include a version attribute.
     * Default is true.
     *
     * @param outputJarVersion new value of field {@link #outputJarVersion}
     */
    protected void setOutputJarVersion( boolean outputJarVersion )
    {
        this.outputJarVersion = outputJarVersion;
    }

    /**
     * Returns the flag that indicates whether or not the jar resource
     * element in the generated JNLP file should include a version attribute.
     * Default is true.
     *
     * @return Returns the value of the outputJarVersion field.
     */
    public boolean isOutputJarVersion()
    {
        return this.outputJarVersion;
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
        return "JarResource[ groupId='" + this.groupId + "', artifactId='" + this.artifactId + "', version='" +
            this.version + "', classifier='" + this.classifier + "', mainClass='" + this.mainClass +
            "', outputJarVersion='" + this.outputJarVersion + "' ]";
    }
}
