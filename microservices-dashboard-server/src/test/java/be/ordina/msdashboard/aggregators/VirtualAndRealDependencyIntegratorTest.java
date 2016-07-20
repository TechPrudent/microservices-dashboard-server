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
package be.ordina.msdashboard.aggregators;

import be.ordina.msdashboard.constants.Constants;
import be.ordina.msdashboard.model.Node;
import be.ordina.msdashboard.model.NodeBuilder;
import com.google.common.collect.Sets;
import org.junit.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link VirtualAndRealDependencyIntegrator}
 * TODO: To be reviewed + refactored
 *
 * @author Andreas Evers
 */
public class VirtualAndRealDependencyIntegratorTest {

	@Test
	public void shouldIntegrateVirtualNodesToReal() throws Exception {
		VirtualAndRealDependencyIntegrator virtualAndRealDependencyIntegrator = new VirtualAndRealDependencyIntegrator();
		Set<Node> integratedDependencies = virtualAndRealDependencyIntegrator.integrateVirtualNodesWithReal(getRealDependencies(), null, getVirtualDependencies());
		assertThat(integratedDependencies.size()).isEqualTo(4);
	}

	private Set<Node> getRealDependencies() {
		return Sets.newHashSet(
				NodeBuilder.node().withId("key1")
						.withDetail("type", Constants.MICROSERVICE)
						.withDetail(Constants.STATUS, "UP")
						.build(),

				NodeBuilder.node().withId("key2")
						.withDetail("type", Constants.MICROSERVICE)
						.withDetail(Constants.STATUS, "UP")
						.build(),
				NodeBuilder.node().withId("key3")
						.withDetail("type", Constants.MICROSERVICE)
						.withDetail(Constants.STATUS, "UP")
						.build()
		);
	}

	private Set<Node> getVirtualDependencies() {
		return Sets.newHashSet(
				NodeBuilder.node().withId("key1")
						.withDetail("type", Constants.MICROSERVICE)
						.withDetail(Constants.STATUS, "UP")
						.build(),

				NodeBuilder.node().withId("key4")
						.withDetail("type", Constants.MICROSERVICE)
						.withDetail(Constants.STATUS, "UP")
						.build()
		);
	}
}