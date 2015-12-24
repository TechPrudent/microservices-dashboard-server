package com.pxs.dependencies.aggregator;

import static org.assertj.core.api.Assertions.assertThat;

import static com.pxs.dependencies.constants.Constants.GROUP;
import static com.pxs.dependencies.constants.Constants.MICROSERVICE;
import static com.pxs.dependencies.constants.Constants.NAME;
import static com.pxs.dependencies.constants.Constants.OWN_HEALTH;
import static com.pxs.dependencies.constants.Constants.TYPE;
import static com.pxs.dependencies.constants.Constants.VERSION;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.springframework.boot.actuate.health.Health;

import com.pxs.dependencies.model.Node;
import com.pxs.utilities.converters.json.JsonToObjectConverter;

public class VirtualDependenciesConverterTest {

	@Test
	public void testConvert() throws Exception {
		VirtualDependenciesConverter converter = new VirtualDependenciesConverter();
		Map<String, Node> input = generateInputMap();

		Map<String, Map<String, Object>> resutingMap = converter.convert(input);

		System.out.println(resutingMap);
		Map<String, Object> dependenciesMap = resutingMap.get("testservice");
		assertThat(dependenciesMap.keySet()).containsExactly(OWN_HEALTH, "db", "pptDeviceTechnicalDetails", "ngrpGetEligibleAwards", "pptAwards");
		Health ownHealth = (Health) dependenciesMap.get(OWN_HEALTH);
		assertThat(ownHealth.getStatus().toString()).isEqualTo("UNKNOWN");
		assertThat(ownHealth.getDetails().get(TYPE)).isEqualTo(MICROSERVICE);
		assertThat(ownHealth.getDetails().get(GROUP)).isEqualTo("BCI");
		assertThat(ownHealth.getDetails().get(NAME)).isEqualTo("testservice");
		assertThat(ownHealth.getDetails().get(VERSION)).isEqualTo("1.0.0");

	}

	private Map<String, Node> generateInputMap() {
		String nodeString = "{\"id\":\"testservice\",\"details\":{\"status\":\"UP\",\"type\":\"MICROSERVICE\",\"group\":\"BCI\",\"name\":\"testservice\",\"version\":\"1.0.0\"},\"lane\":2,\"linkedNodes\":[{\"id\":\"pptAwards\",\"details\":{\"status\":\"UNKNOWN\",\"type\":\"REST\",\"group\":\"PPT\"},\"lane\":3,\"linkedNodes\":null},{\"id\":\"ngrpGetEligibleAwards\",\"details\":{\"status\":\"UP\",\"version\":\"\",\"type\":\"SOAP\",\"group\":\"NGRP\"},\"lane\":3,\"linkedNodes\":null},{\"id\":\"pptDeviceTechnicalDetails\",\"details\":{\"status\":\"UNKNOWN\",\"type\":\"REST\",\"group\":\"PPT\"},\"lane\":3,\"linkedNodes\":null},{\"id\":\"db\",\"details\":{\"status\":\"UP\",\"sapcacheDataSource\":{\"status\":\"UP\",\"database\":\"Oracle\",\"hello\":\"Hello\"},\"pptDataSource\":{\"status\":\"UP\",\"database\":\"Microsoft SQL Server\",\"hello\":1},\"esrvDataSource\":{\"status\":\"UP\",\"database\":\"Microsoft SQL Server\",\"hello\":1}},\"lane\":3,\"linkedNodes\":null}]}";
		JsonToObjectConverter<Node> converter = new JsonToObjectConverter<>(Node.class);
		Node node = converter.convert(nodeString);
		Map<String, Node> nodeMap = new HashMap<>();
		nodeMap.put("testservice", node);
		return nodeMap;
	}
}