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

target/it/MWEBSTART-11/target/
├── keystore
└── jnlp
    ├── commons-cli-commons-1.1.jar
    ├── MWEBSTART-11-1.0.jar
    ├── test-application.jnlp
    ├── test-applet.jnlp
    ├── test-component.jnlp
    └── test-installer.jnlp
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

def assertContains( content, expected )
{
  if ( !content.contains(expected) )
  {
    println( expected + " was not found in \n :" + content )
    return false
  }
  return true
}

String[] expectedJnlpFiles = ["test-application.jnlp", "test-component.jnlp", "test-installer.jnlp"]
String[] expectedJnlpLibFiles = ["commons-cli__V1.1.jar", "MWEBSTART-11__V1.0.jar"]

File target = new File( basedir, "target" )
assert assertExistsDirectory( target )

File jnlpDirectory = new File( target, "jnlp" )
assert assertExistsDirectory( jnlpDirectory )

expectedJnlpFiles.each {
  assert assertExistsFile( new File( jnlpDirectory, it ) )
}

File jnlpLib = jnlpDirectory;

expectedJnlpLibFiles.each {
  assert assertExistsFile( new File( jnlpLib, it ) )
}

assert jnlpLib.list().length ==  expectedJnlpLibFiles.length + expectedJnlpFiles.length // jar + jnlp files

File applicationJnlpFile  = new File( jnlpDirectory, "test-application.jnlp" )
assert assertContains( applicationJnlpFile.text,  "<jar href=\"commons-cli.jar\" version=\"1.1\"/>" )
assert assertContains( applicationJnlpFile.text,  "<jar href=\"MWEBSTART-11.jar\" version=\"1.0\" main=\"true\"/>" )
assert assertContains( applicationJnlpFile.text,  "<application-desc main-class=\"org.codehaus.mojo.webstart.Main\"/>" )

File componentJnlpFile  = new File( jnlpDirectory, "test-component.jnlp" )
assert assertContains( componentJnlpFile .text,  "<jar href=\"commons-cli.jar\" version=\"1.1\"/>" )
assert assertContains( componentJnlpFile.text,  "<jar href=\"MWEBSTART-11.jar\" version=\"1.0\"/>" )
assert assertContains( componentJnlpFile .text,  "<component-desc/>" )

File installerJnlpFile  = new File( jnlpDirectory, "test-installer.jnlp" )
assert assertContains( installerJnlpFile .text,  "<jar href=\"commons-cli.jar\" version=\"1.1\"/>" )
assert assertContains( installerJnlpFile.text,  "<jar href=\"MWEBSTART-11.jar\" version=\"1.0\"/>" )
assert assertContains( installerJnlpFile .text,  "<installer-desc/>" )


return true