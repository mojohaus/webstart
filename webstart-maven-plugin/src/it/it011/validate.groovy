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

def assertNotContains( content, expected )
{
  if ( content.contains(expected) )
  {
      println( expected + " should not be found in \n :" + content )
      return false
  }
  return true
}

File target = new File( basedir, "target" )
assert assertExistsDirectory( target )

File jnlp = new File( target, "jnlp" )
assert assertExistsDirectory( jnlp )

File jnlpFile = new File( jnlp, "test.jnlp" )
assert assertExistsFile( jnlpFile )

File site = new File( target, "site" )
assert assertExistsDirectory( site )

File siteJnlp = new File( site, "jnlp" )
assert assertExistsDirectory( jnlp )

File siteJnplFile = new File( siteJnlp, "test.jnlp" )
assert assertExistsFile( siteJnplFile )


//String[] expectedFiles = [ "webstart-it011-1.0.zip" ]
//expectedFiles.each{
// assert assertExistsFile( new File ( target, it ) )
//}

String[] expectedJnlpFiles = [ "commons-cli-1.1.jar", "commons-logging-1.1.1.jar", "javahelp-2.0.02.jar", "jxlayer-3.0.4.jar",  "jxlayer.jnlp","sun.jnlp", "test.jnlp" ]
String[] expectedJnlpContains = [ "<jar href=\"commons-logging-1.1.1.jar\"/>", "<jar href=\"commons-cli-1.1.jar\" main=\"true\"/>",  "<extension name=\"sun\" href=\"sun.jnlp\"/>", "<extension name=\"jxlayer\" href=\"jxlayer.jnlp\"/>" ]
String[] notExpectedJnlpContains = [ "<jar href=\"javahelp-2.0.02.jar\"/>", "<jar href=\"jxlayer-3.0.4.jar\"/>" ]


expectedJnlpFiles.each{
 assert assertExistsFile( new File ( jnlp, it ) )
}
expectedJnlpFiles.each{
 assert assertExistsFile( new File ( siteJnlp, it ) )
}

String jnlpContent = jnlpFile.text

expectedJnlpContains.each{
  assert assertContains( jnlpContent,  it )
}

notExpectedJnlpContains.each{
  assert assertNotContains( jnlpContent,  it )
}

jnlpContent = siteJnplFile.text

expectedJnlpContains.each{
  assert assertContains( jnlpContent,  it )
}

notExpectedJnlpContains.each{
  assert assertNotContains( jnlpContent,  it )
}

return true
