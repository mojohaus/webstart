package org.codehaus.mojo.webstart.test;

import org.apache.commons.io.IOUtils;
import java.net.URL;
import java.io.InputStream;

public class Test
{
  public static void main( String[] args )
    throws Exception
  {
     InputStream in = new URL( "http://jakarta.apache.org" ).openStream();
     try {
       System.out.println( IOUtils.toString( in ) );
     } finally {
       IOUtils.closeQuietly( in );
     }
  }
}
