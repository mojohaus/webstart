package org.codehaus.mojo.webstart;

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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Bean to host part of the JnlpMojo configuration.
 *
 * @author <a href="jerome@coffeebreaks.org">Jerome Lacoste</a>
 * @version $Id$
 */
public class JnlpConfig
{
    public static class Description
    {
        private String kind;

        private String text;

        public Description( String kind, String text )
        {
            this.kind = kind;
            this.text = text;
        }

        public String getKind()
        {
            return kind;
        }

        public String getText()
        {
            return text;
        }
    }

    public static class Icon
    {
        private String href;

        private String kind;

        private String width;

        private String height;

        private String depth;

        private String size;

        private String fileName; // not a parameter!!

        //private String version;
        public String getHref()
        {
            return href;
        }

        public void setHref( String href )
        {
            this.href = href;
        }

        public String getKind()
        {
            return kind;
        }

        public void setKind( String kind )
        {
            this.kind = kind;
        }

        public String getHeight()
        {
            return height;
        }

        public void setHeight( String height )
        {
            this.height = height;
        }

        public String getWidth()
        {
            return width;
        }

        public void setWidth( String width )
        {
            this.width = width;
        }

        public String getDepth()
        {
            return depth;
        }

        public void setDepth( String depth )
        {
            this.depth = depth;
        }

        public String getSize()
        {
            return size;
        }

        public void setSize( String size )
        {
            this.size = size;
        }

        public void setFileName( String fileName )
        {
            this.fileName = fileName;
        }

        public String getFileName()
        {
            return fileName;
        }
    }

    public static class Information
    {
        // os, arch, platform, locale
        private String os;

        private String title;

        private String vendor;

        private String onelineDescription;

        private String shortDescription;

        private String tooltipDescription;

        private String homepage;

        private Icon[] icons;

        private String offlineAllowed;

        public void setOs( String os )
        {
            this.os = os;
        }

        public void setTitle( String title )
        {
            this.title = title;
        }

        public void setVendor( String vendor )
        {
            this.vendor = vendor;
        }

        public void setOnelineDescription( String onelineDescription )
        {
            this.onelineDescription = onelineDescription;
        }

        public void setShortDescription( String shortDescription )
        {
            this.shortDescription = shortDescription;
        }

        public void setTooltipDescription( String tooltipDescription )
        {
            this.tooltipDescription = tooltipDescription;
        }

        public void setHomepage( String homepage )
        {
            this.homepage = homepage;
        }

        public void setIcons( Icon[] icons )
        {
            this.icons = icons;
        }

        public void setOfflineAllowed( String offlineAllowed )
        {
            this.offlineAllowed = offlineAllowed;
        }

        public String getOs()
        {
            return os;
        }

        public String getTitle()
        {
            return title;
        }

        public String getVendor()
        {
            return vendor;
        }

        public String getOnelineDescription()
        {
            return onelineDescription;
        }

        public String getShortDescription()
        {
            return shortDescription;
        }

        public String getTooltipDescription()
        {
            return tooltipDescription;
        }

        public String getHomepage()
        {
            return homepage;
        }

        public Icon[] getIcons()
        {
            return icons;
        }

        public String getOfflineAllowed()
        {
            return offlineAllowed;
        }

        public List getDescriptions()
        {
            List descriptions = new ArrayList();
            if ( onelineDescription != null )
            {
                descriptions.add( new Description( "one-line", onelineDescription ) );
            }
            if ( shortDescription != null )
            {
                descriptions.add( new Description( "short", shortDescription ) );
            }
            if ( tooltipDescription != null )
            {
                descriptions.add( new Description( "tooltip", tooltipDescription ) );
            }
            return Collections.unmodifiableList( descriptions );
            // return (Description[]) descriptions.toArray( new Description[ descriptions.size() ] );
        }
    }

    public static class AllResources
    {
        private J2SE[] j2ses;

        public J2SE[] getJ2ses()
        {
            return j2ses;
        }

        public void setJ2ses( J2SE[] j2ses )
        {
            this.j2ses = j2ses;
        }
    }

    public static interface Resources
    {
    }

    public static class J2SE
        implements Resources
    {
        private boolean autodownload;

        private String version;

        private String href;

        private String initialHeapSize;

        private String maxHeapSize;

        private String javaVMArgs;

        public boolean getAutodownload()
        {
            return autodownload;
        }

        public void setAutodownload( boolean autodownload )
        {
            this.autodownload= autodownload;
        }

        public String getVersion()
        {
            return version;
        }

        public void setVersion( String version )
        {
            this.version = version;
        }

        public String getHref()
        {
            return href;
        }

        public void setHref( String href )
        {
            this.href = href;
        }

        public String getInitialHeapSize()
        {
            return initialHeapSize;
        }

        public void setInitialHeapSize( String initialHeapSize )
        {
            this.initialHeapSize = initialHeapSize;
        }

        public String getMaxHeapSize()
        {
            return maxHeapSize;
        }

        public void setMaxHeapSize( String maxHeapSize )
        {
            this.maxHeapSize = maxHeapSize;
        }

        public String getJavaVMArgs()
        {
            return javaVMArgs;
        }

        public void setJavaVMArgs( String javaVMArgs )
        {
            this.javaVMArgs = javaVMArgs;
        }

    }

    private String outputFile;

    private String spec;

    private String version;

    private String codebase;

    private String href;

    private Information[] informations;

    private AllResources allResources;

    private String mainClass;

    private String[] arguments;

    private String security;


    public void setOutputFile( String outputFile )
    {
        this.outputFile = outputFile;
    }

    public void setSpec( String spec )
    {
        this.spec = spec;
    }

    public void setVersion( String version )
    {
        this.version = version;
    }

    public void setCodebase( String codebase )
    {
        this.codebase = codebase;
    }

    public void setHref( String href )
    {
        this.href = href;
    }

    public void setInformations( Information[] informations )
    {
        this.informations = informations;
    }

    public void setResources( AllResources allResources )
    {
        this.allResources = allResources;
    }

    public void setMainClass( String mainClass )
    {
        this.mainClass = mainClass;
    }

    public void setArguments( String[] arguments )
    {
        this.arguments = arguments;
    }

    public void setSecurity( String security )
    {
        this.security = security;
    }

    public String getOutputFile()
    {
        return outputFile;
    }

    public String getSpec()
    {
        return spec;
    }

    public String getVersion()
    {
        return version;
    }

    public String getCodebase()
    {
        return codebase;
    }

    public String getHref()
    {
        return href;
    }

    public Information[] getInformations()
    {
        return informations;
    }

    public AllResources getAllResources()
    {
        return allResources;
    }

    public String getMainClass()
    {
        return mainClass;
    }

    public String[] getArguments()
    {
        return arguments;
    }

    public String getSecurity()
    {
        return security;
    }

    public boolean isJ2EEClientPermissions()
    {
        return "j2ee".equals( security );
    }

    public boolean isAllPermissions()
    {
        return "all".equals( security );
    }
}
