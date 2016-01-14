package com.pxs.dependencies.caching;

import java.io.IOException;
import java.util.Map;

import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pxs.utilities.converters.json.JsonToObjectConverter;
import com.pxs.utilities.converters.json.ObjectToJsonConverter;

public class DependenciesMapSerializer<T> implements RedisSerializer<T> {

	private Class<T> type;

	public DependenciesMapSerializer(Class<T> type) {
		this.type = type;
	}

	@Override
	public byte[] serialize(T t) throws SerializationException {
		ObjectToJsonConverter<T> serializer = new ObjectToJsonConverter<>();
		String serializedObject = serializer.convert(t);
		return serializedObject.getBytes();
	}

	@Override
	public T deserialize(byte[] bytes) throws SerializationException {
    return null;
	}
}
