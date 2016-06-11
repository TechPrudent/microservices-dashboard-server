package be.ordina.msdashboard.aggregator;

import be.ordina.msdashboard.model.Node;
import rx.Observable;

/**
 * @author Andreas Evers
 */
public interface NodeAggregator {

    Observable<Node> aggregateNodes();
}
