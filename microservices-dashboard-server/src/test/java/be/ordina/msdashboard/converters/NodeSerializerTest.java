package be.ordina.msdashboard.converters;

import be.ordina.msdashboard.constants.Constants;
import be.ordina.msdashboard.model.Node;
import be.ordina.msdashboard.model.NodeBuilder;
import org.junit.Test;

import java.io.UnsupportedEncodingException;

import static be.ordina.msdashboard.constants.Constants.MICROSERVICE;
import static be.ordina.msdashboard.constants.Constants.STATUS;
import static org.assertj.core.api.Assertions.assertThat;

public class NodeSerializerTest {

	@Test
	public void serialize() {
		Node node = NodeBuilder.node()
							   .withId("key1")
							   .withDetail("type", Constants.MICROSERVICE)
							   .withDetail(Constants.STATUS, "UP")
							   .withLinkedToNode(NodeBuilder.node().withId("1a").withDetail(Constants.STATUS, "DOWN").withDetail("type", "REST").build())
							   .build();

		String nodeAsJson = "{\"linkedToNodeIds\":[],\"linkedFromNodeIds\":[],\"id\":\"key1\",\"details\":{\"type\":\"MICROSERVICE\",\"status\":\"UP\"},\"linkedToNodes\":[{\"linkedToNodeIds\":[],\"linkedFromNodeIds\":[],\"id\":\"1a\",\"details\":{\"type\":\"REST\",\"status\":\"DOWN\"},\"linkedToNodes\":[]}]}";

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
