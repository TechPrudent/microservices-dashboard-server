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

import be.ordina.msdashboard.constants.Constants;
import org.springframework.cache.annotation.CacheEvict;

/**
 * @author Andreas Evers
 */
public interface NodeCache {

    @CacheEvict(value = Constants.GRAPH_CACHE_NAME, allEntries = true)
    void evictGraphCache();

    @CacheEvict(value = Constants.HEALTH_CACHE_NAME, allEntries = true)
    void evictHealthsCache();

    @CacheEvict(value = Constants.INDEX_CACHE_NAME, allEntries = true)
    void evictIndexesCache();

    @CacheEvict(value = Constants.PACTS_CACHE_NAME, allEntries = true)
    void evictPactsCache();
}
