package com.pxs.dependencies.aggregator;

import static com.pxs.dependencies.constants.Constants.CONFIGSERVER;
import static com.pxs.dependencies.constants.Constants.DISCOVERY;
import static com.pxs.dependencies.constants.Constants.DISK_SPACE;
import static com.pxs.dependencies.constants.Constants.HYSTRIX;

import com.google.common.base.Predicate;
import com.pxs.dependencies.model.Node;

public class DependenciesListFilterPredicate implements Predicate<Node> {

	@Override
	public boolean apply(Node input) {
		return !HYSTRIX.equals(input.getId()) && !DISK_SPACE.equals(input.getId())
				&& !DISCOVERY.equals(input.getId()) && !CONFIGSERVER.equals(input.getId());
	}
}

