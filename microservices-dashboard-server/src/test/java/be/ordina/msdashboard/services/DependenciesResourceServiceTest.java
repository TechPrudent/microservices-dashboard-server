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
package be.ordina.msdashboard.services;

import be.ordina.msdashboard.aggregators.health.HealthIndicatorsAggregator;
import be.ordina.msdashboard.aggregators.index.IndexesAggregator;
import be.ordina.msdashboard.aggregators.pact.PactsAggregator;
import be.ordina.msdashboard.converters.ObjectToJsonConverter;
import be.ordina.msdashboard.stores.NodeStore;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import rx.Observable;

import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Map;

import static be.ordina.msdashboard.JsonHelper.load;
import static be.ordina.msdashboard.JsonHelper.removeBlankNodes;
import static be.ordina.msdashboard.model.NodeBuilder.node;
import static com.google.common.collect.Sets.newHashSet;

/**
 * @author Andreas Evers
 * @author Tim Ysewyn
 */
@RunWith(MockitoJUnitRunner.class)
public class DependenciesResourceServiceTest {

	private DependenciesResourceService dependenciesResourceService;

	@Mock
	private HealthIndicatorsAggregator healthIndicatorsAggregator;
	@Mock
	private IndexesAggregator indexesAggregator;
	@Mock
	private PactsAggregator pactsAggregator;
	@Mock
	private NodeStore redisService;

	@Before
	public void setup() {
		dependenciesResourceService = new DependenciesResourceService(Arrays.asList(healthIndicatorsAggregator, indexesAggregator, pactsAggregator), redisService);
	}

	@Test
	public void retrieveGraph() throws FileNotFoundException {
		Mockito.when(healthIndicatorsAggregator.aggregateNodes())
				.thenReturn(Observable.from(newHashSet(
						node().withId("service1").havingLinkedToNodeIds(newHashSet("backend1")).build(),
						node().withId("service2").havingLinkedToNodeIds(newHashSet("backend2")).build(),
						node().withId("backend1").havingLinkedFromNodeIds(newHashSet("service1")).build(),
						node().withId("backend2").havingLinkedFromNodeIds(newHashSet("service2")).build())));
		Mockito.when(indexesAggregator.aggregateNodes())
				.thenReturn(Observable.from(newHashSet(
						node().withId("svc1rsc1").havingLinkedToNodeIds(newHashSet("service1")).build(),
						node().withId("svc1rsc2").havingLinkedToNodeIds(newHashSet("service1")).build(),
						node().withId("svc2rsc1").havingLinkedToNodeIds(newHashSet("service2")).build(),
						node().withId("service1").withDetail("test", "test").build(),
						node().withId("service2").build())));
		Mockito.when(pactsAggregator.aggregateNodes())
				.thenReturn(Observable.from(newHashSet(
						node().withId("svc1rsc2").build(),
						node().withId("svc2rsc1").build(),
						node().withId("service1").havingLinkedToNodeIds(newHashSet("svc2rsc1")).build(),
						node().withId("service2").havingLinkedToNodeIds(newHashSet("svc1rsc2")).build())));
		Mockito.when(redisService.getAllNodes())
				.thenReturn(newHashSet());

		long startTime = System.currentTimeMillis();
		Map<String, Object> graph = dependenciesResourceService.getDependenciesGraphResourceJson();
		long totalTime = System.currentTimeMillis() - startTime;

		System.out.println("Time spent waiting for processing: " + totalTime);
		ObjectToJsonConverter<Map<String, Object>> converter = new ObjectToJsonConverter<>();
		String nodeAsJson = converter.convert(graph);
		JSONAssert.assertEquals(removeBlankNodes(load("src/test/resources/DependenciesResourceServiceTest.json")),
				removeBlankNodes(nodeAsJson), JSONCompareMode.LENIENT);
	}
}