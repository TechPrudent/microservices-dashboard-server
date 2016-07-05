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

The graph exposing nodes and links is located under the following URL:

```
http://localhost:8383/graph
```

## Troubleshooting

For remote debugging, run the following command:

```bash
java -jar -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005 target/microservices-dashboard-server-0.1.0-SNAPSHOT.jar --spring.config.location=../microservices-dashboard-server-configuration/microservices-dashboard-server.yml
```

To enable Spring debug logging, add ```--debug``` to the command.

Make sure to use actuator endpoints such as ```/autoconfig``` and ```/beans``` for validating the right beans have been loaded.
