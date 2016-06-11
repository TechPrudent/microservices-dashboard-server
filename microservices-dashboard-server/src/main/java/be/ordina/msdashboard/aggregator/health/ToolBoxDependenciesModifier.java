package be.ordina.msdashboard.aggregator.health;

import static be.ordina.msdashboard.constants.Constants.CONFIGSERVER;
import static be.ordina.msdashboard.constants.Constants.DISCOVERY;
import static be.ordina.msdashboard.constants.Constants.GROUP;
import static be.ordina.msdashboard.constants.Constants.MICROSERVICE;
import static be.ordina.msdashboard.constants.Constants.TOOLBOX;
import static be.ordina.msdashboard.constants.Constants.TYPE;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import be.ordina.msdashboard.model.Node;

/**
 * @author Andreas Evers
 */
public class ToolBoxDependenciesModifier {

	public Collection<Node> modify(final Collection<Node> source) {
		Collection<Node> modified = new HashSet<>();
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
