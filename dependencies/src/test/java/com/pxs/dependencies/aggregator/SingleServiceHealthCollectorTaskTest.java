package com.pxs.dependencies.aggregator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.boot.actuate.health.Health.unknown;
import static org.springframework.boot.actuate.health.Health.up;

import static com.google.common.collect.Lists.newArrayList;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.boot.actuate.health.Health;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import static com.pxs.dependencies.constants.Constants.*;

@RunWith(MockitoJUnitRunner.class)
public class SingleServiceHealthCollectorTaskTest {

	@InjectMocks
	@Spy
	private SingleServiceHealthCollectorTask task = new SingleServiceHealthCollectorTask("", 10, "", null);

	@Mock
	private ResponseEntity<Map> response;
	@Mock
	private MapToHealthConverter mapToHealthConverter;
	private RestTemplate restTemplate;

	@Before
	public void onSetUp() throws URISyntaxException {
		restTemplate = mock(RestTemplate.class, RETURNS_DEEP_STUBS);
		when(task.getRestTemplate()).thenReturn(restTemplate);
		when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Map.class))).thenReturn(response);
		when(response.getBody()).thenReturn(new HashMap());
		when(mapToHealthConverter.convert(any(Map.class))).thenReturn(mockResponseEntity());
	}

	private Health mockResponseEntity() {
		Health body = up()
				.withDetail("bciManageCustomerContact", up()
						.withDetail(VERSION, "4.0")
						.withDetail(TYPE, "SOAP")
						.withDetail(GROUP, "BCI")
						.build())
				.withDetail("cslCustomer", up()
						.withDetail(VERSION, "Customer 4.0 (since 2012 CRC03)")
						.withDetail(TYPE, "SOAP")
						.withDetail(GROUP, "CSL")
						.build())
				.withDetail("cslEmployee", unknown()
						.withDetail(VERSION, "4.0")
						.withDetail(TYPE, "SOAP")
						.withDetail(GROUP, "CSL")
						.build())
				.withDetail("imaIMAServices", up()
						.withDetail(VERSION, "IMA SERVICES - Service Version 1.1")
						.withDetail(TYPE, "SOAP")
						.withDetail(GROUP, "IMA")
						.build())
				.withDetail("bciManageCustomerContact", up()
						.withDetail(VERSION, "4.0")
						.withDetail(TYPE, "SOAP")
						.withDetail(GROUP, "BCI")
						.build())
				.withDetail("discovery", up()
						.withDetail("description", "Spring Cloud Eureka Discovery Client")
						.withDetail("discoveryClient", up()
								.withDetail("description", "Spring Cloud Eureka Discovery Client")
								.withDetail("services", new String[0])
								.build())
						.build())
				.withDetail("diskSpace", up()
						.withDetail("free", 165026881536L)
						.withDetail("threshold", 10485760)
						.build())
				.withDetail("db", up()
						.withDetail("buscDataSource", up()
								.withDetail("database", "Oracle")
								.withDetail("hello", "hello")
								.build())
						.withDetail("pdbDataSource", up()
								.withDetail("database", "Oracle")
								.withDetail("hello", "hello")
								.build())
						.build())
				.withDetail("configServer", up()
						.withDetail("propertySources", newArrayList("applicationConfig: [classpath:/application-loc-jboss.yml]",
								"applicationConfig: [classpath:/application.yml]").toArray())
						.build())
				.withDetail("hystrix", up().build())
				.build();
		return body;
	}

	@Test
	public void unmarshallsHealths() throws Exception {
		Map<String, Object> health = task.call();
		assertThat(health.keySet()).contains(OWN_HEALTH,"bciManageCustomerContact",
				"cslCustomer", "cslEmployee", "imaIMAServices", "discovery", "db", "configServer");
		assertThat(((Health) health.get("configServer")).getDetails().keySet())
				.containsExactly("propertySources", TYPE, GROUP);
		assertThat(((Health) health.get(OWN_HEALTH)).getStatus().toString()).isEqualTo("UP");
	}
}
