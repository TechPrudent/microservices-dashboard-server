package be.ordina.msdashboard.caching;

import be.ordina.msdashboard.services.RedisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CacheCleaningBean {

	private static final Logger LOG = LoggerFactory.getLogger(CacheCleaningBean.class);

	public CacheCleaningBean(RedisService redisService, boolean evict) {
		LOG.info("Cleaning cash: " + evict);
		if (evict) {
			redisService.evictHealthsCache();
			redisService.evictIndexesCache();
		}
	}
}
