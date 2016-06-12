package be.ordina.msdashboard.aggregator;

import be.ordina.msdashboard.aggregator.health.HealthIndicatorsAggregator;
import be.ordina.msdashboard.aggregator.index.IndexesAggregator;
import be.ordina.msdashboard.aggregator.pact.PactsAggregator;
import be.ordina.msdashboard.constants.Constants;
import be.ordina.msdashboard.model.Node;
import be.ordina.msdashboard.model.NodeBuilder;
import be.ordina.msdashboard.store.NodeStore;
import com.github.tomakehurst.wiremock.common.Json;
import com.google.common.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import rx.Observable;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;

import java.util.*;

import static be.ordina.msdashboard.constants.Constants.*;
import static com.google.common.base.CaseFormat.LOWER_CAMEL;
import static com.google.common.base.CaseFormat.LOWER_HYPHEN;
import static com.google.common.collect.Maps.newHashMap;

/**
 * @author Andreas Evers
 * @author Tim Ysewyn
 */
public class ObservablesToGraphConverter {

	private static final Logger LOG = LoggerFactory.getLogger(ObservablesToGraphConverter.class);

	private HealthIndicatorsAggregator healthIndicatorsAggregator;
	private IndexesAggregator indexesAggregator;
	private PactsAggregator pactsAggregator;

	private NodeStore redisService;

	private final Map<String, Object> graph;

	@Autowired
	public ObservablesToGraphConverter(final HealthIndicatorsAggregator healthIndicatorsAggregator, final IndexesAggregator indexesAggregator,
									   final PactsAggregator pactsAggregator, final NodeStore redisService) {
		this.healthIndicatorsAggregator = healthIndicatorsAggregator;
		this.indexesAggregator = indexesAggregator;
		this.pactsAggregator = pactsAggregator;
		this.redisService = redisService;
		graph = new HashMap<>();
		initGraph(graph);
	}

	public Map<String, Object> build() {
		Observable<Node> microservicesAndBackends = healthIndicatorsAggregator.aggregateNodes();
		Observable<Node> resources = indexesAggregator.aggregateNodes();
		Observable<Node> pactComponents = pactsAggregator.aggregateNodes();
		Observable<Node> virtualNodes = redisService.getAllNodesAsObservable();

		return createGraph(microservicesAndBackends, resources, pactComponents, virtualNodes);
	}

	private Map<String, Object> createGraph(final Observable<Node> microservicesWithTheirBackends, Observable<Node> resources, Observable<Node> pactComponents, Observable<Node> virtualNodes) {
		/*Observable<Map<String, Object>> displayableMicroservices = microservicesWithTheirBackends.map(node -> {
			Set<Map<String, Object>> displayableNodes = new HashSet<>();
			String microserviceName = convertMicroserviceName(node.getId());
			Map<String, Object> microserviceNode = createMicroserviceNode(microserviceName, node);
			displayableNodes.add(microserviceNode);
			Set<Node> dependencyNodes = node.getLinkedToNodes();
			removeEurekaDescription(dependencyNodes);
			for (Node dependencyNode : dependencyNodes) {
				if (MICROSERVICE.equals(dependencyNode.getDetails().get(TYPE))) {
					dependencyNode.setId(convertMicroserviceName(dependencyNode.getId()));
				}
				Integer lane = determineLane(dependencyNode.getDetails());
				displayableNodes.add(createNode(dependencyNode.getId(), lane, dependencyNode.getDetails()));
			}
			return Observable.from(displayableNodes);
		}).flatMap(el -> el);*/

		Observable.mergeDelayError(microservicesWithTheirBackends, resources, pactComponents, virtualNodes)
				.observeOn(Schedulers.io())
				.doOnNext(node -> LOG.info("Merging node with id '{}'", node.getId()))
				.reduce(new ArrayList<>(), mergeNodes())
				.flatMap(Observable::from)
				.doOnNext(node -> LOG.info("Merged node with id '{}'", node.getId()))
				.reduce(initNodesAndLinksMap(), toNodesAndLinksMap())
				.map(mapToDisplayableNode())
				.toBlocking()
				.subscribe(element -> {
					graph.put(NODES, element.get(NODES));
					graph.put(LINKS, element.get(LINKS));
				}, throwable -> {
                    //System.out.println("Exceptions: " + ((CompositeException) throwable).getExceptions());
                    System.out.println(throwable);
                    throwable.printStackTrace();
                });

		return graph;
	}

