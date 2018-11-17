package be.ordina.msdashboard;

import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * @author Tim Ysewyn
 * @author Dieter Hubau
 */
@Configuration
@EnableScheduling
public class MicroservicesDashboardServerAutoConfiguration {

	@Bean
	LandscapeWatcher landscapeWatcher(DiscoveryClient discoveryClient, ApplicationEventPublisher publisher) {
		return new LandscapeWatcher(discoveryClient, publisher);
	}

	@Bean WebClient webClient() {
		return WebClient.create();
	}
}
