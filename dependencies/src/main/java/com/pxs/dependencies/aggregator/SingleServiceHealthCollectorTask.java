package com.pxs.dependencies.aggregator;

import static org.springframework.http.HttpMethod.GET;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.servlet.http.HttpServletRequest;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Collections2;
import com.pxs.dependencies.model.Node;

public class SingleServiceHealthCollectorTask implements Callable<Node> {

	private static final Logger LOG = LoggerFactory.getLogger(SingleServiceHealthCollectorTask.class);
	private final String uriString;
	private final static String GATEWAY = "";
	private final static String HEALTH = "health";
	private MapToNodeConverter mapToNodeConverter;
	private DependenciesListFilterPredicate dependenciesListFilterPredicate;
	private ToolBoxDependenciesModifier toolBoxDependenciesModifier;

	public SingleServiceHealthCollectorTask(final String serviceId, final int gatewayPort, final String gatewayHost, final HttpServletRequest originRequest) {
		uriString = buildHealthUri(serviceId, gatewayPort, gatewayHost);
		mapToNodeConverter = new MapToNodeConverter();
		dependenciesListFilterPredicate = new DependenciesListFilterPredicate();
		toolBoxDependenciesModifier = new ToolBoxDependenciesModifier();
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
	public Node call() throws Exception {
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
		Collection<Node> nodeCollection = node.getLinkedNodes();
		nodeCollection = Collections2.filter(nodeCollection, dependenciesListFilterPredicate);
		nodeCollection = toolBoxDependenciesModifier.modify(nodeCollection);
		node.setLinkedNodes((List) nodeCollection);
		if (LOG.isDebugEnabled()) {
			long totalTime = new DateTime().getMillis() - startTime;
			LOG.debug("uri: {} total time: {}", uriString, totalTime);
		}
		return node;
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
}
