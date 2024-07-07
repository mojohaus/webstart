package org.codehaus.mojo.webstart.pack200;

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

import java.util.List;

/**
 * Bean that represents the Pack200 configuration.
 *
 * @author Peter Butkovic butkovic@gmail.com
 * @author $LastChangedBy$
 * @version $Revision$
 * @since 1.0-beta-4
 */
public class Pack200Config
{

    /**
     * Whether pack200 is enabled at all or not.
     *
     * @see #isEnabled()
     */
    private boolean enabled;

    /**
     * Whether the commons compress version of pack200 is enabled
     *
     * @see #isCommonsCompressEnabled()
     */
    private boolean commonsCompressEnabled;

    /**
     * The files to be passed without compression.
     *
     * @see #getPassFiles()
     */
    private List<String> passFiles;

    /**
     * Gets the pack200 enabled configuration value. <br>
     * Please note: Setting this value to true requires SDK 5.0 or greater.
     *
     * @return {@code true} if pack200 compression of jar resources is enabled, {@code false} otherwise.
     */
    public boolean isEnabled()
    {
        return enabled;
    }

    /**
     * Gets the files within jar archive to be passed without pack200 compression. <br>
     * If file ends with a /, all files in the directory are passed through without packing. <br>
     * The same functionality as achievable by:
     * <pre>
     * pack200 --pass-file= file, -P file
     * </pre>
     *
     * @return the files to be passed without pack200 compression.
     */
    public List<String> getPassFiles()
    {
        return passFiles;
    }

    public void setEnabled( boolean enabled )
    {
        this.enabled = enabled;

    }

    public void setPassFiles( List<String> passFiles )
    {
        this.passFiles = passFiles;
    }

	public boolean isCommonsCompressEnabled() {
		return commonsCompressEnabled;
	}

	public void setCommonsCompressEnabled(boolean commonsCompressEnabled) {
		this.commonsCompressEnabled = commonsCompressEnabled;
	}

}
