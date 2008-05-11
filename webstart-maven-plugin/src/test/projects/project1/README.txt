run mvn org.codehaus.mojo.webstart:webstart-maven-plugin:jnlp-inline

or update

<settings>
...
<pluginGroups>
<pluginGroup>org.codehaus.mojo.webstart</pluginGroup>
</pluginGroups>
<settings>

and run mvn webstart:jnlp-inline
