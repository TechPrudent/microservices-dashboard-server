package com.pxs.dependencies.aggregator;

import static com.pxs.dependencies.constants.Constants.GROUP;
import static com.pxs.dependencies.constants.Constants.MICROSERVICE;
import static com.pxs.dependencies.constants.Constants.TYPE;
import static com.pxs.dependencies.constants.Constants.VERSION;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.pxs.dependencies.model.Node;

@Component
public class MapToNodeConverter {

	private static final String STATUS = "status";

	public Node convert(final Map<String, Object> source) {
		if (source.containsKey(STATUS)) {
			Node node = convertMapToNode(source);
			Map<String, Object> details = node.getDetails();
			details.put(TYPE, MICROSERVICE);
			return node;
		} else {
			throw new IllegalStateException("Health deserialization fails because no status was found at the root");
		}
	}

	private Node convertMapToNode(final Map<String, Object> source) {
		Node node = new Node();
		Map<String, Object> ownDetails = new HashMap<>();
		ownDetails.put(STATUS, source.get(STATUS));
		node.setDetails(ownDetails);
		for (String key : source.keySet()) {
			if (!STATUS.equals(key)) {
				Object nested = source.get(key);
				if (nested instanceof Map && ((Map) nested).containsKey(STATUS)) {
					ownDetails.put(key, convertMapToNode((Map<String, Object>) source.get(key)));
				} else {
					ownDetails.put(key, source.get(key));
				}
			}
		}
		return node;
	}
}

