package be.ordina.msdashboard.services;

import be.ordina.msdashboard.aggregator.NodeAggregator;
import be.ordina.msdashboard.constants.Constants;
import be.ordina.msdashboard.model.Node;
import be.ordina.msdashboard.store.NodeStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import rx.Observable;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;

import java.util.*;
import java.util.stream.Collectors;

import static be.ordina.msdashboard.constants.Constants.*;
import static be.ordina.msdashboard.constants.Constants.LANE;
import static be.ordina.msdashboard.constants.Constants.TYPE;
import static com.google.common.collect.Maps.newHashMap;

/**
 * @author Andreas Evers
 * @author Tim Ysewyn
 */
public class DependenciesResourceService {

	private static final Logger logger = LoggerFactory.getLogger(DependenciesResourceService.class);

	private final List<NodeAggregator> aggregators;
	private final NodeStore redisService;

	public DependenciesResourceService(List<NodeAggregator> aggregators, NodeStore redisService) {
		this.aggregators = aggregators;
		this.redisService = redisService;
	}

    // TODO: Caching
    //@Cacheable
	public Map<String, Object> getDependenciesGraphResourceJson() {
		List<Observable<Node>> observables = aggregators.stream()
				.collect(Collectors.mapping(NodeAggregator::aggregateNodes, Collectors.toList()));
		observables.add(redisService.getAllNodesAsObservable());

		Map<String, Object> graph = new HashMap<>();
		graph.put(DIRECTED, true);
		graph.put(MULTIGRAPH, false);
		graph.put(GRAPH, new String[0]);
		graph.put(LANES, constructLanes());
		graph.put(TYPES, constructTypes());

        Map<String, Object> nodesAndLinks = Observable.mergeDelayError(observables)
				.observeOn(Schedulers.io())
				.doOnNext(node -> logger.info("Merging node with id '{}'", node.getId()))
				.reduce(new ArrayList<>(), NodeMerger.merge())
				.doOnNext(node -> logger.info("Merged all emitted nodes"))
				.doOnNext(nodes -> logger.info("Converting to nodes and links map"))
                .map(GraphMapper.toGraph())
				.doOnNext(nodesAndLinksMap -> logger.info("Converted to nodes and links map"))
                .doOnError(throwable -> logger.error("An error occurred: {}", throwable))
				.toBlocking()
                .first();

        graph.put(NODES, nodesAndLinks.get(NODES));
        graph.put(LINKS, nodesAndLinks.get(LINKS));

		return graph;
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
}