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

import static be.ordina.msdashboard.model.NodeBuilder.node;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import be.ordina.msdashboard.constants.Constants;
import be.ordina.msdashboard.model.Node;
import be.ordina.msdashboard.model.NodeBuilder;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.ObjectWriter;

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
    public void shouldReturnNull() throws Exception {
        ObjectToJsonConverter<Node> converter = new ObjectToJsonConverter<>();

        String nodeAsJson = converter.convert(null);

        assertThat(nodeAsJson).isNull();
    }
    
    @SuppressWarnings("deprecation")
	@Test(expected=IllegalArgumentException.class)
    public void shouldFail() throws Exception{
    	ObjectToJsonConverter<Node> converter = new ObjectToJsonConverter<>();
    	ObjectWriter objectWriter = mock(ObjectWriter.class);
        setFinalStatic(ObjectToJsonConverter.class.getDeclaredField("OBJECT_WRITER"), objectWriter);
        
        when(objectWriter.writeValueAsString(Mockito.any(Node.class))).thenThrow(new JsonGenerationException("some json generating error"));
        
        converter.convert(node().build());
    }

    
    private void setFinalStatic(Field field, Object newValue) throws Exception {
        field.setAccessible(true);        
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        field.set(null, newValue);
    }
}
