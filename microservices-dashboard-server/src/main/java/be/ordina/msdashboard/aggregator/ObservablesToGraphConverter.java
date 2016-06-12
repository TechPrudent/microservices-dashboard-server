package be.ordina.msdashboard.aggregator;

import be.ordina.msdashboard.constants.Constants;
import be.ordina.msdashboard.model.Node;
import be.ordina.msdashboard.store.NodeStore;
import com.google.common.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import rx.Observable;
import rx.functions.Func2;
import rx.schedulers.Schedulers;

import java.util.*;
import java.util.stream.Collectors;

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

	private final List<NodeAggregator> aggregators;
	private final NodeStore redisService;

	private final Map<String, Object> graph;

	@Autowired
	public ObservablesToGraphConverter(List<NodeAggregator> aggregators, NodeStore redisService) {
		this.aggregators = aggregators;
		this.redisService = redisService;

		graph = new HashMap<>();
		initGraph(graph);
	}

	public Map<String, Object> build() {
		List<Observable<Node>> observables = aggregators.stream().collect(Collectors.mapping(NodeAggregator::aggregateNodes, Collectors.toList()));
		observables.add(redisService.getAllNodesAsObservable());

		Observable.mergeDelayError(observables)
				.observeOn(Schedulers.io())
				.doOnNext(node -> LOG.info("Merging node with id '{}'", node.getId()))
				.reduce(new ArrayList<>(), mergeNodes())
				.doOnNext(node -> LOG.info("Merged all emitted nodes"))
				.doOnNext(node -> LOG.info("Converting to nodes and links map"))
				.reduce(new HashMap<>(), toNodesAndLinksMap())
				.doOnNext(nodesAndLinksMap -> LOG.info("Converted to nodes and links map"))
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

	private Func2<Map<String, Object>, ArrayList<Node>, Map<String, Object>> toNodesAndLinksMap() {
		return (nodesAndLinksMap, nodes) -> {
			List<Map<String, Object>> displayableNodes = new ArrayList<>();
			Set<Map<String, Integer>> links = new HashSet<>();

			nodes.stream().forEach(node -> {
				displayableNodes.add(createDisplayableNode(node));

				int mappedNodeIndex = nodes.indexOf(node);

				Set<String> linkedToNodeIds = node.getLinkedToNodeIds();
				for (String nodeId : linkedToNodeIds) {
					Optional<Integer> nodeIndex = findNodeIndexById(nodes, nodeId);
					if (nodeIndex.isPresent()) {
						links.add(createLink(mappedNodeIndex, nodeIndex.get()));
					}
				}

				Set<String> linkedFromNodeIds = node.getLinkedFromNodeIds();
				for (String nodeId : linkedFromNodeIds) {
					Optional<Integer> nodeIndex = findNodeIndexById(nodes, nodeId);
					if (nodeIndex.isPresent()) {
						links.add(createLink(nodeIndex.get(), mappedNodeIndex));
					}
				}
			});

			nodesAndLinksMap.put(Constants.NODES, displayableNodes);
			nodesAndLinksMap.put(Constants.LINKS, links);

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
