package com.pxs.dependencies.caching;

import java.lang.reflect.Method;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.DefaultRedisCachePrefix;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCachePrefix;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
@EnableCaching
@EnableConfigurationProperties(CachingProperties.class)
public class CachingConfig {

	@Autowired
	private CachingProperties cachingProperties;
	@Bean
	public RedisCacheManager cacheManager(RedisTemplate<String, String> redisTemplate) {
		RedisCacheManager cacheManager = new RedisCacheManager(redisTemplate);
		cacheManager.setDefaultExpiration(cachingProperties.getDefaultExpiration());
		RedisCachePrefix redisCachePrefix = new DefaultRedisCachePrefix();
		redisCachePrefix.prefix(cachingProperties.getRedisCachePrefix());
		cacheManager.setCachePrefix(redisCachePrefix);
		return cacheManager;
	}

	@Bean
	public KeyGenerator simpleKeyGenerator() {
		return new KeyGenerator() {
			@Override
			public Object generate(Object target, Method method, Object... params) {
				StringBuilder sb = new StringBuilder();
				sb.append(target.getClass().getName());
				sb.append(method.getName());
				for (Object obj : params) {
					sb.append(obj.toString());
				}
				return sb.toString();
			}
		};
	}
}
