package be.ordina.msdashboard.services;

import java.util.Map;

import be.ordina.msdashboard.aggregator.ObservablesToGraphConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import be.ordina.msdashboard.aggregator.DependenciesGraphResourceJsonBuilder;
import be.ordina.msdashboard.aggregator.DependenciesResourceJsonBuilder;
import be.ordina.msdashboard.model.Node;

public class DependenciesResourceService {

	private DependenciesResourceJsonBuilder dependenciesResourceJsonBuilder;
	private DependenciesGraphResourceJsonBuilder dependenciesGraphResourceJsonBuilder;
	private ObservablesToGraphConverter observablesToGraphConverter;

	public DependenciesResourceService(DependenciesResourceJsonBuilder dependenciesResourceJsonBuilder, DependenciesGraphResourceJsonBuilder dependenciesGraphResourceJsonBuilder, ObservablesToGraphConverter observablesToGraphConverter) {
		this.dependenciesResourceJsonBuilder = dependenciesResourceJsonBuilder;
		this.dependenciesGraphResourceJsonBuilder = dependenciesGraphResourceJsonBuilder;
		this.observablesToGraphConverter = observablesToGraphConverter;
	}

	public Node getDependenciesResourceJson() {
		return dependenciesResourceJsonBuilder.build();
	}

	public Map<String, Object> getDependenciesGraphResourceJson() {
		//return dependenciesGraphResourceJsonBuilder.build();
		return observablesToGraphConverter.build();
	}
}