package org.codehaus.mojo.webstart.dependency;

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

/**
 * Created on 1/7/14.
 *
 * @author Tony Chemit <chemit@codelutin.com>
 * @since 1.0-beta-5
 */
public class JnlpDependencyRequestConsumerConfig
{

    private boolean verbose;

    private int maxThreads;

    private boolean failFast;

    public int getMaxThreads()
    {
        return maxThreads;
    }

    public void setMaxThreads( int maxThreads )
    {
        this.maxThreads = maxThreads;
    }

    public boolean isFailFast()
    {
        return failFast;
    }

    public void setFailFast( boolean failFast )
    {
        this.failFast = failFast;
    }

    public boolean isVerbose()
    {
        return verbose;
    }

    public void setVerbose( boolean verbose )
    {
        this.verbose = verbose;
    }
}
