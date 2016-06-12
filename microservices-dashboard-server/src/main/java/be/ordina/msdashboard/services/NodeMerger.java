package be.ordina.msdashboard.services;

import be.ordina.msdashboard.model.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.functions.Func2;

import java.util.ArrayList;
import java.util.Optional;

/**
 * @author Tim Ysewyn
 */
public class NodeMerger {

    private static final Logger logger = LoggerFactory.getLogger(NodeMerger.class);

    public static Func2<ArrayList<Node>, Node, ArrayList<Node>> merge() {
        return (mergedNodes, node) -> {

            // TODO: we should be able to modify nodes in a general way before merging, eg. convert all service names to lowercase
            // Aggregator specific logic should not come here, eg. removing the Eureka description

            Optional<Integer> nodeIndex = mergedNodes.stream()
                    .filter(n -> n.getId().equals(node.getId()))
                    .map(mergedNodes::indexOf)
                    .findFirst();

            if (nodeIndex.isPresent()) {
                logger.info("Node previously added, merging");
                mergedNodes.get(nodeIndex.get())
                        .mergeWith(node);
            } else {
                logger.info("Node was not merged before, adding it to the list");
                mergedNodes.add(node);
            }

            return mergedNodes;
        };
    }
}
