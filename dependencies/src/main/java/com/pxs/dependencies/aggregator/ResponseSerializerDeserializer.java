package com.pxs.dependencies.aggregator;

import java.io.IOException;
import java.util.List;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pxs.dependencies.model.Node;
import com.pxs.utilities.converters.json.ObjectToJsonConverter;

@Component
public class ResponseSerializerDeserializer {

	public String serializeResponse(List<Node> nodes) {
		ObjectToJsonConverter<List<Node>> serializer = new ObjectToJsonConverter<>();
		return serializer.convert(nodes);
	}

	public List<Node> deserializeResponse(final String json) {
		ObjectMapper mapper = new ObjectMapper();
		try {
			List<Node> deserializedList = mapper.readValue(json, new TypeReference<List<Node>>() {
			});
			return deserializedList;
		} catch (IOException e) {
			throw new IllegalArgumentException(json, e);
		}
	}
}
