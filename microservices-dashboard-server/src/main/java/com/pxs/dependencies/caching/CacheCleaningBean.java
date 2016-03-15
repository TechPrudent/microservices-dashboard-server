package com.pxs.dependencies.caching;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.pxs.dependencies.services.RedisService;

public class CacheCleaningBean {

	private static final Logger LOG = LoggerFactory.getLogger(CacheCleaningBean.class);

	public CacheCleaningBean(RedisService redisService, boolean evict) {
		LOG.info("Cleaning cash=" + evict);
		if (evict) {
			redisService.evictCache();
		}
	}
}
