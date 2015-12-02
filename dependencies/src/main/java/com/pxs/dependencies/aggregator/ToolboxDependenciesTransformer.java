package com.pxs.dependencies.aggregator;

import static org.springframework.boot.actuate.health.Health.status;

import java.util.Map;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Health.Builder;

import com.google.common.collect.Maps.EntryTransformer;

public class ToolboxDependenciesTransformer implements EntryTransformer<String, Object, Object> {

	private static final String TOOLBOX = "TOOLBOX";
	private static final String MICROSERVICE = "MICROSERVICE";
	private static final String DISCOVERY = "discovery";
	private static final String CONFIGSERVER = "configServer";

	@Override
	public Object transformEntry(final String key, final Object value) {
		if (DISCOVERY.equals(key) || CONFIGSERVER.equals(key)) {
			Health originalHealth = (Health) value;
			Map<String, Object> details = originalHealth.getDetails();
			Builder enrichedHealth = status(originalHealth.getStatus());
			for (String detailKey : details.keySet()) {
				enrichedHealth.withDetail(detailKey, details.get(detailKey));
			}
			enrichedHealth.withDetail("type", MICROSERVICE);
			enrichedHealth.withDetail("group", TOOLBOX);
			return enrichedHealth.build();
		}
		return value;
	}

}