	private Func2<ArrayList<Node>, Node, ArrayList<Node>> mergeNodes() {
		return (mergedNodes, node) -> {
			Optional<Integer> nodeIndex = findNodeIndexByNode(mergedNodes, node);
			if (nodeIndex.isPresent()) {
				LOG.info("Node previously added, merging");
				mergedNodes.get(nodeIndex.get()).mergeWith(node);
			} else {
				LOG.info("Node was not merged before, adding it to the list");
				mergedNodes.add(node);
			}

			return mergedNodes;
		};
	}

	private Map<String, Object> initNodesAndLinksMap() {
		Map<String, Object> nodesAndLinksMap = new HashMap<>();
		nodesAndLinksMap.put(Constants.NODES, new ArrayList<>());
		nodesAndLinksMap.put(Constants.LINKS, new HashSet<>());
		return nodesAndLinksMap;
	}

	private Func2<Map<String, Object>, Node, Map<String, Object>> toNodesAndLinksMap() {
		return (nodesAndLinksMap, node) -> {
			List<Node> mappedNodes = (List<Node>) nodesAndLinksMap.get(Constants.NODES);
			Optional<Integer> nodeIndex = findNodeIndexByNode(mappedNodes, node);
			int mappedNodeIndex;

			if (nodeIndex.isPresent()) {
				mappedNodeIndex = nodeIndex.get();
				Node mappedNode = mappedNodes.get(mappedNodeIndex);
				mappedNode.mergeWith(node);
				mappedNodes.set(mappedNodeIndex, mappedNode);
			} else {
				mappedNodes.add(node);
				mappedNodeIndex = mappedNodes.size() - 1;
			}

			Set<Map<String, Integer>> links = (Set<Map<String, Integer>>) nodesAndLinksMap.get(Constants.LINKS);

			Set<String> linkedToNodeIds = node.getLinkedToNodeIds();
			for (String nodeId : linkedToNodeIds) {
				nodeIndex = findNodeIndexById(mappedNodes, nodeId);
				int linkedNodeIndex;
				if (!nodeIndex.isPresent()) {
					mappedNodes.add(createNodeById(nodeId));
					linkedNodeIndex = mappedNodes.size() - 1;
				} else {
					linkedNodeIndex = nodeIndex.get();
				}
				links.add(createLink(mappedNodeIndex, linkedNodeIndex));
			}

			Set<String> linkedFromNodeIds = node.getLinkedFromNodeIds();
			for (String nodeId : linkedFromNodeIds) {
				nodeIndex = findNodeIndexById(mappedNodes, nodeId);
				int linkedNodeIndex;
				if (!nodeIndex.isPresent()) {
					mappedNodes.add(createNodeById(nodeId));
					linkedNodeIndex = mappedNodes.size() - 1;
				} else {
					linkedNodeIndex = nodeIndex.get();
				}
				links.add(createLink(linkedNodeIndex, mappedNodeIndex));
			}
			
			return nodesAndLinksMap;
		};
	}

	private Optional<Integer> findNodeIndexByNode(List<Node> nodes, Node node) {
		return findNodeIndexById(nodes, node.getId());
	}

	private Optional<Integer> findNodeIndexById(List<Node> nodes, String nodeId) {
		return nodes.stream()
				.filter(n -> n.getId().equals(nodeId))
				.map(nodes::indexOf)
				.findFirst();
	}

