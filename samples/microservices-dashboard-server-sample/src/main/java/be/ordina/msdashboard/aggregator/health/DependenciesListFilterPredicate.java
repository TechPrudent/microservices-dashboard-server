package be.ordina.msdashboard.aggregator.health;

import be.ordina.msdashboard.constants.Constants;
import be.ordina.msdashboard.model.Node;
import com.google.common.base.Predicate;

public class DependenciesListFilterPredicate implements Predicate<Node> {

	@Override
	public boolean apply(Node input) {
		return !Constants.HYSTRIX.equals(input.getId()) && !Constants.DISK_SPACE.equals(input.getId())
				&& !Constants.DISCOVERY.equals(input.getId()) && !Constants.CONFIGSERVER.equals(input.getId());
	}
}

