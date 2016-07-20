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

import static be.ordina.msdashboard.constants.Constants.MICROSERVICE;
import static be.ordina.msdashboard.constants.Constants.STATUS;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import be.ordina.msdashboard.model.Node;

/**
 * Tests for {@link JsonToObjectConverter}
 *
 * @author Tim Ysewyn
 */
@RunWith(MockitoJUnitRunner.class)
public class JsonToObjectConverterTest {

    @Test
    public void shouldReturnNode() {
        String nodeAsJson = "{\"id\":\"key1\",\"details\":{\"type\":\"MICROSERVICE\",\"status\":\"UP\"},\"linkedToNodes\":[{\"id\":\"1a\",\"details\":{\"type\":\"REST\",\"status\":\"DOWN\"}}]}";
        JsonToObjectConverter<Node> converter = new JsonToObjectConverter<>(Node.class);

        Node node = converter.convert(nodeAsJson);

        assertThat(node).isNotNull();
        assertThat(node.getId()).isEqualTo("key1");
        assertThat(node.getDetails().size()).isEqualTo(2);
        assertThat(node.getDetails().get("type")).isEqualTo(MICROSERVICE);
        assertThat(node.getDetails().get(STATUS)).isEqualTo("UP");
        assertThat(node.getLinkedToNodes().size()).isEqualTo(1);

        assertThat(node.getLinkedToNodes().iterator().next().getId()).isEqualTo("1a");
        assertThat(node.getLinkedToNodes().iterator().next().getDetails().size()).isEqualTo(2);
        assertThat(node.getLinkedToNodes().iterator().next().getDetails().get("type")).isEqualTo("REST");
        assertThat(node.getLinkedToNodes().iterator().next().getDetails().get(STATUS)).isEqualTo("DOWN");
    }

    @Test
    public void emptyStringShouldReturnNull() {
        String nodeAsJson = "";
        JsonToObjectConverter<Node> converter = new JsonToObjectConverter<>(Node.class);

        Node node = converter.convert(nodeAsJson);

        assertThat(node).isNull();
    }

    @Test(expected = IllegalArgumentException.class)
    public void malformedJsonShouldThrowAnIllegalArgumentException() {
        String nodeAsJson = "{\"id\":\"key1\",}";
        JsonToObjectConverter<Node> converter = new JsonToObjectConverter<>(Node.class);

        converter.convert(nodeAsJson);
    }

}
