[ ![Codeship Status for ordina-jworks/microservices-dashboard-server](https://codeship.com/projects/29bfd6e0-de37-0133-bed6-5e9acf2db2e6/status?branch=master)](https://codeship.com/projects/144644)

[![codecov](https://codecov.io/gh/ordina-jworks/microservices-dashboard-server/branch/master/graph/badge.svg)](https://codecov.io/gh/ordina-jworks/microservices-dashboard-server)

# microservices-dashboard-server

## Overview

The primary goal of this project is to provide a server implementation for the microservices-dashboard GUI project.
This implementation is for now only supporting Spring Boot microservices.
It will query other Spring Boot applications for their actuator endpoints (such as ```/health```) to get information on their status and their dependencies.
After gathering these details from all available applications, it will aggregate these into a single response.
This response can be queried by the microservices-dashboard GUI application.

## Setting up the server

There are two ways of getting up and running with the microservices-dashboard-server.
Either by creating a new Spring Boot application and enhancing it with our dependency and annotation, or by using the sample project.

### Using a vanilla Spring Boot application

First you need to setup your server. To do this just setup a simple boot project (using [start.spring.io](start.spring.io) for example).

Build the microservices-dashboard-server project from source (see below), and add the artefact as a dependency to your new Spring Boot's dependencies:

```xml
<dependency>
	<groupId>be.ordina</groupId>
	<artifactId>microservices-dashboard-server</artifactId>
	<version>x.y.z</version>
</dependency>
```

Pull in the Microservices Dashboard Server configuration via adding `@EnableMicroservicesDashboardServer` to your configuration:

```java
@Configuration
@EnableAutoConfiguration
@EnableMicroservicesDashboardServer
public class MicroservicesDashboardServerApplication {
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
}
```

### Using the microservices-dashboard-server-sample project

Simply build the sample project from source (see below) and run it (also see below).

## Building from source

Microservices-dashboard-server requires Java 8 or later and is built using maven:

```bash
mvn install
```

## Building the sample from source

```bash
cd ./microservices-dashboard-sample
mvn install
```

## Running the sample locally

To run the sample application locally, build it from source and run the following command:

```bash
java -jar target/microservices-dashboard-server-sample-1.0.0-SNAPSHOT.jar --spring.config.location=./microservices-dashboard-server-configuration/microservices-dashboard-server.yml
```

If successful, you should see the following output in the log:

> o.s.b.c.e.t.TomcatEmbeddedServletContainer Tomcat started on port(s): 8383 (http)

From there on, you can visit actuator endpoints to validate the server's status such as ```http://localhost:8383/env``` (to see the environment variables), and ```http://localhost:8383/mappings``` (for all the available mappings).

## Available endpoints

For now, two major functional resources are exposed:

- ```http://localhost:8383/graph```
- ```http://localhost:8383/table```

## Troubleshooting

For remote debugging, run the following command:

```bash
java -jar -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005 target/microservices-dashboard-server-0.1.0-SNAPSHOT.jar --spring.config.location=../microservices-dashboard-server-configuration/microservices-dashboard-server.yml
```

To enable Spring debug logging, add ```--debug``` to the command.

Make sure to use actuator endpoints such as ```/autoconfig``` and ```/beans``` for validating the right beans have been loaded.
