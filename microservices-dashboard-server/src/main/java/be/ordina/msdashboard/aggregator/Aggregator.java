package be.ordina.msdashboard.aggregator;

import be.ordina.msdashboard.node.Node;

import java.util.Collections;
import java.util.List;

/**
 * An abstract aggregator that can be used to compose nodes with their respective information.
 *
 * @author Tim Ysewyn
 */
public abstract class Aggregator {

    public final List<Node> getNodes() {
        List<Node> nodes = fetchNodes();
        if (nodes == null) {
            nodes = Collections.emptyList();
        }
        return nodes;
    }

    protected abstract List<Node> fetchNodes();

}
