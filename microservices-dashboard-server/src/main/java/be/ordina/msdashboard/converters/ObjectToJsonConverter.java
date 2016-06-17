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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.converter.Converter;

/**
 * @author Andreas Evers
 */
public class ObjectToJsonConverter<T> implements Converter<T, String> {

	private static final Logger logger = LoggerFactory.getLogger(ObjectToJsonConverter.class);
	private static final ObjectWriter OBJECT_WRITER = new ObjectMapper().writer();

	@Override
	public String convert(T source) {
		if (source == null)
			return null;

		try {
			return OBJECT_WRITER.writeValueAsString(source);
		} catch (JsonProcessingException e) {
			logger.error("unable to create string value", e);
			throw new IllegalArgumentException("", e);

		}
	}
}
