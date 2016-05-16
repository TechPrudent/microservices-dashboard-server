package be.ordina.msdashboard.aggregator;



import static org.assertj.core.api.Assertions.assertThat;

import java.util.Iterator;
import java.util.Set;

import be.ordina.msdashboard.constants.Constants;
import be.ordina.msdashboard.model.Node;
import be.ordina.msdashboard.model.NodeBuilder;
import com.google.common.collect.Sets;
import org.junit.Test;

public class VirtualAndRealDependencyIntegratorTest {

	@Test
	public void shouldIntegrateVirtualNodesToReal() throws Exception {
		VirtualAndRealDependencyIntegrator virtualAndRealDependencyIntegrator = new VirtualAndRealDependencyIntegrator();
		Set<Node> integratedDependencies = virtualAndRealDependencyIntegrator.integrateVirtualNodesWithReal(getRealDependencies(), null, getVirtualDependencies());
		assertThat(integratedDependencies.size()).isEqualTo(4);
		assertThat(integratedDependencies.iterator().next().getLinkedToNodes().size()).isEqualTo(4);
		Iterator<Node> nodeIterator = integratedDependencies.iterator().next().getLinkedToNodes().iterator();
		assertThat(nodeIterator.next().getId()).isEqualTo("1a");
		assertThat(nodeIterator.next().getId()).isEqualTo("1b");
		assertThat(nodeIterator.next().getId()).isEqualTo("1c");
		assertThat(nodeIterator.next().getId()).isEqualTo("1d");
	}

	private Set<Node> getRealDependencies() {
		return Sets.newHashSet(
				NodeBuilder.node().withId("key1")
						.withDetail("type", Constants.MICROSERVICE)
						.withDetail(Constants.STATUS, "UP")
						.withLinkedToNode(NodeBuilder.node().withId("1a").withDetail(Constants.STATUS, "DOWN").withDetail("type", "SOAP").build())
						.withLinkedToNode(NodeBuilder.node().withId("1b").withDetail(Constants.STATUS, "DOWN").withDetail("type", "SOAP").build())
						.withLinkedToNode(NodeBuilder.node().withId("1c").withDetail(Constants.STATUS, "DOWN").withDetail("type", "SOAP").build())
						.build(),

				NodeBuilder.node().withId("key2")
						.withDetail("type", Constants.MICROSERVICE)
						.withDetail(Constants.STATUS, "UP")
						.withLinkedToNode(NodeBuilder.node().withId("2a").withDetail(Constants.STATUS, "DOWN").withDetail("type", "SOAP").build())
						.withLinkedToNode(NodeBuilder.node().withId("2b").withDetail(Constants.STATUS, "DOWN").withDetail("type", "SOAP").build())
						.withLinkedToNode(NodeBuilder.node().withId("2c").withDetail(Constants.STATUS, "DOWN").withDetail("type", "SOAP").build())
						.build(),
				NodeBuilder.node().withId("key3")
						.withDetail("type", Constants.MICROSERVICE)
						.withDetail(Constants.STATUS, "UP")
						.withLinkedToNode(NodeBuilder.node().withId("3a").withDetail(Constants.STATUS, "DOWN").withDetail("type", "SOAP").build())
						.withLinkedToNode(NodeBuilder.node().withId("3b").withDetail(Constants.STATUS, "DOWN").withDetail("type", "SOAP").build())
						.withLinkedToNode(NodeBuilder.node().withId("3c").withDetail(Constants.STATUS, "DOWN").withDetail("type", "SOAP").build())
						.build()
		);
	}

	private Set<Node> getVirtualDependencies() {
		return Sets.newHashSet(
				NodeBuilder.node().withId("key1")
						.withDetail("type", Constants.MICROSERVICE)
						.withDetail(Constants.STATUS, "UP")
						.withLinkedToNode(NodeBuilder.node().withId("1a").withDetail(Constants.STATUS, "DOWN").withDetail("type", "SOAP").build())
						.withLinkedToNode(NodeBuilder.node().withId("1b").withDetail(Constants.STATUS, "DOWN").withDetail("type", "SOAP").build())
						.withLinkedToNode(NodeBuilder.node().withId("1c").withDetail(Constants.STATUS, "DOWN").withDetail("type", "SOAP").build())
						.withLinkedToNode(NodeBuilder.node().withId("1d").withDetail(Constants.STATUS, "DOWN").withDetail("type", "SOAP").build())
						.build(),

				NodeBuilder.node().withId("key4")
						.withDetail("type", Constants.MICROSERVICE)
						.withDetail(Constants.STATUS, "UP")
						.withLinkedToNode(NodeBuilder.node().withId("4a").withDetail(Constants.STATUS, "DOWN").withDetail("type", "SOAP").build())
						.withLinkedToNode(NodeBuilder.node().withId("4b").withDetail(Constants.STATUS, "DOWN").withDetail("type", "SOAP").build())
						.withLinkedToNode(NodeBuilder.node().withId("4c").withDetail(Constants.STATUS, "DOWN").withDetail("type", "SOAP").build())
						.build()
		);
	}
}