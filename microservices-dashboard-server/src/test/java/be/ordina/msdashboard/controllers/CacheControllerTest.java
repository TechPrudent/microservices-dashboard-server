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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.verify;

/**
 * Tests for {@link GraphController}
 *
 * @author Tim Ysewyn
 */
@RunWith(MockitoJUnitRunner.class)
public class CacheControllerTest {

    @InjectMocks
    private CacheController cacheController;
    @Mock
    private CacheCleaningBean cacheCleaningBean;

    @Test
    public void shouldTriggerTheCacheCleaningBean() {
        cacheController.evictCache();

        verify(cacheCleaningBean).clean();
    }

}
