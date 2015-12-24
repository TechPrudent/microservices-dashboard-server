package com.pxs.dependencies.aggregator;

import static org.springframework.boot.actuate.health.Health.status;

import static com.pxs.dependencies.constants.Constants.CONFIGSERVER;
import static com.pxs.dependencies.constants.Constants.DISCOVERY;
import static com.pxs.dependencies.constants.Constants.GROUP;
import static com.pxs.dependencies.constants.Constants.MICROSERVICE;
import static com.pxs.dependencies.constants.Constants.TOOLBOX;
import static com.pxs.dependencies.constants.Constants.TYPE;

import java.util.Map;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Health.Builder;

import com.google.common.collect.Maps.EntryTransformer;

public class ToolboxDependenciesTransformer implements EntryTransformer<String, Object, Object> {

	@Override
	public Object transformEntry(final String key, final Object value) {
		if (DISCOVERY.equals(key) || CONFIGSERVER.equals(key)) {
			Health originalHealth = (Health) value;
			Map<String, Object> details = originalHealth.getDetails();
			Builder enrichedHealth = status(originalHealth.getStatus());
			for (String detailKey : details.keySet()) {
				enrichedHealth.withDetail(detailKey, details.get(detailKey));
			}
			enrichedHealth.withDetail(TYPE, MICROSERVICE);
			enrichedHealth.withDetail(GROUP, TOOLBOX);
			return enrichedHealth.build();
		}
		return value;
	}

}
