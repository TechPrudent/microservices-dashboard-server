# Samples

Currently two working samples are available: simple and simple-mocks.

## Sample 1: microservices-dashboard-simple
The simple sample is a great way to get up and running quickly without any mocks.
You can use this as a base for your own version of the microservices-dashboard.

## Sample 2: microservices-dashboard-simple-mocks
This sample builds on top of the simple sample, with mocks enabled.

## Setup

The first step is to clone the Git repository:

```bash
git clone https://github.com/ordina-jworks/microservices-dashboard-server
```

Once the clone is complete, you’re ready to get the service up and running:

```bash
cd samples/microservices-dashboard-simple (or -simple-mocks)
./mvnw install
java -jar target/*.jar
```

If successful, you should see the following output in the log:

> o.s.b.c.e.t.TomcatEmbeddedServletContainer Tomcat started on port(s): 8080 (http)

From there on, you can visit actuator endpoints to validate the server's status such as ```http://localhost:8080/env``` (to see the environment variables), and ```http://localhost:8080/mappings``` (for all the available mappings).

## Loading the UI

The UI is located under the root of the webserver:

```
http://localhost:8080/
```

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

Make sure to use actuator endpoints such as ```/autoconfig``` and ```/beans``` for validating the right beans have been loaded.
