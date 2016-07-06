package be.ordina.simple;

import be.ordina.msdashboard.EnableMicroservicesDashboardServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableMicroservicesDashboardServer
public class MicroservicesDashboardSimpleApplication {

	public static void main(String[] args) {
		SpringApplication.run(MicroservicesDashboardSimpleApplication.class, args);
	}
}
