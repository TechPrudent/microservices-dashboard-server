/*
 * Copyright 2012-2017 the original author or authors.
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
 *
 */

package be.ordina.msdashboard.security.strategy;

import org.junit.Before;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationContext;

import java.util.HashMap;
import java.util.Map;

public class SecuritySecurityStrategyFactoryTest {

	@Mock
	private ApplicationContext applicationContext;

//	@InjectMocks
//	private final SecurityStrategyFactory factory = new SecurityStrategyFactory(applicationContext);

	private Map<String, Object> annotatedBeans = new HashMap<>();

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);

//		annotatedBeans.put("bean_auth", new AuthTestStrategy());
//		annotatedBeans.put("bean_none", new DefaultTestStrategy());
	}

//	@Test
//	public void shouldGetAuthStrategy() {
//		when(applicationContext.getBeansWithAnnotation(SecurityStrategy.class)).thenReturn(annotatedBeans);
//
//		factory.init();
//
//		TestStrategy strategy = factory.getStrategy(TestStrategy.class, StrategyType.FORWARD_INBOUND_AUTH_HEADER);
//		assertEquals("SecurityStrategy returned was for the wrong profile", strategy.getClass(), AuthTestStrategy.class);
//	}
//
//	@Test
//	public void shouldGetDefaultStrategy() {
//		when(applicationContext.getBeansWithAnnotation(SecurityStrategy.class)).thenReturn(annotatedBeans);
//
//		factory.init();
//
//		TestStrategy strategy = factory.getStrategy(TestStrategy.class, StrategyType.NONE);
//		assertEquals("SecurityStrategy returned was for the wrong profile", strategy.getClass(), DefaultTestStrategy.class);
//	}
//
//
//	private interface TestStrategy {
//	}
//
//	@SecurityStrategy(type = TestStrategy.class, strategyType = StrategyType.FORWARD_INBOUND_AUTH_HEADER)
//	private static class AuthTestStrategy implements TestStrategy {
//	}
//
//	@SecurityStrategy(type = TestStrategy.class, strategyType = StrategyType.NONE)
//	private static class DefaultTestStrategy implements TestStrategy {
//	}
}
