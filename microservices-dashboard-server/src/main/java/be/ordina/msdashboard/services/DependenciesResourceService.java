package be.ordina.msdashboard.services;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import be.ordina.msdashboard.aggregator.DependenciesGraphResourceJsonBuilder;
import be.ordina.msdashboard.aggregator.DependenciesResourceJsonBuilder;
import be.ordina.msdashboard.model.Node;

@Service
public class DependenciesResourceService {

	@Autowired
	private DependenciesResourceJsonBuilder dependenciesResourceJsonBuilder;

	@Autowired

	private DependenciesGraphResourceJsonBuilder dependenciesGraphResourceJsonBuilder;

	public Node getDependenciesResourceJson() {
		return dependenciesResourceJsonBuilder.build();
	}

	public Map<String, Object> getDependenciesGraphResourceJson() {
		return dependenciesGraphResourceJsonBuilder.build();
	}
}