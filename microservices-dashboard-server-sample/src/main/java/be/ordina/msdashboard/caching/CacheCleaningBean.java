package be.ordina.msdashboard.caching;

import be.ordina.msdashboard.services.RedisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CacheCleaningBean {

	private static final Logger LOG = LoggerFactory.getLogger(CacheCleaningBean.class);

	private RedisService redisService;
	private boolean evict;

	public CacheCleaningBean(RedisService redisService, boolean evict) {
		this.redisService = redisService;
		this.evict = evict;
	}

	public void clean() {
		LOG.info("Cleaning cache: " + evict);
		if (evict) {
			redisService.evictHealthsCache();
			redisService.evictIndexesCache();
			redisService.evictPactsCache();
		}
	}
}
