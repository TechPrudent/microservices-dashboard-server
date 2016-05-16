package be.ordina.msdashboard.aggregator.health;

import static com.google.common.collect.Collections2.*;
import static org.springframework.http.HttpMethod.GET;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import javax.servlet.http.HttpServletRequest;

import be.ordina.msdashboard.model.Node;
import be.ordina.msdashboard.model.Service;
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

public class SingleServiceHealthCollectorTask implements Callable<Node> {

	private static final Logger LOG = LoggerFactory.getLogger(SingleServiceHealthCollectorTask.class);
	private final String uriString;
	private final static String HEALTH = "health";
	private HealthToNodeConverter healthToNodeConverter;
	private DependenciesListFilterPredicate dependenciesListFilterPredicate;
	private ToolBoxDependenciesModifier toolBoxDependenciesModifier;

	public SingleServiceHealthCollectorTask(final Service service, final HttpServletRequest originRequest, final String managementContextPath) {
		uriString = buildHealthUri(service, managementContextPath);
		healthToNodeConverter = new HealthToNodeConverter();
		dependenciesListFilterPredicate = new DependenciesListFilterPredicate();
		toolBoxDependenciesModifier = new ToolBoxDependenciesModifier();
	}

	private String buildHealthUri(final Service service, final String managementContextPath) {
		Assert.notNull(service.getId());
		StringBuilder builder = new StringBuilder("http://");
		builder.append(service.getHost())
				.append(":")
				.append(service.getPort())
				.append("/")
				.append(service.getId())
				.append(managementContextPath!=null?managementContextPath:"")
				.append("/")
				.append(HEALTH);
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

		LOG.debug("Calling URI: {}", uriString);

		ResponseEntity<Map> responseRest = restTemplate.exchange(uriString,
				GET,
				new HttpEntity<Map>(headers),
				Map.class);
		Node node = healthToNodeConverter.convert(responseRest.getBody());
		Collection<Node> nodeCollection = node.getLinkedToNodes();
		nodeCollection = filter(nodeCollection, dependenciesListFilterPredicate);
		nodeCollection = toolBoxDependenciesModifier.modify(nodeCollection);
		node.setLinkedToNodes((Set) nodeCollection);
		if (LOG.isDebugEnabled()) {
			long totalTime = new DateTime().getMillis() - startTime;
			LOG.debug("Finished URI: {} Total time: {}", uriString, totalTime);
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
