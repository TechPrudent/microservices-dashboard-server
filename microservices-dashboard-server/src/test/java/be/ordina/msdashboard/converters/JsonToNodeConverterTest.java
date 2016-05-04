package be.ordina.msdashboard.converters;

import be.ordina.msdashboard.node.Node;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class JsonToNodeConverterTest {

    @Test
    public void shouldReturnNode() {
        String nodeAsJson = "{\"id\":\"key1\",\"details\":{\"type\":\"MICROSERVICE\",\"status\":\"UP\"},\"linkedNodes\":[{\"id\":\"1a\",\"details\":{\"type\":\"REST\",\"status\":\"DOWN\"}}]}";
        JsonToNodeConverter converter = new JsonToNodeConverter();

        Node node = converter.convert(nodeAsJson);

        assertThat(node).isNotNull();
        assertThat(node.getId()).isEqualTo("key1");
        assertThat(node.getDetails().size()).isEqualTo(2);
        assertThat(node.getDetails().get("type")).isEqualTo("MICROSERVICE");
        assertThat(node.getDetails().get("status")).isEqualTo("UP");
        assertThat(node.getLinkedNodes().size()).isEqualTo(1);

        assertThat(node.getLinkedNodes().get(0).getId()).isEqualTo("1a");
        assertThat(node.getLinkedNodes().get(0).getDetails().size()).isEqualTo(2);
        assertThat(node.getLinkedNodes().get(0).getDetails().get("type")).isEqualTo("REST");
        assertThat(node.getLinkedNodes().get(0).getDetails().get("status")).isEqualTo("DOWN");
    }

    @Test
    public void emptyStringShouldReturnNull() {
        String nodeAsJson = "";
        JsonToNodeConverter converter = new JsonToNodeConverter();

        Node node = converter.convert(nodeAsJson);

        assertThat(node).isNull();
    }

    @Test(expected = IllegalArgumentException.class)
    public void malformedJsonShouldThrowAnIllegalArgumentException() {
        String nodeAsJson = "{\"id\":\"key1\",}";
        JsonToNodeConverter converter = new JsonToNodeConverter();

        converter.convert(nodeAsJson);
    }

}
