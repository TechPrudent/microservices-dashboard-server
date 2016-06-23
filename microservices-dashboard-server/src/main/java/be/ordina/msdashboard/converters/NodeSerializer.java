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

import be.ordina.msdashboard.model.Node;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import java.util.HashMap;

/**
 * @author Andreas Evers
 */
public class NodeSerializer implements RedisSerializer<Object> {

	@Override
	public byte[] serialize(Object node) throws SerializationException {
		if (node instanceof Node) {
			ObjectToJsonConverter<Node> converter = new ObjectToJsonConverter<>();
			return converter.convert((Node) node).getBytes();
		} else {
			ObjectToJsonConverter<HashMap> converter = new ObjectToJsonConverter<>();
			return converter.convert((HashMap) node).getBytes();
		}
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
