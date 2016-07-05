package be.ordina.msdashboard.aggregator;



import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import be.ordina.msdashboard.constants.Constants;
import be.ordina.msdashboard.model.Node;
import be.ordina.msdashboard.model.NodeBuilder;
import org.junit.Test;

import com.google.common.collect.Lists;

public class VirtualAndRealDependencyIntegratorTest {

	@Test
	public void shouldIntegrateVirtualNodesToReal() throws Exception {
		VirtualAndRealDependencyIntegrator virtualAndRealDependencyIntegrator = new VirtualAndRealDependencyIntegrator();
		List<Node> integratedDependencies = virtualAndRealDependencyIntegrator.integrateVirtualNodesWithReal(getRealDependencies(), null, getVirtualDependencies());
		assertThat(integratedDependencies.size()).isEqualTo(4);
		assertThat(integratedDependencies.get(0).getLinkedNodes().size()).isEqualTo(4);
		assertThat(integratedDependencies.get(0).getLinkedNodes().get(0).getId()).isEqualTo("1a");
		assertThat(integratedDependencies.get(0).getLinkedNodes().get(1).getId()).isEqualTo("1b");
		assertThat(integratedDependencies.get(0).getLinkedNodes().get(2).getId()).isEqualTo("1c");
		assertThat(integratedDependencies.get(0).getLinkedNodes().get(3).getId()).isEqualTo("1d");
	}

	private List<Node> getRealDependencies() {
		return Lists.newArrayList(
				NodeBuilder.node().withId("key1")
						.withDetail("type", Constants.MICROSERVICE)
						.withDetail(Constants.STATUS, "UP")
						.withLinkedNode(NodeBuilder.node().withId("1a").withDetail(Constants.STATUS, "DOWN").withDetail("type", "SOAP").build())
						.withLinkedNode(NodeBuilder.node().withId("1b").withDetail(Constants.STATUS, "DOWN").withDetail("type", "SOAP").build())
						.withLinkedNode(NodeBuilder.node().withId("1c").withDetail(Constants.STATUS, "DOWN").withDetail("type", "SOAP").build())
						.build(),

				NodeBuilder.node().withId("key2")
						.withDetail("type", Constants.MICROSERVICE)
						.withDetail(Constants.STATUS, "UP")
						.withLinkedNode(NodeBuilder.node().withId("2a").withDetail(Constants.STATUS, "DOWN").withDetail("type", "SOAP").build())
						.withLinkedNode(NodeBuilder.node().withId("2b").withDetail(Constants.STATUS, "DOWN").withDetail("type", "SOAP").build())
						.withLinkedNode(NodeBuilder.node().withId("2c").withDetail(Constants.STATUS, "DOWN").withDetail("type", "SOAP").build())
						.build(),
				NodeBuilder.node().withId("key3")
						.withDetail("type", Constants.MICROSERVICE)
						.withDetail(Constants.STATUS, "UP")
						.withLinkedNode(NodeBuilder.node().withId("3a").withDetail(Constants.STATUS, "DOWN").withDetail("type", "SOAP").build())
						.withLinkedNode(NodeBuilder.node().withId("3b").withDetail(Constants.STATUS, "DOWN").withDetail("type", "SOAP").build())
						.withLinkedNode(NodeBuilder.node().withId("3c").withDetail(Constants.STATUS, "DOWN").withDetail("type", "SOAP").build())
						.build()
		);
	}

	private List<Node> getVirtualDependencies() {
		return Lists.newArrayList(
				NodeBuilder.node().withId("key1")
						.withDetail("type", Constants.MICROSERVICE)
						.withDetail(Constants.STATUS, "UP")
						.withLinkedNode(NodeBuilder.node().withId("1a").withDetail(Constants.STATUS, "DOWN").withDetail("type", "SOAP").build())
						.withLinkedNode(NodeBuilder.node().withId("1b").withDetail(Constants.STATUS, "DOWN").withDetail("type", "SOAP").build())
						.withLinkedNode(NodeBuilder.node().withId("1c").withDetail(Constants.STATUS, "DOWN").withDetail("type", "SOAP").build())
						.withLinkedNode(NodeBuilder.node().withId("1d").withDetail(Constants.STATUS, "DOWN").withDetail("type", "SOAP").build())
						.build(),

				NodeBuilder.node().withId("key4")
						.withDetail("type", Constants.MICROSERVICE)
						.withDetail(Constants.STATUS, "UP")
						.withLinkedNode(NodeBuilder.node().withId("4a").withDetail(Constants.STATUS, "DOWN").withDetail("type", "SOAP").build())
						.withLinkedNode(NodeBuilder.node().withId("4b").withDetail(Constants.STATUS, "DOWN").withDetail("type", "SOAP").build())
						.withLinkedNode(NodeBuilder.node().withId("4c").withDetail(Constants.STATUS, "DOWN").withDetail("type", "SOAP").build())
						.build()
		);
	}
}