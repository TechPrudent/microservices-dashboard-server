package com.pxs.dependencies.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;
import com.pxs.dependencies.model.Node;
import com.pxs.utilities.converters.json.JsonToObjectConverter;

@Service
public class RedisService {

	@Autowired
	private RedisTemplate<String, String> redisTemplate;

	@Autowired
	private RedisConnectionFactory redisConnectionFactory;

	public List<Node> getAllNodes() {
		List<Node> results = new ArrayList<>();
		Set<String> keys = redisTemplate.keys("*");
		for (String key : keys) {
			String nodeString = redisTemplate.opsForValue().get(key);
			JsonToObjectConverter<Node> converter = new JsonToObjectConverter<>(Node.class);
			Node node = converter.convert(nodeString);
			node.setId(key);
			results.add(node);
		}
		return results;
	}

	public void saveNode(final String nodeData) {
		String nodeId = getNodeId(nodeData);
		redisTemplate.opsForValue().set(nodeId, nodeData);
	}

	public void deleteNode(final String nodeId) {
		redisTemplate.delete(nodeId);
	}

	public void deleteAllNodes() {
		redisTemplate.delete(redisTemplate.keys("*"));
	}

	private String getNodeId(String nodeData) {
		JsonToObjectConverter<Node> converter = new JsonToObjectConverter<>(Node.class);
		Node node = converter.convert(nodeData);
		return node.getId();
	}

	public void flushDB() {
		redisConnectionFactory.getConnection().flushDb();
	}
}
