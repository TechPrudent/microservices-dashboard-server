package be.ordina.msdashboard.converters;

import be.ordina.msdashboard.converters.JsonToObjectConverter;
import be.ordina.msdashboard.converters.ObjectToJsonConverter;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import be.ordina.msdashboard.model.Node;

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
