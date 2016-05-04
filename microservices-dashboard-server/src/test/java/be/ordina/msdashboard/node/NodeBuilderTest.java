package be.ordina.msdashboard.node;

import org.junit.Test;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class NodeBuilderTest {

    @Test
    public void shouldReturnEmptyNode() {
        Node emptyNode = NodeBuilder.node().build();

        assertThat(emptyNode).isNotNull();
        assertThat(emptyNode.getId()).isNull();
        assertThat(emptyNode.getLane()).isNull();
        assertThat(emptyNode.getLinkedNodes()).isEmpty();
        assertThat(emptyNode.getDetails()).isEmpty();
    }

    @Test
    public void shouldReturnSingleNode() {
        Node singleNode = NodeBuilder.node().withId("id").withLane(1).withDetail("key", "value").build();

        assertThat(singleNode).isNotNull();
        assertThat(singleNode.getId()).isEqualTo("id");
        assertThat(singleNode.getLane()).isEqualTo(1);
        assertThat(singleNode.getLinkedNodes()).isEmpty();
        assertThat(singleNode.getDetails()).isNotEmpty();
        assertThat(singleNode.getDetails().get("key")).isEqualTo("value");
    }

    @Test
    public void shouldReturnOneNodeWithTwoLinkedNodes() {
        Node firstNode = NodeBuilder.node().withId("id").withLane(1).build();

        assertThat(firstNode).isNotNull();
        assertThat(firstNode.getId()).isEqualTo("id");
        assertThat(firstNode.getLane()).isEqualTo(1);
        assertThat(firstNode.getLinkedNodes()).isEmpty();
        assertThat(firstNode.getDetails()).isEmpty();

        Node secondNode = NodeBuilder.node().withId("id").withLane(2).build();

        assertThat(secondNode).isNotNull();
        assertThat(secondNode.getId()).isEqualTo("id");
        assertThat(secondNode.getLane()).isEqualTo(2);
        assertThat(secondNode.getLinkedNodes()).isEmpty();
        assertThat(secondNode.getDetails()).isEmpty();

        Map<String, String> thirdNodeDetails = Collections.unmodifiableMap(
                Stream.of(new AbstractMap.SimpleEntry<>("key", "value"))
                      .collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue))
        );

        Node thirdNode = NodeBuilder.node().withId("id").withLane(3)
                                    .withLinkedNodes(Arrays.asList(firstNode, secondNode)).withDetails(thirdNodeDetails)
                                    .build();

        assertThat(thirdNode).isNotNull();
        assertThat(thirdNode.getId()).isEqualTo("id");
        assertThat(thirdNode.getLane()).isEqualTo(3);
        assertThat(thirdNode.getLinkedNodes()).isNotEmpty();
        assertThat(thirdNode.getLinkedNodes().get(0)).isEqualTo(firstNode);
        assertThat(thirdNode.getLinkedNodes().get(1)).isEqualTo(secondNode);
        assertThat(thirdNode.getDetails()).isNotEmpty();
        assertThat(thirdNode.getDetails().get("key")).isEqualTo("value");
    }

}
