# webstart

[![Build Status](https://github.com/mojohaus/webstart/workflows/GitHub%20CI/badge.svg?branch=master)](https://github.com/mojohaus/webstart/actions/workflows/maven.yml?query=branch%3Amaster)
[![Maven Central](https://img.shields.io/maven-central/v/org.codehaus.mojo/webstart-maven-plugin.svg?label=Maven%20Central)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22org.codehaus.mojo%22%20a%3A%webstart-maven-plugin%22)
[![MIT Licence](https://img.shields.io/github/license/mojohaus/webstart.svg?label=License)](http://opensource.org/licenses/MIT)

## Releasing

* Make sure `gpg-agent` is running.
* Execute `mvn -B release:prepare release:perform`

For publishing the site do the following:

```
cd target/checkout
mvn -Preporting verify site site:stage scm-publish:publish-scm
```
