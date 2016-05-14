package be.ordina.msdashboard.cache;

import be.ordina.msdashboard.store.RedisStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CacheCleaningBean {

	private static final Logger LOG = LoggerFactory.getLogger(CacheCleaningBean.class);

	private RedisStore redisStore;
	private boolean evict;

	public CacheCleaningBean(RedisStore redisService, boolean evict) {
		this.redisStore = redisService;
		this.evict = evict;
	}

	public void clean() {
		LOG.info("Cleaning cache: " + evict);
		if (evict) {
			redisStore.evictHealthsCache();
			redisStore.evictIndexesCache();
			redisStore.evictPactsCache();
		}
	}
}
