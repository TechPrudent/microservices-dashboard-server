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
package be.ordina.msdashboard.aggregators.health;

import static be.ordina.msdashboard.constants.Constants.CONFIGSERVER;
import static be.ordina.msdashboard.constants.Constants.DISCOVERY;
import static be.ordina.msdashboard.constants.Constants.GROUP;
import static be.ordina.msdashboard.constants.Constants.MICROSERVICE;
import static be.ordina.msdashboard.constants.Constants.TOOLBOX;
import static be.ordina.msdashboard.constants.Constants.TYPE;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.Collections.emptyList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import org.junit.Test;

import be.ordina.msdashboard.model.Node;

/**
 * Tests for {@link ToolBoxDependenciesModifier}
 *
 * @author Tim De Bruyn
 */
public class ToolBoxDependenciesModifierTest {

	private ToolBoxDependenciesModifier toolBoxDependenciesModifier = new ToolBoxDependenciesModifier();
	
	@Test
	public void emptySourceList(){
		Collection<Node> modified = toolBoxDependenciesModifier.modify(emptyList()); 
		assertNotNull(modified);
	}
	
	@Test
	public void noMicroServiceNode(){
		Collection<Node> source = newHashSet(new Node("some id"), new Node("some other id"));
		Collection<Node> modified = toolBoxDependenciesModifier.modify(source);
		assertEquals(source, modified);
	}
	
	@Test
	public void someMicroserviceNodes(){
		Collection<Node> source = newHashSet(new Node(DISCOVERY), new Node("some id"), new Node(CONFIGSERVER));
		HashSet<Node> modified = (HashSet<Node>)toolBoxDependenciesModifier.modify(source);
		assertEquals(3, modified.size());
		Iterator<Node> nodes = modified.iterator();
		
		Node node1 = nodes.next();
		assertNotEquals(MICROSERVICE, node1.getDetails().get(TYPE));
		assertNotEquals(TOOLBOX, node1.getDetails().get(GROUP), TOOLBOX);
		
		Node node2 = nodes.next();
		assertEquals(MICROSERVICE, node2.getDetails().get(TYPE));
		assertEquals(TOOLBOX, node2.getDetails().get(GROUP));

		Node node3 = nodes.next();
		assertEquals(MICROSERVICE, node3.getDetails().get(TYPE));
		assertEquals(TOOLBOX, node3.getDetails().get(GROUP));
	}
}
