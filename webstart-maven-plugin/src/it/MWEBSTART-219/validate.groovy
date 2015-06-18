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

def assertContains( content, expected )
{
  if ( !content.contains(expected) )
  {
      println( expected + " was not found in \n :" + content )
      return false
  }
  return true
}

File target = new File( basedir, "webstart/target" )
assert assertExistsDirectory( target )

String[] expectedFiles = [ "webapp.war" ]
expectedFiles.each{
 assert assertExistsFile( new File ( target, it ) )
}

File explodedWebstart = new File( target, "webapp/webstart" )
assert assertExistsDirectory( explodedWebstart )

expectedFiles = ["launch1.jnlp", "launch2.jnlp"]
expectedFiles.each{
 assert assertExistsFile( new File ( explodedWebstart, it ) )
}

File file = new File( explodedWebstart, "launch1.jnlp" )
String content = file.text

String[] expectedContains = [ "<jar href=\"lib/share-1.0-SNAPSHOT.jar\"/>", "<jar href=\"lib/client-1.0-SNAPSHOT.jar\" main=\"true\"/>" ]
expectedContains.each{
  assert assertContains( content,  it )
}

assert explodedWebstart.list().length == expectedFiles.length + 1

File explodedWebstartlib = new File( target, "webapp/webstart/lib" )
assert assertExistsDirectory( explodedWebstartlib )

expectedFiles = [ "client-1.0-SNAPSHOT.jar", "share-1.0-SNAPSHOT.jar", "version.xml" ]
expectedFiles.each{
 assert assertExistsFile( new File ( explodedWebstartlib, it ) )
}

file = new File( explodedWebstartlib, "version.xml" )
content = file.text

expectedContains = [ "<file>client-1.0-SNAPSHOT.jar</file>", "<file>share-1.0-SNAPSHOT.jar</file>" ]
expectedContains.each{
  assert assertContains( content,  it )
}

assert explodedWebstartlib.list().length == expectedFiles.length

return true
