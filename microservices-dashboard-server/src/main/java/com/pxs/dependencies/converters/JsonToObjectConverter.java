package com.pxs.dependencies.converters;

import static org.apache.commons.lang3.StringUtils.isEmpty;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.converter.Converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

public class JsonToObjectConverter<T> implements Converter<String, T> {

	private static final Logger LOG = LoggerFactory.getLogger(JsonToObjectConverter.class);
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
			LOG.error("unable to read value", e);
			throw new IllegalArgumentException(objectAsString, e);
		}
	}
}
