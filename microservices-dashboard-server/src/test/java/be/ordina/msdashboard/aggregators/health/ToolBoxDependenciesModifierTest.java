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
