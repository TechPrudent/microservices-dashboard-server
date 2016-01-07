package com.pxs.dependencies.aggregator;

import static org.assertj.core.api.Assertions.assertThat;
import static com.pxs.dependencies.constants.Constants.*;
import static org.mockito.Mockito.doReturn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.boot.actuate.health.Health;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pxs.dependencies.services.RedisService;

@RunWith(MockitoJUnitRunner.class)
public class DependenciesGraphResourceJsonBuilderTest {

	@InjectMocks
	private DependenciesGraphResourceJsonBuilder dependenciesGraphResourceJsonBuilder;

	@Spy
	private ObjectMapper primaryObjectMapper;

	@Mock
	private HealthIndicatorsAggregator healthIndicatorsAggregator;

	@Mock
	private RedisService redisService;

	@Mock
	private VirtualDependenciesConverter virtualDependenciesConverter;

	private Health microserviceHealth;

	private Health backendHealth;

	private Health ownHealth;

	@Before
	public void init(){
		microserviceHealth = Health.unknown().withDetail("type", MICROSERVICE).build();
		backendHealth = Health.unknown().withDetail("type", "SOAP").build();
		ownHealth = Health.unknown().withDetail("type", MICROSERVICE).build();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testBuild() throws Exception {
		Map<String, Map<String, Object>> map = new HashMap<>();
		Map<String, Object> innermap1 = new HashMap<>();
		innermap1.put("1a", microserviceHealth);
		innermap1.put("1b", microserviceHealth);
		innermap1.put("1c", microserviceHealth);
		innermap1.put(OWN_HEALTH, ownHealth);
		Map<String, Object> innermap2 = new HashMap<>();
		innermap2.put("2a", backendHealth);
		innermap2.put("2b", backendHealth);
		innermap2.put("2c", backendHealth);
		innermap2.put(OWN_HEALTH, ownHealth);
		Map<String, Object> innermap3 = new HashMap<>();
		innermap3.put("3a", backendHealth);
		innermap3.put("3b", backendHealth);
		innermap3.put("3c", backendHealth);
		innermap3.put(OWN_HEALTH, ownHealth);
		map.put("key1", innermap1);
		map.put("key2", innermap2);
		map.put("key3", innermap3);
		doReturn(map).when(healthIndicatorsAggregator).fetchCombinedDependencies();

		Map<String, Object> returnedMap = dependenciesGraphResourceJsonBuilder.build();
		assertThat(((boolean) returnedMap.get("directed"))).isEqualTo(true);
		assertThat(((boolean) returnedMap.get("multigraph"))).isEqualTo(false);
		assertThat(((String[]) returnedMap.get("graph")).length).isEqualTo(0);

		List<Map<String, Object>> expectedNodeList = getExpectedNodesList();
		System.out.println(expectedNodeList);
		List<Map<String, String>> returnedNodeList = (List<Map<String, String>>) returnedMap.get("nodes");
		System.out.println(returnedNodeList);

		assertThat(CollectionUtils.isEqualCollection(expectedNodeList, returnedNodeList)).isTrue();

		List<Map<String, Integer>> expectedLinks = getExpectedLinks();
		List<Map<String, Integer>> returnedLinks = (List<Map<String, Integer>>) returnedMap.get("links");

		assertThat(CollectionUtils.isEqualCollection(expectedLinks, returnedLinks)).isTrue();

	}

	private List<Map<String, Object>> getExpectedNodesList() {
		List<Map<String, Object>> expectedNodeList = new ArrayList<>();
		Map<String, Object> node1 = new HashMap<>();
		node1.put(ID, "key3");
		node1.put(LANE, 2);
		node1.put(DETAILS, ownHealth);
		Map<String, Object> node2 = new HashMap<>();
		node2.put(ID, "3c");
		node2.put(LANE, 3);
		node2.put(DETAILS, backendHealth);
		Map<String, Object> node3 = new HashMap<>();
		node3.put(ID, "3b");
		node3.put(LANE, 3);
		node3.put(DETAILS, backendHealth);
		Map<String, Object> node4 = new HashMap<>();
		node4.put(ID, "3a");
		node4.put(LANE, 3);
		node4.put(DETAILS, backendHealth);
		Map<String, Object> node5 = new HashMap<>();
		node5.put(ID, "key2");
		node5.put(LANE, 2);
		node5.put(DETAILS, ownHealth);
		Map<String, Object> node6 = new HashMap<>();
		node6.put(ID, "2a");
		node6.put(LANE, 3);
		node6.put(DETAILS, backendHealth);
		Map<String, Object> node7 = new HashMap<>();
		node7.put(ID, "2c");
		node7.put(LANE, 3);
		node7.put(DETAILS, backendHealth);
		Map<String, Object> node8 = new HashMap<>();
		node8.put(ID, "2b");
		node8.put(LANE, 3);
		node8.put(DETAILS, backendHealth);
		Map<String, Object> node9 = new HashMap<>();
		node9.put(ID, "key1");
		node9.put(LANE, 2);
		node9.put(DETAILS, ownHealth);
		Map<String, Object> node10 = new HashMap<>();
		node10.put(ID, "1b");
		node10.put(LANE, 2);
		node10.put(DETAILS, microserviceHealth);
		Map<String, Object> node11 = new HashMap<>();
		node11.put(ID, "1a");
		node11.put(LANE, 2);
		node11.put(DETAILS, microserviceHealth);
		Map<String, Object> node12 = new HashMap<>();
		node12.put(ID, "1c");
		node12.put(LANE, 2);
		node12.put(DETAILS, microserviceHealth);
		expectedNodeList.add(node1);
		expectedNodeList.add(node2);
		expectedNodeList.add(node3);
		expectedNodeList.add(node4);
		expectedNodeList.add(node5);
		expectedNodeList.add(node6);
		expectedNodeList.add(node7);
		expectedNodeList.add(node8);
		expectedNodeList.add(node9);
		expectedNodeList.add(node10);
		expectedNodeList.add(node11);
		expectedNodeList.add(node12);
		return expectedNodeList;
	}

	private List<Map<String, Integer>> getExpectedLinks() {
		List<Map<String, Integer>> expectedLinks = new ArrayList<>();
		Map<String, Integer> link1 = new HashMap<>();
		link1.put("source", 0);
		link1.put("target", 1);
		Map<String, Integer> link2 = new HashMap<>();
		link2.put("source", 0);
		link2.put("target", 2);
		Map<String, Integer> link3 = new HashMap<>();
		link3.put("source", 0);
		link3.put("target", 3);
		Map<String, Integer> link4 = new HashMap<>();
		link4.put("source", 4);
		link4.put("target", 5);
		Map<String, Integer> link5 = new HashMap<>();
		link5.put("source", 4);
		link5.put("target", 6);
		Map<String, Integer> link6 = new HashMap<>();
		link6.put("source", 4);
		link6.put("target", 7);
		Map<String, Integer> link7 = new HashMap<>();
		link7.put("source", 8);
		link7.put("target", 9);
		Map<String, Integer> link8 = new HashMap<>();
		link8.put("source", 8);
		link8.put("target", 10);
		Map<String, Integer> link9 = new HashMap<>();
		link9.put("source", 8);
		link9.put("target", 11);
		expectedLinks.add(link1);
		expectedLinks.add(link2);
		expectedLinks.add(link3);
		expectedLinks.add(link4);
		expectedLinks.add(link5);
		expectedLinks.add(link6);
		expectedLinks.add(link7);
		expectedLinks.add(link8);
		expectedLinks.add(link9);
		return expectedLinks;
	}
}
