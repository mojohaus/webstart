/*
Required result :

target/it/MWEBSTART-221/target/
├── keystore
└── jnlp
    ├── commons-cli__V1.1.jar
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

String[] expectedJnlpFiles = ["test.jnlp"]
String[] expectedJnlpLibFiles = ["commons-cli__V1.1.jar"]


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

assert jnlpLib.list().length == ( expectedJnlpLibFiles.length * 2 ) + 1 // jar + pack files + jnlp file

File jnlpFile  = new File( jnlpDirectory, "test.jnlp" )
String jnlpContent1 = jnlpFile.text

assert assertContains( jnlpContent1,  "<jar href=\"commons-cli.jar\" version=\"1.1\" main=\"true\"/>" )
assert assertContains( jnlpContent1,  "<property name=\"jnlp.versionEnabled\" value=\"true\" />" )

return true