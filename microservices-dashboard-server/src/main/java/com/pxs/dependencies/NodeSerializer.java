package com.pxs.dependencies;

import com.pxs.dependencies.converters.JsonToObjectConverter;
import com.pxs.dependencies.converters.ObjectToJsonConverter;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import com.pxs.dependencies.model.Node;

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
