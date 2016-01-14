package com.pxs.dependencies.aggregator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
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
import org.springframework.boot.actuate.health.Health;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pxs.dependencies.model.Node;
import com.pxs.utilities.converters.json.JsonToObjectConverter;
import com.pxs.utilities.converters.json.ObjectToJsonConverter;

import progress.message.util.ListNode;

@Component
public class HealthIndicatorsAggregator extends AbstractAggregator<Node> {

	private static final Logger LOG = LoggerFactory.getLogger(HealthIndicatorsAggregator.class);

	private static final long TIMEOUT = 17000L;

	public List<Node> fetchCombinedDependencies() {
		List<Node> taskResponses = buildAggregatedDependenciesListFromTaskResponses(getFutureTasks());
		System.out.println("aaaaaaaaaaaaaaaaaaaaaaaaaa" + taskResponses);
		return taskResponses;
	}

	private List<Node> buildAggregatedDependenciesListFromTaskResponses(final List<FutureTask<Node>> tasks) {
		List<Node> nodes = new ArrayList<>();
		for (FutureTask<Node> task : tasks) {
			String key = null;
			try {
				key = ((IdentifiableFutureTask) task).getId();
				Node value = task.get(TIMEOUT, TimeUnit.MILLISECONDS);
				LOG.debug("task {} is done {}", key, task.isDone());
				value.setId(key);
				nodes.add(value);
			} catch (InterruptedException | ExecutionException | TimeoutException e) {
				LOG.debug("Problem getting results for task: {} caused by: {}", key, e.toString());
			}
		}
		LOG.debug("Finished fetching combined dependencies");
		String serializeResponse = serializeResponse(nodes);
		System.out.println(serializeResponse);

		System.out.println(deserializeResponse(serializeResponse));

		return nodes;
	}

	@Override
	protected Callable<Node> instantiateAggregatorTask(final HttpServletRequest originRequest, final String serviceId, final String serviceHost, final int servicePort) {
		return new SingleServiceHealthCollectorTask(serviceId, servicePort, serviceHost, originRequest);
	}

	private String serializeResponse(List<Node> nodes) {
		ObjectToJsonConverter<List<Node>> serializer = new ObjectToJsonConverter<>();
		return serializer.convert(nodes);
	}

	private List<Node> deserializeResponse(final String json) {
		ObjectMapper mapper = new ObjectMapper();
		try {
			List<Node> deserializedList = mapper.readValue(json, new TypeReference<List<Node>>() {
			});
			return deserializedList;
		} catch (IOException e) {
			throw new IllegalArgumentException(json, e);
		}
	}
}
