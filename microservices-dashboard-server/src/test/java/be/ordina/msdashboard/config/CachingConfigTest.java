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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.core.RedisTemplate;

import be.ordina.msdashboard.cache.CacheCleaningBean;
import be.ordina.msdashboard.cache.CachingProperties;
import be.ordina.msdashboard.nodes.stores.RedisStore;

/**
 * Tests for {@link CachingConfig}
 *
 * @author Tim Ysewyn
 */
@RunWith(MockitoJUnitRunner.class)
public class CachingConfigTest {

    @InjectMocks
    private RedisConfiguration cachingConfig;

    @Mock
    private CachingProperties cachingProperties;

    @Test
    @SuppressWarnings("unchecked")
    public void shouldReturnARedisCacheManager() {
        RedisTemplate<String, Object> redisTemplate = mock(RedisTemplate.class);

        when(cachingProperties.getDefaultExpiration()).thenReturn(30L);
        when(cachingProperties.getRedisCachePrefix()).thenReturn("prefix");

        RedisCacheManager redisCacheManager = cachingConfig.cacheManager(redisTemplate);

        assertThat(redisCacheManager).isNotNull();
    }

    @Test
    public void shouldNotEvictCache() {
        RedisStore redisService = mock(RedisStore.class);

        when(cachingProperties.isEvict()).thenReturn(false);

        CacheCleaningBean cacheCleaningBean = cachingConfig.cacheCleaningBean(redisService);
        assertThat(cacheCleaningBean).isNotNull();

        cacheCleaningBean.clean();

        verify(redisService, times(0)).evictHealthsCache();
        verify(redisService, times(0)).evictIndexesCache();
        verify(redisService, times(0)).evictPactsCache();
    }

    @Test
    public void shouldEvictCache() {
        RedisStore redisService = mock(RedisStore.class);

        when(cachingProperties.isEvict()).thenReturn(true);

        CacheCleaningBean cacheCleaningBean = cachingConfig.cacheCleaningBean(redisService);
        assertThat(cacheCleaningBean).isNotNull();

        cacheCleaningBean.clean();

        verify(redisService, times(1)).evictHealthsCache();
        verify(redisService, times(1)).evictIndexesCache();
        verify(redisService, times(1)).evictPactsCache();
    }

}
