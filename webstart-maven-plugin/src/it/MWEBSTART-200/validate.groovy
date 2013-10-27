
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

File target = new File( basedir, "target" )
assert assertExistsDirectory( target )

File jnlp = new File( target, "jnlp" )
assert assertExistsDirectory( jnlp )


File jnlpFile = new File( jnlp , "test.jnlp" )
assert assertExistsFile( jnlpFile )

String jnlpContent = jnlpFile.text

assert assertContains( jnlpContent,  "<property name=\"environment\" value=\"dev\"/>" )

return true;