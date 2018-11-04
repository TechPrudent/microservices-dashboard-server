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
package be.ordina.msdashboard.config;

import be.ordina.msdashboard.cache.CacheProperties;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.core.RedisTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link RedisConfiguration}
 *
 * @author Tim Ysewyn
 */
@RunWith(MockitoJUnitRunner.class)
public class RedisConfigurationTest {

	@InjectMocks
	private RedisConfiguration cachingConfig;

	@Mock
	private CacheProperties cacheProperties;

	@Test
	@SuppressWarnings("unchecked")
	public void shouldReturnARedisCacheManager() {
//		RedisTemplate<String, Object> redisTemplate = mock(RedisTemplate.class);
//
//		when(cacheProperties.getDefaultExpiration()).thenReturn(30L);
//		when(cacheProperties.getRedisCachePrefix()).thenReturn("prefix");
//
//		RedisCacheManager redisCacheManager = cachingConfig.cacheManager(redisTemplate);
//
//		assertThat(redisCacheManager).isNotNull();
	}

}
