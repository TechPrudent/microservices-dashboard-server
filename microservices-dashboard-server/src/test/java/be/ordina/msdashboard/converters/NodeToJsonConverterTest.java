package be.ordina.msdashboard.converters;

import be.ordina.msdashboard.node.Node;
import be.ordina.msdashboard.node.NodeBuilder;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class NodeToJsonConverterTest {

    @Test
    public void shouldReturnJSON() {
        Node node = NodeBuilder.node()
                               .withId("key1")
                               .withDetail("type", "MICROSERVICE")
                               .withDetail("status", "UP")
                               .withLinkedNode(NodeBuilder.node().withId("1a").withDetail("status", "DOWN").withDetail("type", "REST").build())
                               .build();

        NodeToJsonConverter converter = new NodeToJsonConverter();

        String nodeAsJson = converter.convert(node);

        assertThat(nodeAsJson).isNotNull();
        assertThat(nodeAsJson).isEqualTo("{\"id\":\"key1\",\"details\":{\"type\":\"MICROSERVICE\",\"status\":\"UP\"},\"linkedNodes\":[{\"id\":\"1a\",\"details\":{\"type\":\"REST\",\"status\":\"DOWN\"},\"linkedNodes\":[]}]}");
    }

    @Test
    public void shouldReturnNull() {
        NodeToJsonConverter converter = new NodeToJsonConverter();

        String nodeAsJson = converter.convert(null);

        assertThat(nodeAsJson).isNull();
    }

}
