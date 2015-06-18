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

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created on 1/4/14.
 *
 * @author Tony Chemit <chemit@codelutin.com>
 * @since 1.0-beta-5
 */
public class JnlpDependencyResults
{

    /**
     * Registred results.
     */
    private final Map<JnlpDependencyRequest, JnlpDependencyResult> results;

    public JnlpDependencyResults()
    {
        results = new LinkedHashMap<JnlpDependencyRequest, JnlpDependencyResult>();
    }

    public void registerResult( JnlpDependencyRequest request, JnlpDependencyResult result )
    {
        results.put( request, result );
    }

    public Map<JnlpDependencyRequest, JnlpDependencyResult> getResults()
    {
        return Collections.unmodifiableMap( results );
    }

    public boolean isError()
    {
        boolean result = false;

        for ( JnlpDependencyResult dependencyResult : results.values() )
        {
            if ( dependencyResult.isError() )
            {
                result = true;
                break;
            }
        }
        return result;
    }

    public JnlpDependencyResult[] getResultsWithError()
    {
        List<JnlpDependencyResult> resultWithErrors = new ArrayList<JnlpDependencyResult>();
        for ( JnlpDependencyResult dependencyResult : results.values() )
        {
            if ( dependencyResult.isError() )
            {
                resultWithErrors.add( dependencyResult );
            }
        }
        return resultWithErrors.toArray( new JnlpDependencyResult[resultWithErrors.size()] );
    }

    public int getNbRequestsProcessed()
    {
        int result = 0;
        for ( JnlpDependencyRequest request : results.keySet() )
        {
            if ( !request.isUptodate() )
            {
                result++;
            }
        }
        return result;
    }

    public int getNbRequestsUptodate()
    {
        int result = 0;
        for ( JnlpDependencyRequest request : results.keySet() )
        {
            if ( request.isUptodate() )
            {
                result++;
            }
        }
        return result;
    }
}
