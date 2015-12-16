package com.pxs.dependencies.services;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;

@Service
public class RedisService {

	@Autowired
	private RedisTemplate<String, Object> template;

	public void saveNode(final String nodeName, final List<String> properties) {
		template.opsForList().leftPushAll(nodeName, properties);
	}

	public void updateNode(final String nodeName, final List<String> properties) {
		template.opsForList().leftPushAll(nodeName, properties);
	}

	@SuppressWarnings("unchecked")
	public Map<String, List<String>> getNodes() {
		Map<String, List<String>> nodesWithDependencies = Maps.newHashMap();

		Set<String> keys = template.keys("*");
		for (String key : keys) {
			List<String> properties = (List<String>) template.opsForList().leftPop(key);
			nodesWithDependencies.put(key, properties);
		}

		return nodesWithDependencies;
	}
}
