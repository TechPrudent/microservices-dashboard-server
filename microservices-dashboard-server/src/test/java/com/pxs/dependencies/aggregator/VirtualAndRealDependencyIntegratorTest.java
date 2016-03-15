package com.pxs.dependencies.aggregator;



import static org.assertj.core.api.Assertions.assertThat;

import static com.pxs.dependencies.constants.Constants.MICROSERVICE;
import static com.pxs.dependencies.constants.Constants.STATUS;
import static com.pxs.dependencies.model.NodeBuilder.node;

import java.util.List;

import org.junit.Test;

import com.google.common.collect.Lists;
import com.pxs.dependencies.model.Node;

public class VirtualAndRealDependencyIntegratorTest {

	@Test
	public void shouldIntegrateVirtualNodesToReal() throws Exception {
		VirtualAndRealDependencyIntegrator virtualAndRealDependencyIntegrator = new VirtualAndRealDependencyIntegrator();
		List<Node> integratedDependencies = virtualAndRealDependencyIntegrator.integrateVirtualNodesToReal(getRealDependencies(), getVirtualDependencies());
		assertThat(integratedDependencies.size()).isEqualTo(4);
		assertThat(integratedDependencies.get(0).getLinkedNodes().size()).isEqualTo(4);
		assertThat(integratedDependencies.get(0).getLinkedNodes().get(0).getId()).isEqualTo("1a");
		assertThat(integratedDependencies.get(0).getLinkedNodes().get(1).getId()).isEqualTo("1b");
		assertThat(integratedDependencies.get(0).getLinkedNodes().get(2).getId()).isEqualTo("1c");
		assertThat(integratedDependencies.get(0).getLinkedNodes().get(3).getId()).isEqualTo("1d");
	}

	private List<Node> getRealDependencies() {
		return Lists.newArrayList(
				node().withId("key1")
						.withDetail("type", MICROSERVICE)
						.withDetail(STATUS, "UP")
						.withLinkedNode(node().withId("1a").withDetail(STATUS, "DOWN").withDetail("type", "SOAP").build())
						.withLinkedNode(node().withId("1b").withDetail(STATUS, "DOWN").withDetail("type", "SOAP").build())
						.withLinkedNode(node().withId("1c").withDetail(STATUS, "DOWN").withDetail("type", "SOAP").build())
						.build(),

				node().withId("key2")
						.withDetail("type", MICROSERVICE)
						.withDetail(STATUS, "UP")
						.withLinkedNode(node().withId("2a").withDetail(STATUS, "DOWN").withDetail("type", "SOAP").build())
						.withLinkedNode(node().withId("2b").withDetail(STATUS, "DOWN").withDetail("type", "SOAP").build())
						.withLinkedNode(node().withId("2c").withDetail(STATUS, "DOWN").withDetail("type", "SOAP").build())
						.build(),
				node().withId("key3")
						.withDetail("type", MICROSERVICE)
						.withDetail(STATUS, "UP")
						.withLinkedNode(node().withId("3a").withDetail(STATUS, "DOWN").withDetail("type", "SOAP").build())
						.withLinkedNode(node().withId("3b").withDetail(STATUS, "DOWN").withDetail("type", "SOAP").build())
						.withLinkedNode(node().withId("3c").withDetail(STATUS, "DOWN").withDetail("type", "SOAP").build())
						.build()
		);
	}

	private List<Node> getVirtualDependencies() {
		return Lists.newArrayList(
				node().withId("key1")
						.withDetail("type", MICROSERVICE)
						.withDetail(STATUS, "UP")
						.withLinkedNode(node().withId("1a").withDetail(STATUS, "DOWN").withDetail("type", "SOAP").build())
						.withLinkedNode(node().withId("1b").withDetail(STATUS, "DOWN").withDetail("type", "SOAP").build())
						.withLinkedNode(node().withId("1c").withDetail(STATUS, "DOWN").withDetail("type", "SOAP").build())
						.withLinkedNode(node().withId("1d").withDetail(STATUS, "DOWN").withDetail("type", "SOAP").build())
						.build(),

				node().withId("key4")
						.withDetail("type", MICROSERVICE)
						.withDetail(STATUS, "UP")
						.withLinkedNode(node().withId("4a").withDetail(STATUS, "DOWN").withDetail("type", "SOAP").build())
						.withLinkedNode(node().withId("4b").withDetail(STATUS, "DOWN").withDetail("type", "SOAP").build())
						.withLinkedNode(node().withId("4c").withDetail(STATUS, "DOWN").withDetail("type", "SOAP").build())
						.build()
		);
	}
}