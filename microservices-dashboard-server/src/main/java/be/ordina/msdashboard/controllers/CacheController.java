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
package be.ordina.msdashboard.controllers;

import be.ordina.msdashboard.cache.CacheCleaningBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.web.bind.annotation.RequestMethod.POST;

/**
 * @author Tim Ysewyn
 */
@CrossOrigin(maxAge = 3600)
@RestController
@ResponseBody
public class CacheController {

	private static final Logger logger = LoggerFactory.getLogger(CacheController.class);

	private final CacheCleaningBean cacheCleaningBean;

	public CacheController(CacheCleaningBean cacheCleaningBean) {
		this.cacheCleaningBean = cacheCleaningBean;
	}

	@RequestMapping(value = "/evictCache", method = POST)
	public void evictCache(){
		if (cacheCleaningBean != null) {
			logger.info("Cleaning cache");
			cacheCleaningBean.clean();
		}
	}

}
