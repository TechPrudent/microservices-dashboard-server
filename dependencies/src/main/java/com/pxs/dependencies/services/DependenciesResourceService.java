package com.pxs.dependencies.services;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.pxs.dependencies.aggregator.DependenciesGraphResourceJsonBuilder;
import com.pxs.dependencies.aggregator.DependenciesResourceJsonBuilder;
import com.pxs.dependencies.model.Node;

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