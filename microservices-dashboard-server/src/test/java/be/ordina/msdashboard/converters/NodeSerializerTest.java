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
package be.ordina.msdashboard.converters;

import be.ordina.msdashboard.constants.Constants;
import be.ordina.msdashboard.model.Node;
import be.ordina.msdashboard.model.NodeBuilder;
import org.junit.Test;

import java.io.UnsupportedEncodingException;

import static be.ordina.msdashboard.constants.Constants.*;
import static org.assertj.core.api.Assertions.assertThat;

public class NodeSerializerTest {

	@Test
	public void serialize() {
		Node node = NodeBuilder.node()
							   .withId("key1")
							   .withDetail("type", MICROSERVICE)
							   .withDetail(STATUS, "UP")
							   .withLinkedToNode(NodeBuilder.node().withId("1a").withDetail(STATUS, "DOWN").withDetail("type", "REST").build())
							   .build();

		String nodeAsJson = "{\"id\":\"key1\",\"details\":{\"type\":\"MICROSERVICE\",\"status\":\"UP\"},\"linkedToNodes\":[{\"id\":\"1a\",\"details\":{\"type\":\"REST\",\"status\":\"DOWN\"}}]}";

		NodeSerializer nodeSerializer = new NodeSerializer();

		byte[] bytes = nodeSerializer.serialize(node);
		String out = "";
		try {
			out = new String(bytes, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		System.out.println(out);
		assertThat(bytes).isEqualTo(nodeAsJson.getBytes());
	}

	@Test
	public void deserialize() {
		String nodeAsJson = "{\"id\":\"key1\",\"details\":{\"type\":\"MICROSERVICE\",\"status\":\"UP\"},\"linkedToNodes\":[{\"id\":\"1a\",\"details\":{\"type\":\"REST\",\"status\":\"DOWN\"}}]}";
		NodeSerializer nodeSerializer = new NodeSerializer();

		Node node = nodeSerializer.deserialize(nodeAsJson.getBytes());

		assertThat(node).isNotNull();
		assertThat(node.getId()).isEqualTo("key1");
		assertThat(node.getDetails().size()).isEqualTo(2);
		assertThat(node.getDetails().get("type")).isEqualTo(MICROSERVICE);
		assertThat(node.getDetails().get(STATUS)).isEqualTo("UP");
		assertThat(node.getLinkedToNodes().size()).isEqualTo(1);

		Node node1 = node.getLinkedToNodes().iterator().next();
		assertThat(node1.getId()).isEqualTo("1a");
		assertThat(node1.getDetails().size()).isEqualTo(2);
		assertThat(node1.getDetails().get("type")).isEqualTo("REST");
		assertThat(node1.getDetails().get(STATUS)).isEqualTo("DOWN");
	}
}
