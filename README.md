# Eureka! Clinical I2b2 Integration Webapp
[Georgia Clinical and Translational Science Alliance (Georgia CTSA)](http://www.georgiactsa.org), [Emory University](http://www.emory.edu), Atlanta, GA

## What does it do?
It implements a proxy servlet and router for web clients to access the web services provided by eurekaclinical-i2b2-integration-service.

## Version 2.0 development series
Latest release: [![Latest release](https://maven-badges.herokuapp.com/maven-central/org.eurekaclinical/eurekaclinical-i2b2-integration-webapp/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.eurekaclinical/eurekaclinical-i2b2-integration-webapp)

The 2.0 series will port the proxy over to the new [eurekaclinical-i2b2-integration-service](https://github.com/eurekaclinical/eurekaclinical-i2b2-integration-service).

## Version history
### Version 1.0.1
The version 1 series implements the proxy and router for all service functionality, but it assumes the old version of the service ([i2b2-eureka-service](https://github.com/eurekaclinical/i2b2-eureka)).

## Build requirements
* [Oracle Java JDK 8](http://www.oracle.com/technetwork/java/javase/overview/index.html)
* [Maven 3.2.5 or greater](https://maven.apache.org)

## Runtime requirements
* [Oracle Java JRE 8](http://www.oracle.com/technetwork/java/javase/overview/index.html)
* [Tomcat 7](https://tomcat.apache.org)
* Also running
  * The [eurekaclinical-i2b2-integration-service](https://github.com/eurekaclinical/eurekaclinical-i2b2-integration-service) war
  * The [cas-server](https://github.com/eurekaclinical/cas) war

## Proxied REST APIs
You can call all of [eurekaclinical-i2b2-integration-service](https://github.com/eurekaclinical/eurekaclinical-i2b2-integration-service)'s REST APIs through the proxy. Replace `/protected/api` with `/proxy-resource`. The point of doing this is for web clients -- you can deploy the webapp on the same server as web client, and deploy the service on a separate server.

## Building it
The project uses the maven build tool. Typically, you build it by invoking `mvn clean install` at the command line. For simple file changes, not additions or deletions, you can usually use `mvn install`. See https://github.com/eurekaclinical/dev-wiki/wiki/Building-Eureka!-Clinical-projects for more details.

## Performing system tests
You can run this project in an embedded tomcat by executing `mvn tomcat7:run -Ptomcat` after you have built it. It will be accessible in your web browser at https://localhost:8443/eurekaclinical-i2b2-integration-webapp/. Your username will be `superuser`.

## Installation
### Configuration
This webapp is configured using a properties file located at `/etc/ec-i2b2-integration/application.properties`. It supports the following properties:
* `eurekaclinical.i2b2integrationwebapp.callbackserver` = https://hostname:port
* `eurekaclinical.i2b2integrationservice.url` = https://hostname.of.service:port/eurekaclinical-i2b2-integration-service
* `eurekaclinical.i2b2integrationwebapp.url` = https://hostname:port/eurekaclinical-i2b2-integration-webapp
* `cas.url`=https://hostname.of.casserver:port/cas-server

A Tomcat restart is required to detect any changes to the configuration file.

### WAR installation
1) Stop Tomcat.
2) Remove any old copies of the unpacked war from Tomcat's webapps directory.
3) Copy the warfile into the Tomcat webapps directory, renaming it to remove the version. For example, rename `eurekaclinical-i2b2-integration-webapp-1.0.war` to `eurekaclinical-i2b2-integration-webapp.war`.
4) Start Tomcat.

## Maven dependency
```
<dependency>
    <groupId>org.eurekaclinical</groupId>
    <artifactId>eurekaclinical-i2b2-integration-webapp</artifactId>
    <version>version</version>
</dependency>
```

## Developer documentation
* [Javadoc for latest development release](http://javadoc.io/doc/org.eurekaclinical/eurekaclinical-i2b2-integration-webapp) [![Javadocs](http://javadoc.io/badge/org.eurekaclinical/eurekaclinical-i2b2-integration-webapp.svg)](http://javadoc.io/doc/org.eurekaclinical/eurekaclinical-i2b2-integration-webapp)

## Getting help
Feel free to contact us at help@eurekaclinical.org.

