package org.codehaus.mojo.webstart;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.List;
import java.util.Iterator;

import org.apache.maven.project.MavenProject;
import org.apache.maven.reporting.AbstractMavenReport;
import org.apache.maven.reporting.MavenReportException;

import org.apache.maven.doxia.siterenderer.Renderer;
import org.codehaus.plexus.util.FileUtils;

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
     * Location where generated html will be created.
     *
     * @parameter expression="${project.build.directory}/site"
     */
    private File outputDirectory;

    /**
     * <i>Maven Internal</i>: The Doxia Site Renderer.
     *
     * @component
     * @required @readonly
     */
    private Renderer siteRenderer;

    /**
     * Maven Project
     *
     * @parameter expression="${project}"
     * @required @readonly
     */
    private MavenProject project;

    /**
     * Directory where the jnlp artifacts and jnlp sources files reside.
     *
     * @parameter expression="${project.build.directory}/jnlp"
     * @required
     */
    private File jnlpSourceDirectory;

    /**
     * Directory in the site directory where the jnlp artifacts and jnlp sources files reside.
     *
     * @parameter expression="jnlp/launch.jnlp"
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

    public void executeReport( Locale locale )
        throws MavenReportException
    {
        copyJnlpFiles();
        fillReport( locale );
    }

    private void copyJnlpFiles()
        throws MavenReportException
    {
        if ( ! jnlpSourceDirectory.exists() )
        {
            throw new MavenReportException( "jnlpSourceDirectory does not exist" );
        }
        try
        {
            File destinationDirectory = new File( outputDirectory, siteJnlpFile ).getParentFile();
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
        getSink().head();
        getSink().text( getBundle( locale ).getString( "report.jnlp-report.description" ) );
        getSink().head_();
        getSink().body();
        getSink().sectionTitle1();
        getSink().text( getBundle( locale ).getString( "report.jnlp-report.label.installation.header" ) );
        getSink().sectionTitle1_();
        getSink().paragraph();
        getSink().text( getBundle( locale ).getString( "report.jnlp-report.label.installation.description" ) );
        getSink().paragraph_();
        getSink().paragraph();
        getSink().link( siteJnlpFile );
        getSink().text( getBundle( locale ).getString( "report.jnlp-report.label.installation.webStartMeNow" ) );
        getSink().link_();
        getSink().paragraph_();
        getSink().paragraph();
        getSink().text( getBundle( locale ).getString( "report.jnlp-report.label.installation.getJava" ) + " " );
        getSink().link( "http://java.com" );
        getSink().text( "http://java.com" );
        getSink().link_();
        getSink().paragraph_();
        getSink().sectionTitle1();
        getSink().text( getBundle( locale ).getString( "report.jnlp-report.label.uninstallation.header" ) );
        getSink().sectionTitle1_();
        getSink().paragraph();
        getSink().text( getBundle( locale ).getString( "report.jnlp-report.label.uninstallation.description" ) );
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
