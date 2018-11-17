package be.ordina.msdashboard.aggregator.health.events;

import be.ordina.msdashboard.aggregator.health.HealthInfo;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.context.ApplicationEvent;

public class HealthInfoRetrieved extends ApplicationEvent {

	private HealthInfo healthInfo;

	public HealthInfoRetrieved(ServiceInstance instance, HealthInfo healthInfo) {
		super(instance);
		this.healthInfo = healthInfo;
	}

	public HealthInfo getHealthInfo() {
		return healthInfo;
	}
}