	private Node createNodeById(String nodeId) {
		return NodeBuilder.node().withId(nodeId).build();
	}

	@VisibleForTesting
	Map<String, Integer> createLink(final int source, final int target) {
		Map<String, Integer> link = new HashMap<>();
		link.put("source", source);
		link.put("target", target);
		return link;
	}

	private String convertMicroserviceName(String name) {
		return LOWER_CAMEL.to(LOWER_HYPHEN, name);
	}

	@VisibleForTesting
	Map<String, Object> createMicroserviceNode(final String microServicename, final Node node) {
		Map<String, Object> details = new HashMap<>();
		for (Map.Entry<String, Object> detail : node.getDetails().entrySet()) {
			if (!(detail.getValue() instanceof Node)) {
				details.put(detail.getKey(), detail.getValue());
			}
		}
		Integer lane = determineLane(details);
		return createNode(microServicename, lane, details);
	}

	private void removeEurekaDescription(final Set<Node> dependencyNodes) {
		for (Node dependencyNode : dependencyNodes) {
			if (DESCRIPTION.equals(dependencyNode.getId())) {
				dependencyNodes.remove(dependencyNode);
				break;
			}
		}
	}

	private Func1<Map<String, Object>, Map<String, Object>> mapToDisplayableNode() {
		return map -> {
			List<Node> nodes = (List<Node>) map.get(NODES);
			List<Map<String, Object>> displayableNodes = new ArrayList<>();
			for (Node node : nodes) {
				displayableNodes.add(createDisplayableNode(node));
			}
			map.replace(NODES, displayableNodes);
			return map;
		};
	}

	@VisibleForTesting
	Integer determineLane(Map<String, Object> details) {
		String type = (String) details.get(TYPE);
		if (type == null) {
			return new Integer("3");
		}
		switch (type) {
			case UI_COMPONENT: return new Integer("0");
			case RESOURCE: return new Integer("1");
			case MICROSERVICE: return new Integer("2");
			default: return new Integer("3");
		}
	}

	private Map<String, Object> createDisplayableNode(final Node node) {
		LOG.info("Creating displayable node: " + node.getId());
		Integer lane = determineLane(node.getDetails());
		return createNode(node.getId(), lane, node.getDetails());
	}

	private Map<String, Object> createResourceNode(final Node node) {
		return createNode(node.getId(), node.getLane(), node.getDetails());
	}

	private Map<String, Object> createPactComponentNode(final Node node) {
		Integer lane = determineLane(node.getDetails());
		return createNode(node.getId(), lane, node.getDetails());
	}

	private Map<String, Object> createNode(final String id, final Integer lane, Map<String, Object> details) {
		Map<String, Object> node = new HashMap<>();
		node.put(ID, id);
		node.put(LANE, lane);
		node.put(DETAILS, details);
		return node;
	}

	private List<Map<Object, Object>> constructLanes() {
		List<Map<Object, Object>> lanes = new ArrayList<>();
		lanes.add(constructLane(0, UI));
		lanes.add(constructLane(1, RESOURCES));
		lanes.add(constructLane(2, MICROSERVICES));
		lanes.add(constructLane(3, BACKEND));
		return lanes;
	}

	private List<String> constructTypes() {
		List<String> types = new ArrayList<>();
		types.add(DB);
		types.add(MICROSERVICE);
		types.add(REST);
		types.add(SOAP);
		types.add(JMS);
		types.add(RESOURCE);
		return types;
	}

	private Map<Object, Object> constructLane(final int lane, final String type) {
		Map<Object, Object> laneMap = newHashMap();
		laneMap.put(LANE, lane);
		laneMap.put(TYPE, type);
		return laneMap;
	}

	private void initGraph(final Map<String, Object> graph) {
		graph.put(DIRECTED, true);
		graph.put(MULTIGRAPH, false);
		graph.put(GRAPH, new String[0]);
		graph.put(LANES, constructLanes());
		graph.put(TYPES, constructTypes());
	}
}
