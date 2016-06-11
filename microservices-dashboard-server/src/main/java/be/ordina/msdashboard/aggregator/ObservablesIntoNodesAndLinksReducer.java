package be.ordina.msdashboard.aggregator;

import be.ordina.msdashboard.constants.Constants;
import be.ordina.msdashboard.model.Node;
import be.ordina.msdashboard.model.NodeBuilder;
import com.google.common.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.util.*;

/**
 * @author Andreas Evers
 */
public class ObservablesIntoNodesAndLinksReducer {

    private static final Logger LOG = LoggerFactory.getLogger(ObservablesIntoNodesAndLinksReducer.class);

    public Observable<Map<String, Object>> reduceToNodesAndLinksMap(Observable<Node> mergedObservable) {
        return mergedObservable.reduce(initNodesAndLinks(), (Map<String, Object> returnedNodesAndLinks, Node node) -> {
            LOG.info("Reducing nodes and links for node with id: " + node.getId());
            List<Node> existingNodes = (List<Node>) returnedNodesAndLinks.get(Constants.NODES);
            int existingNodeIndex;
            Optional<Integer> nodeIndex = findNodeIndexByNode(existingNodes, node);
            if (!nodeIndex.isPresent()) {
                LOG.info("Adding node1: " + node.getId());
                existingNodes.add(node);
                existingNodeIndex = existingNodes.size() - 1;
            } else {
                Node existingNode = existingNodes.get(nodeIndex.get());
                existingNode.mergeWith(node);
                existingNodeIndex = nodeIndex.get();
            }
            Set<Map<String, Integer>> links = (Set<Map<String, Integer>>) returnedNodesAndLinks.get(Constants.LINKS);
            Set<String> linkedToNodeIds = node.getLinkedToNodeIds();
            for (String nodeId : linkedToNodeIds) {
                nodeIndex = findNodeIndexById(existingNodes, nodeId);
                int existingLinkedNodeIndex;
                if (!nodeIndex.isPresent()) {
                    LOG.info("Adding node2: " + nodeId + " / from: " + node.getId());
                    existingNodes.add(createNodeById(nodeId));
                    existingLinkedNodeIndex = existingNodes.size() - 1;
                } else {
                    existingLinkedNodeIndex = nodeIndex.get();
                }
                LOG.info("Adding link1: {} to {}", existingNodeIndex, existingLinkedNodeIndex);
                links.add(createLink(existingNodeIndex, existingLinkedNodeIndex));
            }
            Set<String> linkedFromNodeIds = node.getLinkedFromNodeIds();
            for (String nodeId : linkedFromNodeIds) {
                nodeIndex = findNodeIndexById(existingNodes, nodeId);
                int existingLinkedNodeIndex;
                if (!nodeIndex.isPresent()) {
                    LOG.info("Adding node3: " + nodeId);
                    existingNodes.add(createNodeById(nodeId));
                    existingLinkedNodeIndex = existingNodes.size() - 1;
                } else {
                    existingLinkedNodeIndex = nodeIndex.get();
                }
                LOG.info("Adding link2: {} to {} for node {} and fromNodeId {}", existingLinkedNodeIndex, existingNodeIndex, node, nodeId);
                links.add(createLink(existingLinkedNodeIndex, existingNodeIndex));
            }
            return returnedNodesAndLinks;
        });
    }

    private Map initNodesAndLinks() {
        Map initialExistingNodes = new HashMap<String, Object>();
        initialExistingNodes.put(Constants.NODES, new ArrayList<>());
        initialExistingNodes.put(Constants.LINKS, new HashSet<>());
        return initialExistingNodes;
    }

    private Optional<Integer> findNodeIndexByNode(List<Node> existingNodes, Node node) {
        return findNodeIndexById(existingNodes, node.getId());
    }

    private Optional<Integer> findNodeIndexById(List<Node> existingNodes, String nodeId) {
        return existingNodes.stream()
                .filter(node -> node.getId().equals(nodeId))
                .map(node -> existingNodes.indexOf(node))
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
}