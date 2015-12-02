package com.pxs.dependencies.aggregator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class HealthIndicatorsAggregator extends AbstractAggregator<Map<String, Object>> {

	private static final Logger LOG = LoggerFactory.getLogger(HealthIndicatorsAggregator.class);

	private static final long TIMEOUT = 17000L;

	public Map<String, Map<String, Object>> fetchCombinedDependencies() {
		return buildAggregatedDependenciesListFromTaskResponses(getFutureTasks());
	}

	private Map<String, Map<String, Object>> buildAggregatedDependenciesListFromTaskResponses(final List<FutureTask<Map<String, Object>>> tasks) {

		final Map<String, Map<String, Object>> aggregatedDependencies = new HashMap<>();

		for (FutureTask<Map<String, Object>> task : tasks) {
			String key = null;
			try {
				key = ((IdentifiableFutureTask) task).getId();
				Map<String, Object> value = task.get(TIMEOUT, TimeUnit.MILLISECONDS);
				LOG.debug("task {} is done {}", key, task.isDone());
				aggregatedDependencies.put(key, value);
			} catch (InterruptedException | ExecutionException | TimeoutException e) {
				LOG.debug("Problem getting results for task: {} caused by: {}", key, e.toString());
			}
		}
		LOG.debug("Finished fetching combined dependencies");
		return aggregatedDependencies;
	}

	@Override
	protected Callable<Map<String, Object>> instantiateAggregatorTask(final HttpServletRequest originRequest, final String serviceId, final String serviceHost, final int servicePort) {
		return new SingleServiceHealthCollectorTask(serviceId, servicePort, serviceHost, originRequest);
	}

}
