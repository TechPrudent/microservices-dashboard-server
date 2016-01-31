package com.pxs.dependencies;

import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import com.pxs.dependencies.model.Node;
import com.pxs.utilities.converters.json.JsonToObjectConverter;
import com.pxs.utilities.converters.json.ObjectToJsonConverter;

public class NodeSerializer implements RedisSerializer<Node> {

	@Override
	public byte[] serialize(Node node) throws SerializationException {
		ObjectToJsonConverter<Node> converter = new ObjectToJsonConverter<>();
		return converter.convert(node).getBytes();
	}

	@Override
	public Node deserialize(byte[] bytes) throws SerializationException {
		JsonToObjectConverter<Node> converter = new JsonToObjectConverter<>(Node.class);
		Node node = null;
		if (bytes != null) {
			node = converter.convert(new String(bytes));
		}
		return node;
	}
}
