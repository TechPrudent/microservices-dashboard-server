package com.pxs.dependencies.services;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.pxs.dependencies.aggregator.DependenciesGraphResourceJsonBuilder;
import com.pxs.dependencies.aggregator.DependenciesResourceJsonBuilder;
import com.pxs.dependencies.model.Node;

@Service
public class DependenciesResourceService {

	private static final String CACHE_NAME = "dependenciesResourceCache";
	private static final String GRAPH_CACHE_NAME = "dependenciesGraphResourceCache";

	@Autowired
	private DependenciesResourceJsonBuilder dependenciesResourceJsonBuilder;

	@Autowired
	private DependenciesGraphResourceJsonBuilder dependenciesGraphResourceJsonBuilder;

//	@Cacheable(value = CACHE_NAME)
	public List<Node> getDependenciesResourceJson() {
		return dependenciesResourceJsonBuilder.build();
	}

//	@Cacheable(value = GRAPH_CACHE_NAME)
	public Map<String, Object> getDependenciesGraphResourceJson() {
		return dependenciesGraphResourceJsonBuilder.build();
	}
}