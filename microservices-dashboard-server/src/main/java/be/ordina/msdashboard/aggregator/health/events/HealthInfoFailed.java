package be.ordina.msdashboard.aggregator.health.events;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.context.ApplicationEvent;

public class HealthInfoFailed extends ApplicationEvent {

	public HealthInfoFailed(ServiceInstance instance) {
		super(instance);
	}
}
