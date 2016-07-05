# microservices-dashboard-server
[![Gitter](https://badges.gitter.im/ordina-jworks/microservices-dashboard.svg)](https://gitter.im/ordina-jworks/microservices-dashboard?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge)
[ ![Codeship Status for ordina-jworks/microservices-dashboard-server](https://codeship.com/projects/29bfd6e0-de37-0133-bed6-5e9acf2db2e6/status?branch=master)](https://codeship.com/projects/144644)
[![codecov](https://codecov.io/gh/ordina-jworks/microservices-dashboard-server/branch/master/graph/badge.svg)](https://codecov.io/gh/ordina-jworks/microservices-dashboard-server)
[![][license img]][license]
[![Average time to resolve an issue](http://isitmaintained.com/badge/resolution/ordina-jworks/microservices-dashboard-server.svg)](http://isitmaintained.com/project/ordina-jworks/microservices-dashboard-server "Average time to resolve an issue")
[![Percentage of issues still open](http://isitmaintained.com/badge/open/ordina-jworks/microservices-dashboard-server.svg)](http://isitmaintained.com/project/ordina-jworks/microservices-dashboard-server "Percentage of issues still open")
[ ![Download](https://api.bintray.com/packages/ordina-jworks/microservices-dashboard-server/microservices-dashboard-server/images/download.svg) ](https://bintray.com/ordina-jworks/microservices-dashboard-server/microservices-dashboard-server/_latestVersion)

## Overview

The primary goal of this project is to provide a server implementation for the microservices-dashboard GUI project.
This implementation is for now only supporting Spring Boot microservices.
It will query other Spring Boot applications for their actuator endpoints (such as ```/health```) to get information on their status and their dependencies.
After gathering these details from all available applications, it will aggregate these into a single response.
This response can be queried by the microservices-dashboard UI application.

## Setting up the server

There are two ways of getting up and running with the microservices-dashboard-server.
Either by creating a new Spring Boot application and enhancing it with our dependency and annotation, or by using the sample project.

### Using a vanilla Spring Boot application

First you need to setup a simple Spring Boot project (using [start.spring.io](http://start.spring.io) for example).
Microservices-dashboard-server requires Java 8 or later.

Add the microservices-dashboard-server as a dependency to your new Spring Boot's dependencies:

```xml
<dependency>
	<groupId>be.ordina</groupId>
	<artifactId>microservices-dashboard-server</artifactId>
	<version>x.y.z</version>
</dependency>
```

In case you use a `SNAPSHOT` version, add the JFrog OSS Artifactory repository:

```xml
<repositories>
	<repository>
		<id>oss-snapshots</id>
		<name>JFrog OSS Snapshots</name>
		<url>https://oss.jfrog.org/simple/oss-snapshot-local/</url>
		<snapshots>
			<enabled>true</enabled>
		</snapshots>
	</repository>
</repositories>
```

Pull in the Microservices Dashboard Server configuration via adding `@EnableMicroservicesDashboardServer` to your configuration:

```java
@SpringBootApplication
@EnableMicroservicesDashboardServer
public class MicroservicesDashboardServerApplication {
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
}
```

### Using a sample

See the sample documentation located here: https://github.com/ordina-jworks/microservices-dashboard-server/tree/master/samples

## Building from source

Microservices-dashboard-server requires Java 8 or later and is built using maven:

```bash
mvn install
```

If successful, you should see the following output in the log:

> o.s.b.c.e.t.TomcatEmbeddedServletContainer Tomcat started on port(s): 8080 (http)

From there on, you can visit actuator endpoints to validate the server's status such as ```http://localhost:8080/env``` (to see the environment variables), and ```http://localhost:8080/mappings``` (for all the available mappings).

## Available endpoints

The graph exposing nodes and links is located under the following URL:

```
http://localhost:8080/graph
```

## Troubleshooting

For remote debugging, run the following command:

```bash
java -jar -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005 target/*.jar
```

To enable Spring debug logging, add ```--debug``` to the command.

You can enable debug logging of the dashboard as well, by adding a ```logback.xml``` file under ```src/main/resources``` with the following contents:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>

    <include resource="org/springframework/boot/logging/logback/base.xml"/>
    <logger name="be.ordina.msdashboard" level="DEBUG"/>
    <logger name="org.springframework.cloud.netflix" level="DEBUG"/>
    
</configuration>
```

Make sure to use actuator endpoints such as ```/autoconfig``` and ```/beans``` for validating the right beans have been loaded.
More information on actuator endpoints can be found here: http://docs.spring.io/spring-boot/docs/current-SNAPSHOT/reference/htmlsingle/#production-ready

[license]:LICENSE-2.0.txt
[license img]:https://img.shields.io/badge/License-Apache%202-blue.svg
