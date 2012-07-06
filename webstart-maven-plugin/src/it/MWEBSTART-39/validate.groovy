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

/*
Required result :

target/it/MWEBSTART-39/target/
├── from-classpath/keystore
├── from-file/keystore
└── from-url/keystore
 */

def assertExistsDirectory( file )
{
  if ( !file.exists() || !file.isDirectory() )
  {
    println( file.getAbsolutePath() + " file is missing or is not a directory." )
    return false
  }
  return true
}

def assertExistsFile( file )
{
  if ( !file.exists() || file.isDirectory() )
  {
    println( file.getAbsolutePath() + " file is missing or a directory." )
    return false
  }
  return true
}

File target = new File( basedir, "target" )
assert assertExistsDirectory( target )

File workdir1 = new File( target, "from-classpath" )
assert assertExistsDirectory( workdir1 )
assert assertExistsFile( new File( workdir1, "keystore" ) )
assert assertExistsFile( new File( workdir1, "launch.jnlp" ) )

File workdir2 = new File( new File( new File( basedir, "src" ), "main" ), "resources" )
assert assertExistsDirectory( workdir2 )
assert assertExistsFile( new File( workdir2, "keystore" ) )
File workdir2Bis = new File( target, "from-file" )
assert assertExistsDirectory( workdir2Bis )
assert assertExistsFile( new File( workdir2Bis, "launch.jnlp" ) )

File workdir3 = new File( target, "from-url" )
assert assertExistsDirectory( workdir3 )
assert assertExistsFile( new File( workdir3, "keystore" ) )
assert assertExistsFile( new File( workdir3, "launch.jnlp" ) )

return true
