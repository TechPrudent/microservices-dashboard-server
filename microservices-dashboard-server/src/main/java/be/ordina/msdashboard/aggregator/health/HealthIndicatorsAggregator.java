package be.ordina.msdashboard.aggregator.health;

import be.ordina.msdashboard.aggregator.EurekaBasedAggregator;
import be.ordina.msdashboard.aggregator.ObservablesToGraphConverter;
import be.ordina.msdashboard.constants.Constants;
import be.ordina.msdashboard.model.Node;
import be.ordina.msdashboard.model.NodeBuilder;
import be.ordina.msdashboard.model.Service;
import com.jayway.jsonpath.JsonPath;
import io.netty.buffer.ByteBuf;
import io.reactivex.netty.RxNetty;
import io.reactivex.netty.protocol.http.AbstractHttpContentHolder;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.json.JacksonJsonParser;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.core.env.Environment;
import rx.Observable;
import rx.Single;
import rx.functions.Func0;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.*;

import static be.ordina.msdashboard.constants.Constants.*;
import static com.google.common.collect.Collections2.filter;

public class HealthIndicatorsAggregator extends EurekaBasedAggregator<Node> {

	private static final Logger LOG = LoggerFactory.getLogger(HealthIndicatorsAggregator.class);

	private static final long TIMEOUT = 17000L;

	private Environment environment;

	private DiscoveryClient discoveryClient;

	private DependenciesListFilterPredicate dependenciesListFilterPredicate;

	private ToolBoxDependenciesModifier toolBoxDependenciesModifier;

	public HealthIndicatorsAggregator(Environment environment, DiscoveryClient discoveryClient) {
		this.environment = environment;
		this.discoveryClient = discoveryClient;
		dependenciesListFilterPredicate = new DependenciesListFilterPredicate();
		toolBoxDependenciesModifier = new ToolBoxDependenciesModifier();
	}

	@Cacheable(value = Constants.HEALTH_CACHE_NAME, keyGenerator = "simpleKeyGenerator")
	public Node fetchCombinedDependencies() {
		Node taskResponses = buildAggregatedDependenciesFromTaskResponses(getFutureTasks());
		return taskResponses;
	}

	private Node buildAggregatedDependenciesFromTaskResponses(final List<FutureTask<Node>> tasks) {
		NodeBuilder nodeBuilder = NodeBuilder.node();
		for (FutureTask<Node> task : tasks) {
			String key = null;
			try {
				key = ((IdentifiableFutureTask) task).getId();
				Node value = task.get(TIMEOUT, TimeUnit.MILLISECONDS);
				LOG.debug("Task {} is done: {}", key, task.isDone());
				value.setId(key);
				nodeBuilder.withLinkedToNode(value);
			} catch (InterruptedException | ExecutionException | TimeoutException e) {
				LOG.warn("Problem getting results for task: {} caused by: {}", key, e.toString());
			}
		}
		LOG.debug("Finished fetching combined dependencies");
		return nodeBuilder.build();
	}

	@Override
	protected Callable<Node> instantiateAggregatorTask(final HttpServletRequest originRequest, final Service service) {
		return new SingleServiceHealthCollectorTask(service, originRequest, environment.getProperty("management.context-path"));
	}

	// REACTIVE WAY

	//@Cacheable(value = Constants.HEALTH_CACHE_NAME, keyGenerator = "simpleKeyGenerator")
	public Observable<Node> fetchCombinedDependenciesAsObservable() {
		Observable<Observable<Node>> observableObservable = getServiceIdsFromDiscoveryClient()
				.map(id -> new ImmutablePair<String, String>(id, discoveryClient.getInstances(id).get(0).getUri().toString() + "/health"))
//				.doOnNext(el -> {
//					LOG.info("Health url discovered: " + el);
//					try {
//						LOG.info("Sleeping now for 10 seconds");
//						Thread.sleep(10000);
//					} catch (InterruptedException e) {
//						e.printStackTrace();
//					}
//				})
				.doOnNext(pair -> LOG.info("Creating health observable: " + pair))
				.map(pair -> getHealthNodesFromService(pair.getLeft(), pair.getRight()))
				/*.flatMap(el -> el, throwable -> {
                    System.out.println("EXCEPTION: " + throwable);
                    return null;
                }, () -> Observable.just(new Node("endNode")))*/
				.filter(Objects::nonNull)
				.doOnNext(el -> LOG.info("Unmerged health observable: " + el))
				.doOnCompleted(() -> LOG.info("Completed getting all health observables"));
		return Observable.merge(observableObservable)
				.doOnNext(el -> LOG.info("Merged health node! " + el.getId()))
				.doOnCompleted(() -> LOG.info("Completed merging all health observables"));
	}

	private Observable<String> getServiceIdsFromDiscoveryClient() {
		LOG.info("Discovering services for health");
		Observable<String> serviceIds = Observable.from(discoveryClient.getServices()).subscribeOn(Schedulers.io());
		serviceIds.filter(id -> !id.equals(ZUUL_ID));
		return serviceIds;
	}

	private Observable<Node> getHealthNodesFromService(String serviceId, String url) {
		return RxNetty.createHttpGet(url)
				.filter(r -> {
					if (r.getStatus().code() < 400) {
						return true;
					} else {
						LOG.warn("Exception {} for call {} with headers {}", r.getStatus(), url, r.getHeaders().entries());
						return false;
					}
				})
				.flatMap(AbstractHttpContentHolder::getContent)
				.map(data -> data.toString(Charset.defaultCharset()))
				.map(response -> {
					JacksonJsonParser jsonParser = new JacksonJsonParser();
					return jsonParser.parseMap(response);
				})
				.map((source) -> HealthToNodeConverter.convertToNodes(serviceId, source))
				.flatMap(el -> el)
				.filter(node -> !HYSTRIX.equals(node.getId()) && !DISK_SPACE.equals(node.getId())
						&& !DISCOVERY.equals(node.getId()) && !CONFIGSERVER.equals(node.getId()))
				//.map(node -> toolBoxDependenciesModifier.modify(node))
				.doOnNext(el -> LOG.info("Health node discovered in url: " + url))
				.doOnNext(el -> LOG.info("Node added: " + el.getId()))
				.doOnCompleted(() -> LOG.info("Completed emission of a health node observable from url: " + url));
	}


}
