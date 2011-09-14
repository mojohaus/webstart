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

target/it/MWEBSTART-159/target/
├── keystore
├── withLibPath
│   └── jnlp
│       ├── myPath
│       │   ├── commons-cli-1.1.jar
│       │   └── commons-cli-1.1.jar.pack
│       └── test.jnlp
└── withoutLibPath
    └── jnlp
        ├── commons-cli-1.1.jar
        ├── commons-cli-1.1.jar.pack
        └── test.jnlp
 */
def assertExistsDirectory( file )
{
  if ( !file.exists() || ! file.isDirectory() )
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

String[] expectedJnlpFiles = [ "test.jnlp" ]
String[] expectedJnlpLibFiles = [ "commons-cli-1.1.jar" ]


File target = new File( basedir, "target" )
assert assertExistsDirectory( target )

File workdir  = new File( target, "withoutLibPath")

File jnlp = new File( workdir, "jnlp" )
assert assertExistsDirectory( jnlp )

expectedJnlpFiles.each{
 assert assertExistsFile( new File ( jnlp, it ) )
}

File jnlpLib = jnlp;

expectedJnlpLibFiles.each{
 assert assertExistsFile( new File ( jnlpLib, it ) )
 assert assertExistsFile( new File ( jnlpLib, it + ".pack" ) )
}

assert jnlpLib.list().length == ( expectedJnlpLibFiles.length * 2 ) +1 // jar + pack files + jnlp file

workdir = new File( target, "withLibPath")

jnlp = new File( workdir, "jnlp" )
assert assertExistsDirectory( jnlp )

jnlpLib = new File(jnlp, "myPath")
assert assertExistsDirectory( jnlpLib )

expectedJnlpFiles.each{
 assert assertExistsFile( new File ( jnlp, it ) )
}

expectedJnlpLibFiles.each{
 assert assertExistsFile( new File ( jnlpLib, it ) )
 assert assertExistsFile( new File ( jnlpLib, it + ".pack" ) )
}

assert jnlpLib.list().length == ( expectedJnlpLibFiles.length * 2 ) // jar + pack files

return true
