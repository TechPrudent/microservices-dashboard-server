package com.pxs.dependencies.aggregator;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DependenciesResourceJsonBuilder {

	@Autowired
	private HealthIndicatorsAggregator healthIndicatorsAggregator;

	public Map<String, Map<String, Object>> build() {
		return healthIndicatorsAggregator.fetchCombinedDependencies();
	}
}
