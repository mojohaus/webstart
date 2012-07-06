Test the feature http://jira.codehaus.org/browse/MWEBSTART-39

Creating a keystore file.
If validity is out, use the following cli to regenerate a keystore

rm src/main/resources/keystore
keytool -genkeypair -alias foo_alias -keystore src/main/resources/keystore -keypass key-passwd -validity 999 -storepass changeit -dname "CN=Me, OU=Unknown, O=Codehaus, L=Unknown, ST=Unknown, C=France"


