package be.ordina.msdashboard.services;

import be.ordina.msdashboard.aggregator.ObservablesToGraphConverter;

import java.util.Map;

/**
 * @author Andreas Evers
 */
public class DependenciesResourceService {

	private ObservablesToGraphConverter observablesToGraphConverter;

	public DependenciesResourceService(ObservablesToGraphConverter observablesToGraphConverter) {
		this.observablesToGraphConverter = observablesToGraphConverter;
	}

	public Map<String, Object> getDependenciesGraphResourceJson() {
		return observablesToGraphConverter.build();
	}
}