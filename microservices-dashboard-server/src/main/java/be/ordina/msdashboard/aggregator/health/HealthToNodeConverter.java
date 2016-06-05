package be.ordina.msdashboard.aggregator.health;

import static be.ordina.msdashboard.constants.Constants.*;
import static be.ordina.msdashboard.constants.Constants.HYSTRIX;
import static be.ordina.msdashboard.constants.Constants.MICROSERVICE;
import static be.ordina.msdashboard.constants.Constants.TYPE;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import be.ordina.msdashboard.constants.Constants;
import be.ordina.msdashboard.model.Node;
import rx.Observable;

public class HealthToNodeConverter {

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
		Node node = new Node("masternode");
		Map<String, Object> ownDetails = node.getDetails();
		ownDetails.put(STATUS, source.get(STATUS));
		for (String key : source.keySet()) {
			if (!STATUS.equals(key)) {
				Object nested = source.get(key);
				if (nested instanceof Map && ((Map) nested).containsKey(STATUS)) {
					Node nestedNode = new Node(key);
					copyDetails((Map<String, Object>) source.get(key), nestedNode.getDetails());
					node.getLinkedToNodes().add(nestedNode);
					nestedNode.getLinkedFromNodeIds().add(node.getId());
//					node.getLinkedToNodes().add(convertMapToNode((Map)nested));
				} else {
					ownDetails.put(key, source.get(key));
				}
			}
		}
		return node;
	}

	private static void copyDetails(Map<String, Object> source, Map<String, Object> target) {
		for (Map.Entry<String, Object> sourceEntry : source.entrySet()) {
			target.put(sourceEntry.getKey(), sourceEntry.getValue());
		}
	}

	// REACTIVE WAY

	public static Observable<Node> convertToNodes(final String serviceId, final Map<String, Object> source) {
		Set<Node> nodes = new HashSet<>();
		Node topLevelNode = new Node(serviceId);
		Map<String, Object> ownDetails = topLevelNode.getDetails();
		ownDetails.put(STATUS, source.get(STATUS));
		ownDetails.put(TYPE, MICROSERVICE);
		nodes.add(topLevelNode);
		for (String key : source.keySet()) {
			if (!STATUS.equals(key) && !HYSTRIX.equals(key) && !DISK_SPACE.equals(key)
					&& !DISCOVERY.equals(key) && !CONFIGSERVER.equals(key)) {
				Object nested = source.get(key);
				if (nested instanceof Map && ((Map) nested).containsKey(STATUS)) {
					Node nestedNode = new Node(key);
					copyDetails((Map<String, Object>) source.get(key), nestedNode.getDetails());
					topLevelNode.getLinkedToNodeIds().add(nestedNode.getId());
					nestedNode.getLinkedFromNodeIds().add(topLevelNode.getId());
					nodes.add(nestedNode);
//					node.getLinkedToNodes().add(convertMapToNode((Map)nested));
				} else {
					ownDetails.put(key, source.get(key));
				}
			}
		}
		return Observable.from(nodes);
	}
}

