package be.ordina.msdashboard.aggregator.health;

import be.ordina.msdashboard.LandscapeWatcher;
import be.ordina.msdashboard.aggregator.health.events.HealthInfoFailed;
import be.ordina.msdashboard.aggregator.health.events.HealthInfoRetrieved;
import be.ordina.msdashboard.events.NewServiceInstanceDiscovered;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

/**
 * Aggregator for the /health endpoint of a regular Spring Boot application.
 *
 * @author Dieter Hubau
 */
@Component
public class HealthAggregator {

	public static final Logger LOG = LoggerFactory.getLogger(HealthAggregator.class);

	private final WebClient webClient;
	private final LandscapeWatcher landscapeWatcher;
	private final ApplicationEventPublisher publisher;
	private final UriComponentsBuilder uriComponentsBuilder;

	public HealthAggregator(LandscapeWatcher landscapeWatcher, WebClient webClient,
		ApplicationEventPublisher publisher) {
		this.landscapeWatcher = landscapeWatcher;
		this.webClient = webClient;
		this.publisher = publisher;
		this.uriComponentsBuilder = UriComponentsBuilder.newInstance().path("/actuator/health");
	}

	@EventListener({ NewServiceInstanceDiscovered.class })
	public void handleApplicationInstanceEvent(NewServiceInstanceDiscovered event) {
		ServiceInstance serviceInstance = (ServiceInstance) event.getSource();
		checkHealthInformation(serviceInstance);
	}

	@Scheduled(fixedRateString = "${aggregator.health.rate}")
	public void aggregateHealthInformation() {
		LOG.debug("Aggregating [HEALTH] information");

		this.landscapeWatcher.getServiceInstances()
			.forEach((serviceId, instances) -> instances.forEach(this::checkHealthInformation));
	}

	private void checkHealthInformation(ServiceInstance instance) {
		URI uri = uriComponentsBuilder.uri(instance.getUri()).build().toUri();

		this.webClient.get().uri(uri).retrieve().bodyToMono(HealthInfo.class)
			.doOnError(exception -> {
				LOG.debug("Could not retrieve health information for [" + uri + "]");

				this.publisher.publishEvent(new HealthInfoFailed(instance));
			})
			.subscribe(healthInfo -> {
				LOG.debug("Found health information for service [{}]", instance.getServiceId());

				this.publisher.publishEvent(new HealthInfoRetrieved(instance, healthInfo));
			});
	}
}
