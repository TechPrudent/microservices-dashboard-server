package com.pxs.dependencies.aggregator;

import static com.pxs.dependencies.constants.Constants.CONFIGSERVER;
import static com.pxs.dependencies.constants.Constants.DISCOVERY;
import static com.pxs.dependencies.constants.Constants.GROUP;
import static com.pxs.dependencies.constants.Constants.MICROSERVICE;
import static com.pxs.dependencies.constants.Constants.TOOLBOX;
import static com.pxs.dependencies.constants.Constants.TYPE;

import java.util.ArrayList;
import java.util.Collection;

import com.pxs.dependencies.model.Node;

public class ToolBoxDependenciesModifier {

	public Collection<Node> modify(final Collection<Node> source) {
		Collection<Node> modified = new ArrayList<>();
		for (Node node : source) {
			if (DISCOVERY.equals(node.getId()) || CONFIGSERVER.equals(node.getId())) {
				node.getDetails().put(TYPE, MICROSERVICE);
				node.getDetails().put(GROUP, TOOLBOX);
			}
			modified.add(node);
		}
		return modified;
	}
}
