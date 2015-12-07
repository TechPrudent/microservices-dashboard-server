package com.pxs.dependencies.aggregator;

import static java.util.Arrays.asList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(MockitoJUnitRunner.class)
public class DependenciesGraphResourceJsonBuilderTest {

	@InjectMocks
	private DependenciesGraphResourceJsonBuilder dependenciesGraphResourceJsonBuilder;

	@Spy
	private ObjectMapper primaryObjectMapper;

	@Mock
	private HealthIndicatorsAggregator healthIndicatorsAggregator;

	@SuppressWarnings("unchecked")
	@Test
	public void testBuild() throws Exception {
		Map<String, Map<String, Object>> map = new HashMap<>();
		Map<String, Object> innermap1 = new HashMap<>();
		innermap1.put("1a", null);
		innermap1.put("1b", null);
		innermap1.put("1c", null);
		Map<String, Object> innermap2 = new HashMap<>();
		innermap2.put("2a", null);
		innermap2.put("2b", null);
		innermap2.put("2c", null);
		Map<String, Object> innermap3 = new HashMap<>();
		innermap3.put("3a", null);
		innermap3.put("3b", null);
		innermap3.put("3c", null);
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
		node1.put("id", "key3");
		node1.put("lane", 2);
		node1.put("details", null);
		Map<String, Object> node2 = new HashMap<>();
		node2.put("id", "3c");
		node2.put("lane", 3);
		node2.put("details", null);
		Map<String, Object> node3 = new HashMap<>();
		node3.put("id", "3b");
		node3.put("lane", 3);
		node3.put("details", null);
		Map<String, Object> node4 = new HashMap<>();
		node4.put("id", "3a");
		node4.put("lane", 3);
		node4.put("details", null);
		Map<String, Object> node5 = new HashMap<>();
		node5.put("id", "key2");
		node5.put("lane", 2);
		node5.put("details", null);
		Map<String, Object> node6 = new HashMap<>();
		node6.put("id", "2a");
		node6.put("lane", 3);
		node6.put("details", null);
		Map<String, Object> node7 = new HashMap<>();
		node7.put("id", "2c");
		node7.put("lane", 3);
		node7.put("details", null);
		Map<String, Object> node8 = new HashMap<>();
		node8.put("id", "2b");
		node8.put("lane", 3);
		node8.put("details", null);
		Map<String, Object> node9 = new HashMap<>();
		node9.put("id", "key1");
		node9.put("lane", 2);
		node9.put("details", null);
		Map<String, Object> node10 = new HashMap<>();
		node10.put("id", "1b");
		node10.put("lane", 3);
		node10.put("details", null);
		Map<String, Object> node11 = new HashMap<>();
		node11.put("id", "1a");
		node11.put("lane", 3);
		node11.put("details", null);
		Map<String, Object> node12 = new HashMap<>();
		node12.put("id", "1c");
		node12.put("lane", 3);
		node12.put("details", null);
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
