package be.ordina.simplemocks;

import be.ordina.msdashboard.EnableMicroservicesDashboardServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "be.ordina.simplemocks")
@EnableMicroservicesDashboardServer
public class MicroservicesDashboardSimpleMocksApplication {

	public static void main(String[] args) {
		SpringApplication.run(MicroservicesDashboardSimpleMocksApplication.class, args);
	}
}
