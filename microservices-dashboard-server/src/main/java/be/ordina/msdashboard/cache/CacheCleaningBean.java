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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Andreas Evers
 */
public class CacheCleaningBean {

	private static final Logger logger = LoggerFactory.getLogger(CacheCleaningBean.class);

	private NodeCache nodeCache;
	private boolean evict;

	public CacheCleaningBean(NodeCache nodeCache, boolean evict) {
		this.nodeCache = nodeCache;
		this.evict = evict;
	}

	public void clean() {
		logger.info("Cleaning cache: " + evict);
		if (evict) {
			nodeCache.evictGraphCache();
			nodeCache.evictHealthsCache();
			nodeCache.evictIndexesCache();
			nodeCache.evictPactsCache();
		}
	}
}
