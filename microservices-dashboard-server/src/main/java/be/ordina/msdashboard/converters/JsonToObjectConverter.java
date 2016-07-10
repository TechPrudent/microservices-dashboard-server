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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.converter.Converter;

import java.io.IOException;

import static org.apache.commons.lang3.StringUtils.isEmpty;

/**
 * @author Andreas Evers
 */
public class JsonToObjectConverter<T> implements Converter<String, T> {

	private static final Logger logger = LoggerFactory.getLogger(JsonToObjectConverter.class);
	private static final ObjectReader OBJECT_READER = new ObjectMapper().reader();

	private Class<T> type;

	public JsonToObjectConverter(Class<T> type) {
		this.type = type;
	}

	@Override
	public T convert(String objectAsString) {
		try {
			if (isEmpty(objectAsString)) {
				return null;
			}
			return OBJECT_READER.withType(type).readValue(objectAsString);
		} catch (IOException e) {
			logger.error("unable to read value", e);
			throw new IllegalArgumentException(objectAsString, e);
		}
	}
}
