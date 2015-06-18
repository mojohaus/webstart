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

File target = new File( basedir, "target" )
assert assertExistsDirectory( target )

File jnlp = new File( target, "jnlp" )
assert assertExistsDirectory( jnlp )

String[] expectedFiles = [ "webstart-it004-1.0.zip" ]
expectedFiles.each{
 assert assertExistsFile( new File ( target, it ) )
}

String[] expectedJnlpFiles = [ "commons-cli-1.1.jar", "commons-logging-1.0.4.jar", "test.jnlp" ]
expectedJnlpFiles.each{
 assert assertExistsFile( new File ( jnlp, it ) )
}

assert jnlp.list().length == expectedJnlpFiles.length + 1 // images

// validate images
File jnlpImages = new File( jnlp, "images" )
assert assertExistsDirectory( jnlpImages )

String[] expectedJnlpImages = [ "icon.gif", "icon.jpg", "icon.png" ]
expectedJnlpImages.each{
 assert assertExistsFile( new File ( jnlpImages, it ) )
}

assert jnlpImages.list().length == expectedJnlpImages.length

return true
