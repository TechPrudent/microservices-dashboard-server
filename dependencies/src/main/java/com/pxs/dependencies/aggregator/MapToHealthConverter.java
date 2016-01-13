package com.pxs.dependencies.aggregator;

import static org.springframework.boot.actuate.health.Health.status;

import java.util.Map;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Health.Builder;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class MapToHealthConverter implements Converter<Map<String, Object>, Health> {

	private static final String STATUS = "status";

	@Override
	public Health convert(final Map<String, Object> source) {
		System.out.println("****************************" + source);
		if (source.containsKey(STATUS)) {
			return convertMapToHealth(source);
		} else {
			throw new IllegalStateException("Health deserialization fails because no status was found at the root");
		}
	}

	private Health convertMapToHealth(final Map<String, Object> source) {
		Builder builder = status((String) source.get(STATUS));
		for (String key : source.keySet()) {
			if (!STATUS.equals(key)) {
				Object nested = source.get(key);
				if (nested instanceof Map && ((Map) nested).containsKey(STATUS)) {
					builder.withDetail(key, convertMapToHealth((Map<String, Object>) source.get(key)));
				} else {
					builder.withDetail(key, source.get(key));
				}
			}
		}
		return builder.build();
	}
}
