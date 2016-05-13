package be.ordina.msdashboard.aggregator;

import be.ordina.msdashboard.node.Node;

import java.util.Collections;
import java.util.List;

/**
 * An abstract aggregator that can be used to compose nodes with their respective information.
 *
 * @author Tim Ysewyn
 */
public interface Aggregator {

    List<Node> getNodes();

}
