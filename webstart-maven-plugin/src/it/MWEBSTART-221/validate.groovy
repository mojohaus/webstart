/*
Required result :

target/it/MWEBSTART-221/target/
├── keystore
└── jnlp
    ├── commons-cli-commons-cli__V1.1.jar
    ├── org.swinglabs-jxlayer__V3.0.4.jar
    ├── jxlayer.jnlp
    └── test.jnlp
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

String[] expectedJnlpFiles = ["test.jnlp", "jxlayer.jnlp"]
String[] expectedJnlpLibFiles = ["commons-cli__V1.1.jar", "jxlayer__V3.0.4.jar"]


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
  assert assertExistsFile( new File( jnlpLib, it + ".pack" ) )
}

assert jnlpLib.list().length == ( expectedJnlpLibFiles.length * 2 ) + 2 // jar + pack files + jnlp files

File jnlpFile  = new File( jnlpDirectory, "test.jnlp" )
String jnlpContent1 = jnlpFile.text

assert assertContains( jnlpContent1,  "<jar href=\"commons-cli.jar\" version=\"1.1\" main=\"true\"/>" )
assert assertContains( jnlpContent1,  "<property name=\"jnlp.versionEnabled\" value=\"true\" />" )

File extensionFile  = new File( jnlpDirectory, "jxlayer.jnlp" )
String extensionContent1 = extensionFile .text

assert assertContains( extensionContent1,  "<jar href=\"jxlayer.jar\" version=\"3.0.4\"/>" )
assert assertContains( extensionContent1,  "<property name=\"jnlp.versionEnabled\" value=\"true\" />" )

return true