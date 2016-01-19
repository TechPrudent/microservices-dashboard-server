package com.pxs.dependencies.caching;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("spring.cache")
public class CachingProperties {

	private String redisCachePrefix;
	private long defaultExpiration;

	public long getDefaultExpiration() {
		return defaultExpiration;
	}

	public void setDefaultExpiration(long defaultExpiration) {
		this.defaultExpiration = defaultExpiration;
	}

	public String getRedisCachePrefix() {
		return redisCachePrefix;
	}

	public void setRedisCachePrefix(String redisCachePrefix) {
		this.redisCachePrefix = redisCachePrefix;
	}
}
