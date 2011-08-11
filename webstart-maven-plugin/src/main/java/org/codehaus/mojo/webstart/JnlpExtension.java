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

import java.util.List;

/**
 * Bean to host a jnlp extension configuration.
 *
 * @author <a href="chemit@codelutin.com">tony Chemit</a>
 * @version $Id$
 */
public class JnlpExtension
    extends JnlpConfig
{

    private List includes;

    private String name;

    private String title;

    private String vendor;

    private String homepage;

    private String description;

    public void setDescription( String description )
    {
        this.description = description;
    }

    public void setHomepage( String homepage )
    {
        this.homepage = homepage;
    }

    public void setIncludes( List includes )
    {
        this.includes = includes;
    }

    public void setTitle( String title )
    {
        this.title = title;
    }

    public void setVendor( String vendor )
    {
        this.vendor = vendor;
    }

    public void setName( String name )
    {
        this.name = name;
    }

    public String getDescription()
    {
        return description;
    }

    public String getHomepage()
    {
        return homepage;
    }

    public List getIncludes()
    {
        return includes;
    }

    public String getTitle()
    {
        return title;
    }

    public String getVendor()
    {
        return vendor;
    }

    public String getName()
    {
        return name;
    }

}
