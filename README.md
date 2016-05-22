# webstart

[![Build Status](https://travis-ci.org/mojohaus/webstart.svg?branch=master)](https://travis-ci.org/mojohaus/webstart)
[![Maven Central](https://img.shields.io/maven-central/v/org.codehaus.mojo/webstart-maven-plugin.svg?label=Maven%20Central)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22org.codehaus.mojo%22%20a%3A%webstart-maven-plugin%22)
[![MIT Licence](https://img.shields.io/github/license/mojohaus/webstart.svg?label=License)](http://opensource.org/licenses/MIT)
[![Gitter](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/mojohaus/webstart?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

## Releasing

* Make sure `gpg-agent` is running.
* Execute `mvn -B release:prepare release:perform`

For publishing the site do the following:

```
cd target/checkout
mvn verify site site:stage scm-publish:publish-scm
