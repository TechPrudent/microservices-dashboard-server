package be.ordina.msdashboard.aggregator;

import be.ordina.msdashboard.aggregator.health.HealthIndicatorsAggregator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import be.ordina.msdashboard.model.Node;

@Component
public class DependenciesResourceJsonBuilder {

	@Autowired
	private HealthIndicatorsAggregator healthIndicatorsAggregator;

	public Node build() {
		return healthIndicatorsAggregator.fetchCombinedDependencies();
	}
}
