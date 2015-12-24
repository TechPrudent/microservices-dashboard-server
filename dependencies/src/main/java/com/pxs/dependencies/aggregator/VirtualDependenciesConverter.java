package com.pxs.dependencies.aggregator;

import static com.pxs.dependencies.constants.Constants.MICROSERVICE;
import static com.pxs.dependencies.constants.Constants.OWN_HEALTH;
import static com.pxs.dependencies.constants.Constants.TYPE;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.actuate.health.Health;
import org.springframework.stereotype.Component;

import com.pxs.dependencies.model.Node;

@Component
public class VirtualDependenciesConverter {

	public Map<String, Map<String, Object>> convert(final Map<String, Node> virtualDependencies) {
		Map<String, Map<String, Object>> resultingDependencies = new HashMap<>();
		Map<String, Object> backendTransformedMap = new HashMap<>();
		for (Map.Entry<String, Node> nodeEntry : virtualDependencies.entrySet()) {
			String microserviceName = nodeEntry.getKey();
			Node microserviceValue = nodeEntry.getValue();
			if (MICROSERVICE.equals(microserviceValue.getDetails().get(TYPE))) {
				for (Node node : microserviceValue.getLinkednodes()) {
					String id = node.getId();
					Health health = Health.unknown().build();
					backendTransformedMap.put(id, health);
				}
				addOwnhealth(backendTransformedMap, microserviceValue);
				resultingDependencies.put(microserviceName, backendTransformedMap);
			}
		}
		return resultingDependencies;
	}

	private void addOwnhealth(final Map<String, Object> backendTransformedMap, final Node microserviceValue) {
		Map<String, Object> details = microserviceValue.getDetails();
		Health.Builder builder = Health.unknown();
		for (Map.Entry<String, Object> entry : details.entrySet()) {
			builder.withDetail(entry.getKey(), entry.getValue());
		}
		backendTransformedMap.put(OWN_HEALTH, builder.build());
	}
}
