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
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TODO: To be reviewed (missing linkedNodeIds)
 *
 * Tests for {@link ObjectToJsonConverter}
 *
 * @author Andreas Evers
 */
@RunWith(MockitoJUnitRunner.class)
public class ObjectToJsonConverterTest {

    @Test
    public void shouldReturnJSON() {
        Node node = NodeBuilder.node()
                               .withId("key1")
                               .withDetail("type", Constants.MICROSERVICE)
                               .withDetail(Constants.STATUS, "UP")
                               .build();

        ObjectToJsonConverter<Node> converter = new ObjectToJsonConverter<>();

        String nodeAsJson = converter.convert(node);

        assertThat(nodeAsJson).isNotNull();
        assertThat(nodeAsJson).isEqualTo("{\"id\":\"key1\",\"details\":{\"type\":\"MICROSERVICE\",\"status\":\"UP\"}}");
    }

    @Test
    public void shouldReturnNull() {
        ObjectToJsonConverter<Node> converter = new ObjectToJsonConverter<>();

        String nodeAsJson = converter.convert(null);

        assertThat(nodeAsJson).isNull();
    }

}
