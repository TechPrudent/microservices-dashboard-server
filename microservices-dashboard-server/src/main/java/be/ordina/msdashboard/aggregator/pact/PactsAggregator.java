package be.ordina.msdashboard.aggregator.pact;

import be.ordina.msdashboard.aggregator.PactBrokerBasedAggregator;
import be.ordina.msdashboard.constants.Constants;
import be.ordina.msdashboard.model.Node;
import be.ordina.msdashboard.model.NodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.concurrent.*;

public class PactsAggregator extends PactBrokerBasedAggregator<Node> {

	private static final Logger LOG = LoggerFactory.getLogger(PactsAggregator.class);

	private static final long TIMEOUT = 17000L;

	@Cacheable(value = Constants.PACTS_CACHE_NAME, keyGenerator = "simpleKeyGenerator")
	public Node fetchPactNodes() {
		NodeBuilder pactNode = new NodeBuilder();
		for (FutureTask<Node> task : getFutureTasks()) {
			String key = null;
			try {
				key = ((IdentifiableFutureTask) task).getId();
				Node value = task.get(TIMEOUT, TimeUnit.MILLISECONDS);
				LOG.debug("Task {} is done: {}", key, task.isDone());
				pactNode.withLinkedNode(value);
			} catch (InterruptedException | ExecutionException | TimeoutException e) {
				LOG.warn("Problem getting results for task: {} caused by: {}", key, e.toString());
			}
		}
		LOG.debug("Finished fetching pacts");
		return pactNode.build();
	}

	@Override
	protected Callable<Node> instantiateAggregatorTask(String pactUrl) {
		return new SinglePactCollectorTask(pactUrl);
	}
}
