package com.pxs.dependencies.caching;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

import com.pxs.dependencies.services.RedisService;

@Component
@Scope(value = "refresh", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class CacheCleaningBean {

	private static final Logger LOG = LoggerFactory.getLogger(CacheCleaningBean.class);

	@Autowired
	private RedisService redisService;

	public CacheCleaningBean() {
		LOG.info("Cleaning cash");
		redisService.evictCache();
	}
}
