package com.pxs.dependencies.aggregator;

import static org.springframework.http.HttpMethod.GET;
import static com.pxs.dependencies.constants.Constants.MICROSERVICE;
import static com.pxs.dependencies.constants.Constants.*;

import static com.google.common.collect.Maps.filterEntries;
import static com.google.common.collect.Maps.transformEntries;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.servlet.http.HttpServletRequest;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import com.google.common.annotations.VisibleForTesting;
import com.pxs.dependencies.model.Node;

public class SingleServiceHealthCollectorTask implements Callable<Map<String, Object>> {

	private static final Logger LOG = LoggerFactory.getLogger(SingleServiceHealthCollectorTask.class);
	private final String uriString;
	private DependenciesPredicate dependenciesPredicate;
	private ToolboxDependenciesTransformer toolboxDependenciesTransformer;
	private MapToHealthConverter mapToHealthConverter;
	private final static String GATEWAY = "";
	private final static String HEALTH = "health";
	private MapToNodeConverter mapToNodeConverter;

	public SingleServiceHealthCollectorTask(final String serviceId, final int gatewayPort, final String gatewayHost, final HttpServletRequest originRequest) {
		uriString = buildHealthUri(serviceId, gatewayPort, gatewayHost);
		dependenciesPredicate = new DependenciesPredicate();
		toolboxDependenciesTransformer = new ToolboxDependenciesTransformer();
		mapToHealthConverter = new MapToHealthConverter();
		mapToNodeConverter = new MapToNodeConverter();
	}

	private String buildHealthUri(final String serviceId, final int gatewayPort, final String gatewayHost) {
		Assert.notNull(serviceId);
		StringBuilder builder = new StringBuilder("http://");
		builder.append(gatewayHost)
				.append(":").append(gatewayPort)
				.append("/").append(GATEWAY)
				.append(serviceId)
				.append("/").append(HEALTH);
		return builder.toString();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Map<String, Object> call() throws Exception {
		long startTime = 0;
		if (LOG.isDebugEnabled()) {
			startTime = new DateTime().getMillis();
		}
		RestTemplate restTemplate = getRestTemplate();
		HttpHeaders headers = new HttpHeaders();
		headers.add("globalId", "abc");

		ResponseEntity<Map> responseRest = restTemplate.exchange(uriString,
				GET,
				new HttpEntity<Map>(headers),
				Map.class);
		Node node = mapToNodeConverter.convert(responseRest.getBody());

		Map<String, Object> nodeMap = node.getDetails();
		nodeMap = filterEntries(nodeMap, dependenciesPredicate);
		nodeMap = transformEntries(nodeMap, toolboxDependenciesTransformer);

		if (LOG.isDebugEnabled()) {
			long totalTime = new DateTime().getMillis() - startTime;
			LOG.debug("uri: {} total time: {}", uriString, totalTime);
		}
		System.out.println("________________________" + nodeMap);
		return nodeMap;
	}

	@VisibleForTesting
	RestTemplate getRestTemplate() {
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.setErrorHandler(new DefaultResponseErrorHandler() {
			@Override
			protected boolean hasError(final HttpStatus statusCode) {
				if (HttpStatus.SERVICE_UNAVAILABLE.equals(statusCode)) {
					return false;
				} else {
					return (statusCode.series() == HttpStatus.Series.CLIENT_ERROR ||
							statusCode.series() == HttpStatus.Series.SERVER_ERROR);
				}
			}
		});
		return restTemplate;
	}

//	private void addOwnHealth(final Map<String, Object> healthMap, final Health health) {
//		Health.Builder ownHealth = Health.status(health.getStatus());
//		if (health.getDetails().get(VERSION) != null) {
//			ownHealth.withDetail(VERSION, health.getDetails().get(VERSION));
//		}
//		ownHealth.withDetail(TYPE, health.getDetails().get(TYPE) != null ? health.getDetails().get(TYPE) : MICROSERVICE);
//		if (health.getDetails().get(NAME) != null) {
//			ownHealth.withDetail(NAME, health.getDetails().get(NAME));
//		}
//		if (health.getDetails().get(GROUP) != null) {
//			ownHealth.withDetail(GROUP, health.getDetails().get(GROUP));
//		}
//		healthMap.put(OWN_HEALTH, ownHealth.build());
//	}
//
//	private Map<String, Object> copyMap(Map<String, Object> healthMap) {
//		Map<String, Object> copy = new HashMap<>();
//		for (Map.Entry<String, Object> entry : healthMap.entrySet()) {
//			copy.put(entry.getKey(), entry.getValue());
//		}
//		return copy;
//	}
}
