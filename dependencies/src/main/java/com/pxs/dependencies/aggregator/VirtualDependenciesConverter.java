package com.pxs.dependencies.aggregator;

import static com.pxs.dependencies.constants.Constants.MICROSERVICE;
import static com.pxs.dependencies.constants.Constants.OWN_HEALTH;
import static com.pxs.dependencies.constants.Constants.TYPE;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.actuate.health.Health;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.pxs.dependencies.model.Node;

@Component
public class VirtualDependenciesConverter {

	public Map<String, Map<String, Object>> convert(final Map<String, Node> virtualDependencies) {
		Map<String, Map<String, Object>> resultingDependencies = new HashMap<>();

		for (Map.Entry<String, Node> nodeEntry : virtualDependencies.entrySet()) {
			String virtualDependencyName = nodeEntry.getKey();
			Node virtualDependencyValue = nodeEntry.getValue();
			Map<String, Object> internalDependenciesMap = new HashMap<>();
			for (Node node : virtualDependencyValue.getLinkednodes()) {
				String id = node.getId();
				addHealth(id, internalDependenciesMap, node);
			}
			addHealth(OWN_HEALTH, internalDependenciesMap, virtualDependencyValue);
			resultingDependencies.put(virtualDependencyName, internalDependenciesMap);
		}
		return resultingDependencies;
	}

	private void addHealth(final String key, final Map<String, Object> backendTransformedMap, final Node microserviceValue) {
		Map<String, Object> details = microserviceValue.getDetails();
		Health.Builder builder = Health.unknown();
		for (Map.Entry<String, Object> entry : details.entrySet()) {
			builder.withDetail(entry.getKey(), entry.getValue());
		}
		backendTransformedMap.put(key, builder.build());
	}
}
