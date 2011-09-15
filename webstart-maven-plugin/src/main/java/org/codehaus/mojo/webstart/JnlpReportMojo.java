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

import org.apache.maven.doxia.siterenderer.Renderer;
import org.apache.maven.project.MavenProject;
import org.apache.maven.reporting.AbstractMavenReport;
import org.apache.maven.reporting.MavenReportException;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Creates a JNLP report.
 *
 * @author Geoffrey De Smet
 * @description Creates a Jnlp Report
 * @goal report
 * @phase site
 */
public class JnlpReportMojo
    extends AbstractMavenReport
{
    /**
     * Location where the site is generated.
     *
     * @parameter default-value="${project.reporting.outputDirectory}"
     */
    private File outputDirectory;

    /**
     * <i>Maven Internal</i>: The Doxia Site Renderer.
     *
     * @component
     * @required
     * @readonly
     */
    private Renderer siteRenderer;

    /**
     * Maven Project
     *
     * @parameter default-value="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    /**
     * Directory where the jnlp artifacts and jnlp sources files reside.
     *
     * @parameter default-value="${project.build.directory}/jnlp"
     * @required
     */
    private File jnlpSourceDirectory;

    /**
     * Directory in the site directory where the jnlp artifacts and jnlp sources files reside.
     *
     * @parameter expression="${jnlp.siteJnlpDirectory}" default-value="jnlp"
     * @required
     */
    private String siteJnlpDirectory;

    /**
     * Name of the main jnlp file of the project.
     *
     * @parameter expression="${jnlp.siteJnlpFile}" default-value="launch.jnlp"
     * @required
     */
    private String siteJnlpFile;

    /**
     * The default filename to use for the report.
     *
     * @parameter expression="${outputName}" default-value="jnlp-report"
     * @required
     */
    private String outputName;

    /**
     * The code base to use on the generated jnlp files.
     *
     * @parameter expression="${jnlp.codebase}" default-value="${project.url}/jnlp"
     * @since 1.0-beta-2
     */
    private String codebase;

    public void executeReport( Locale locale )
        throws MavenReportException
    {
        copyJnlpFiles();
        fillReport( locale );
    }

    private void copyJnlpFiles()
        throws MavenReportException
    {
        if ( !jnlpSourceDirectory.exists() )
        {
            throw new MavenReportException( "jnlpSourceDirectory does not exist" );
        }
        try
        {
            File destinationDirectory = new File( outputDirectory, siteJnlpDirectory );
            List files = FileUtils.getFiles( jnlpSourceDirectory, "**/*", "" );
            for ( Iterator i = files.iterator(); i.hasNext(); )
            {
                File file = (File) i.next();
                getLog().debug( "Copying " + file + " to " + destinationDirectory );
                String path = file.getAbsolutePath().substring( jnlpSourceDirectory.getAbsolutePath().length() + 1 );
                File destDir = new File( destinationDirectory, path );
                getLog().debug( "Copying " + file + " to " + destDir );
                if ( file.isDirectory() )
                {
                    destDir.mkdirs();
                }
                else
                {
                    FileUtils.copyFileToDirectory( file, destDir.getParentFile() );
                }
            }
        }
        catch ( IOException e )
        {
            throw new MavenReportException( "Failed to copy jnlp files", e );
        }
    }

    private void fillReport( Locale locale )
    {
        ResourceBundle bundle = getBundle( locale );
        getSink().head();
        getSink().text( bundle.getString( "report.jnlp-report.description" ) );
        getSink().head_();
        getSink().body();
        getSink().sectionTitle1();
        getSink().text( bundle.getString( "report.jnlp-report.label.installation.header" ) );
        getSink().sectionTitle1_();
        getSink().paragraph();
        getSink().text( bundle.getString( "report.jnlp-report.label.installation.description" ) );
        getSink().paragraph_();
        getSink().paragraph();
        if ( codebase.startsWith( "file://" ) )
        {
            if ( !codebase.endsWith( File.separator ) )
            {
                codebase += File.separator;
            }
        }
        else
        {
            if ( !codebase.endsWith( "/" ) )
            {
                codebase += "/";
            }
        }
        getSink().link( codebase + siteJnlpFile );

        getSink().text( bundle.getString( "report.jnlp-report.label.installation.webStartMeNow" ) );
        getSink().link_();
        getSink().paragraph_();
        getSink().paragraph();
        getSink().text( bundle.getString( "report.jnlp-report.label.installation.getJava" ) + " " );
        getSink().link( "http://java.com" );
        getSink().text( "http://java.com" );
        getSink().link_();
        getSink().paragraph_();
        getSink().sectionTitle1();
        getSink().text( bundle.getString( "report.jnlp-report.label.uninstallation.header" ) );
        getSink().sectionTitle1_();
        getSink().paragraph();
        getSink().text( bundle.getString( "report.jnlp-report.label.uninstallation.description" ) );
        getSink().paragraph_();
        getSink().body_();
        getSink().flush();
        getSink().close();
    }

    public String getName( Locale locale )
    {
        return getBundle( locale ).getString( "report.jnlp-report.name" );
    }

    public String getDescription( Locale locale )
    {
        return getBundle( locale ).getString( "report.jnlp-report.description" );
    }

    protected Renderer getSiteRenderer()
    {
        return siteRenderer;
    }

    protected MavenProject getProject()
    {
        return project;
    }

    public String getOutputName()
    {
        return outputName;
    }

    protected String getOutputDirectory()
    {
        return outputDirectory.getPath();
    }

    private ResourceBundle getBundle( Locale locale )
    {
        return ResourceBundle.getBundle( "jnlp-report", locale, this.getClass().getClassLoader() );
    }

}
