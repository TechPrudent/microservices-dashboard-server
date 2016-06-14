package be.ordina.msdashboard.services;

import be.ordina.msdashboard.constants.Constants;
import be.ordina.msdashboard.model.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.functions.Func1;

import java.util.*;

import static be.ordina.msdashboard.constants.Constants.*;

/**
 * @author Tim Ysewyn
 */
public class GraphMapper {

    private static final Logger logger = LoggerFactory.getLogger(GraphMapper.class);

    public static Func1<List<Node>, Map<String, Object>> toGraph() {
        return (nodes) -> {
            List<Map<String, Object>> displayableNodes = new ArrayList<>();
            Set<Map<String, Integer>> links = new HashSet<>();

            nodes.stream()
                    .forEach(node -> {
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

            Map<String, Object> nodesAndLinksMap = new HashMap<>();

            nodesAndLinksMap.put(Constants.NODES, displayableNodes);
            nodesAndLinksMap.put(Constants.LINKS, links);

            return nodesAndLinksMap;
        };
    }

    private static Optional<Integer> findNodeIndexById(List<Node> nodes, String nodeId) {
        return nodes.stream()
                .filter(n -> n.getId().equals(nodeId))
                .map(nodes::indexOf)
                .findFirst();
    }

    private static Map<String, Integer> createLink(final int source, final int target) {
        Map<String, Integer> link = new HashMap<>();
        link.put("source", source);
        link.put("target", target);
        return link;
    }

    private static Map<String, Object> createDisplayableNode(final Node node) {
        logger.info("Creating displayable node: " + node.getId());
        Integer lane = determineLane(node.getDetails());
        return createNode(node.getId(), lane, node.getDetails());
    }

    private static Map<String, Object> createNode(final String id, final Integer lane, Map<String, Object> details) {
        Map<String, Object> node = new HashMap<>();
        node.put(ID, id);
        node.put(LANE, lane);
        node.put(DETAILS, details);
        return node;
    }

    private static Integer determineLane(Map<String, Object> details) {
        String type = (String) details.get(TYPE);
        if (type == null) {
            return new Integer("3");
        }
        switch (type) {
            case UI_COMPONENT:
                return new Integer("0");
            case RESOURCE:
                return new Integer("1");
            case MICROSERVICE:
                return new Integer("2");
            default:
                return new Integer("3");
        }
    }
}
