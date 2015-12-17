package com.pxs.dependencies.services;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;

@Service
public class VirtualDependenciesService {

	@Autowired
	private RedisService redisService;

	public void createNewNode(final String nodeName, final List<String> dependencies) {
		redisService.saveNode(nodeName, dependencies);
	}

	public void updateNode(final String nodeName, final List<String> dependencies) {
		redisService.updateNode(nodeName, dependencies);
	}

	public Map<String, Map<String, Object>> getAllNodes() {
		Map<String, List<String>> nodesWithDependencies = redisService.getNodes();

		Map<String, Map<String, Object>> nodesWithDependenciesAndHealth = Maps.newHashMap();

		// TODO convert to the right format and return
		// get online status of every dependency (backend) or just return
		// online?

		return nodesWithDependenciesAndHealth;
	}
}
