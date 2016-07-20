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
package be.ordina.msdashboard.cache;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import org.junit.Test;
import org.mockito.Mockito;

/**
 * Tests for {@link CacheCleaningBean}
 *
 * @author Tim De Bruyn
 */
public class CacheCleaningBeanTest {

	@Test
	public void cleanEvictTrue(){
		NodeCache nodeCache = Mockito.mock(NodeCache.class);
		CacheCleaningBean cacheCleaningBean= new CacheCleaningBean(nodeCache, true);
		cacheCleaningBean.clean();
		verify(nodeCache).evictGraphCache();
		verify(nodeCache).evictHealthsCache();
		verify(nodeCache).evictIndexesCache();
		verify(nodeCache).evictPactsCache();
	}
	
	@Test
	public void cleanEvictFalse(){
		NodeCache nodeCache = Mockito.mock(NodeCache.class);
		CacheCleaningBean cacheCleaningBean= new CacheCleaningBean(nodeCache, false);
		cacheCleaningBean.clean();
		verifyZeroInteractions(nodeCache);
	}
}
