# microservices-dashboard-server

## Overview

The primary goal of this project is to provide a server implementation for the microservices-dashboard GUI project.
This implementation is for now only supporting Spring Boot microservices.
It will query other Spring Boot applications for their actuator endpoints (such as ```/health```) to get information on their status and their dependencies.
After gathering these details from all available applications, it will aggregate these into a single response.
This response can be queried by the microservices-dashboard GUI application.

## Building from source

Microservices-dashboard-server requires Java 7 or later and is built using maven:

```
./mvn install
```

## Running locally

To run this application locally, build from source and run the following command:

```
./java -jar target/microservices-dashboard-server-0.1.0-SNAPSHOT.jar --spring.config.location=../microservices-dashboard-server-configuration/microservices-dashboard-server.yml
```
