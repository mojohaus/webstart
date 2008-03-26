Webstart Demo Application
=========================

This demo application shows a simple use case of the webstart maven plugin. It consists of three modules: a simple core-module, a HelloWorld-application and a simple web-application.

  * module core: This core-module contains only one class which returns "Hello World!"-string. It exists to demonstrate that jar-files of transitive dependencies are also provided by webstart.
  * module hello-world: The HelloWorld-application has one class and just prints "HelloWorld!" to the java console. It contains a dependency to module core.
  * module webapp: The web-application consists of only one page "index.jsp". 
  
The use-case is as follow: within the web-application the HelloWorld-application shall be provided using Java webstart and the JnlpDownloadServlet from Sun. Therefore, the webstart-maven-plugin is used/configured in the pom.xml of the web-application. 


How to use the webstart-demo?
=============================

 1. First, install the jnlp-servlet.jar from your *Sun* jdk-installation (>= 1.5) into your local maven repository using the following command:
 
    mvn install:install-file -Dfile=%JAVA_HOME%\sample\jnlp\servlet\jnlp-servlet.jar -DgroupId=com.sun.java.jnlp -DartifactId=jnlp-servlet -Dversion=5.0 -Dpackaging=jar -DgeneratePom=true
    
    (see also: http://mojo.codehaus.org/webstart-maven-plugin-parent/webstart-maven-plugin/jnlp101.html)

 2. Extract zip-file.
 3. Open console and change to the "webstartDemo"-directory (where this README is located).
 4. Enter "mvn clean install"
 5. Deploy the war-file created in /webstartDemo/webapp/target/ to your web-application-server (e.g. Apache Tomcat)
 6. Open browser and go to "http://localhost:8080/webapp" and click on the link "Start HelloWorld". Webstart downloads the jars and asks you to accept the certificate. If you activated the Java console in the Java control panel, you should see the output "Hello World!".


I you want to fully test the deployment into a jetty server in one step, replace the steps 4 to 6 above with

  4. Enter "mvn clean install -Dwith-jetty"
  5. Open browser and go to "http://localhost:8080/webapp" and click on the link "Start HelloWorld". Webstart downloads the jars and asks you to accept the certificate. If you activated the Java console in the Java control panel, you should see the output "Hello World!".
    Then CTRL-C in the terminal and the build should complete.

  You can change the port number by configuring the webapp/pom.xml appropriately

Some more details
=================

The web-application uses the goal "jnlp-download-servlet" of the WebstartMavenPlugin. Because <permission-all /> is requested in the jnlp-file, all jars must be signed. Therefore, a new keystore and key is generated in the target-directory and used to sign the hello-world.jar. The <unsign>-element in the pom.xml tells the plugin to unsign any jars that are already signed before signing them with our own key.
To use the JnlpDownloadServlet, the web.xml must be configured as such, that any requests made to the /webstart directory are forwarded to the jnlp-servlet. Therefore, the following is added to webapp/src/main/webapp/WEB-INF/web.xml:

  <servlet>
    <servlet-name>JnlpDownloadServlet</servlet-name>
    <servlet-class>jnlp.sample.servlet.JnlpDownloadServlet</servlet-class>
  </servlet>
    
  <servlet-mapping>
    <servlet-name>JnlpDownloadServlet</servlet-name>
    <url-pattern>/webstart/*</url-pattern>
  </servlet-mapping>
  
The directory mentioned in the <url-pattern>-element must be the same as defined in the <outputDirectoryName>-element in the pom.xml.
The WebstartMavenPlugin automatically generates a version.xml to use webstarts version-based download-protocol.


Links
=====

   * WebstartMavenPlugin: http://mojo.codehaus.org/webstart-maven-plugin-parent/webstart-maven-plugin/
   * Java Webstart Technology: http://java.sun.com/products/javawebstart/
   * Java Webstart Guide: http://java.sun.com/javase/6/docs/technotes/guides/javaws/developersguide/contents.html
   * Java Webstart FAQ: http://java.sun.com/javase/6/docs/technotes/guides/javaws/developersguide/faq.html
   * JNLP Specification: http://java.sun.com/products/javawebstart/download-spec.html
