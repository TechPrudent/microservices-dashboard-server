package com.pxs.dependencies.aggregator;

import java.util.List;

import com.google.common.base.Predicate;
import com.pxs.dependencies.model.Node;

public class DependenciesListFilterPredicate implements Predicate<Node>{

	private static final String DISK_SPACE = "diskSpace";
	private static final String HYSTRIX = "hystrix";

	@Override
	public boolean apply(Node input) {
		return !HYSTRIX.equals(input.getId())&& !DISK_SPACE.equals(input.getId());
	}
}

