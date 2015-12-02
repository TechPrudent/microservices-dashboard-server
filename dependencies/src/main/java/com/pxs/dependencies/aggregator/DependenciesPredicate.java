package com.pxs.dependencies.aggregator;

import java.util.Map.Entry;

import com.google.common.base.Predicate;

public class DependenciesPredicate implements Predicate<Entry<String, Object>> {

	private static final String DISK_SPACE = "diskSpace";
	private static final String HYSTRIX = "hystrix";

	@Override
	public boolean apply(final Entry<String, Object> input) {
		return !input.getKey().equals(HYSTRIX) &&
				!input.getKey().equals(DISK_SPACE);
	}
}
