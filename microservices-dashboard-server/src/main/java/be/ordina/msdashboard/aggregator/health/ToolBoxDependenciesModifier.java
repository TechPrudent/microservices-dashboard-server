/*
 * Copyright 2012-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
