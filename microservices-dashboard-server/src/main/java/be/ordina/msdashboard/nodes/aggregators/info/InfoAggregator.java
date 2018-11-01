package be.ordina.msdashboard.nodes.aggregators.info;


import be.ordina.msdashboard.nodes.aggregators.ErrorHandler;
import be.ordina.msdashboard.nodes.aggregators.NettyServiceCaller;
import be.ordina.msdashboard.nodes.aggregators.health.HealthProperties;
import be.ordina.msdashboard.nodes.aggregators.health.HealthToNodeConverter;
import be.ordina.msdashboard.nodes.model.Node;
import be.ordina.msdashboard.nodes.uriresolvers.UriResolver;
import be.ordina.msdashboard.security.outbound.SecurityStrategyFactory;
import io.netty.buffer.ByteBuf;
import io.reactivex.netty.protocol.http.client.HttpClientRequest;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Flux;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;
import rx.Observable;
import rx.schedulers.Schedulers;

import java.util.List;
import java.util.Map;

import static be.ordina.msdashboard.nodes.aggregators.Constants.ZUUL;

public class InfoAggregator {

	private static final Logger logger = LoggerFactory.getLogger(InfoAggregator.class);
	private static final String AGGREGATOR_KEY = "info";
	private DiscoveryClient discoveryClient;

	private UriResolver uriResolver;
	private HealthProperties properties;
//	private NettyServiceCaller caller;
	private ErrorHandler errorHandler;
	private HealthToNodeConverter healthToNodeConverter;
	private SecurityStrategyFactory securityStrategyFactory;
	private ApplicationEventPublisher publisher;

	@Deprecated
	public InfoAggregator(final DiscoveryClient discoveryClient, final UriResolver uriResolver,
						  final HealthProperties properties, final NettyServiceCaller caller,
						  final ErrorHandler errorHandler, final HealthToNodeConverter healthToNodeConverter, ApplicationEventPublisher publisher) {
		this.discoveryClient = discoveryClient;
		this.uriResolver = uriResolver;
		this.properties = properties;
//		this.caller = caller;
		this.errorHandler = errorHandler;
		this.healthToNodeConverter = healthToNodeConverter;
		this.publisher = publisher;
	}

	public InfoAggregator(final DiscoveryClient discoveryClient, final UriResolver uriResolver,
						  final HealthProperties properties, final NettyServiceCaller caller,
						  final ErrorHandler errorHandler, final HealthToNodeConverter healthToNodeConverter,
						  final SecurityStrategyFactory securityStrategyFactory, ApplicationEventPublisher publisher) {
		this(discoveryClient, uriResolver, properties, caller, errorHandler, healthToNodeConverter, publisher);
		this.securityStrategyFactory = securityStrategyFactory;
	}

	public void aggregateNodes() {
//		final Object outboundSecurityObject = getOutboundSecurityObject();
//		getServiceIdsFromDiscoveryClient()
//				.map(id -> new ImmutablePair<>(id, resolveHealthCheckUrl(id)))
//				.doOnNext(pair -> logger.info("Creating health observable: " + pair))
//				.map(pair -> outboundSecurityObject != null ?
//						getHealthNodesFromService(pair.getLeft(), pair.getRight(), outboundSecurityObject) :
//						getHealthNodesFromService(pair.getLeft(), pair.getRight())
//				)
//				.doOnNext(el -> logger.debug("Unmerged health observable: " + el))
//				.doOnError(e -> errorHandler.handleSystemError("Error filtering services: " + e.getMessage(), e))
//				.doOnComplete(() -> logger.info("Completed getting all health observables"))
//				.retry()
//				.flatMap(node -> node)
//				.forEach(node -> publisher.publishEvent(node));

		final Object outboundSecurityObject = getOutboundSecurityObject();

		getServiceIdsFromDiscoveryClient()
				.map(serviceId -> Tuples.of(serviceId, resolveInfoUrl(serviceId)))
				.doOnNext(pair -> logger.info("Creating info Flux: " + pair))
				.flatMap(pair -> outboundSecurityObject != null ?
						getInfoNodesFromService(pair.getT1(), pair.getT2(), outboundSecurityObject) :
						getInfoNodesFromService(pair.getT1(), pair.getT2())
				)
				.doOnNext(el -> logger.debug("Unmerged health observable: " + el))
				.doOnError(e -> errorHandler.handleSystemError("Error filtering services: " + e.getMessage(), e))
				.doOnComplete(() -> logger.info("Completed getting all health observables"))
				.retry()
				.subscribe(publisher::publishEvent);
	}

	private String resolveInfoUrl(String id) {
		List<ServiceInstance> instances = discoveryClient.getInstances(id);
		if (instances.isEmpty()) {
			throw new IllegalStateException("No instances found for service " + id);
		} else {
			return uriResolver.resolveInfoUrl(instances.get(0));
		}
	}

	protected Flux<String> getServiceIdsFromDiscoveryClient() {
		logger.info("Discovering services for info");
		return Flux.fromIterable(discoveryClient.getServices())
				.map(id -> id.toLowerCase())
				.filter(id -> !id.equals(ZUUL))
				.doOnNext(s -> logger.debug("Service discovered: " + s))
				.doOnError(e -> errorHandler.handleSystemError("Error filtering services: " + e.getMessage(), e))
				.retry();
	}

	protected Flux<Node> getInfoNodesFromService(String serviceId, String url) {
		return getInfoNodesFromService(serviceId, url, null);
	}

	protected Flux<Node> getInfoNodesFromService(String serviceId, String url, final Object outboundSecurityObject) {
		HttpClientRequest<ByteBuf> request = HttpClientRequest.createGet(url);
		applyOutboundSecurityStrategyOnRequest(request, outboundSecurityObject);
		for (Map.Entry<String, String> header : properties.getRequestHeaders().entrySet()) {
			request.withHeader(header.getKey(), header.getValue());
		}

        return WebClient
                .create()
                .get()
                .uri(url)
                .retrieve()
                .bodyToFlux(Node.class);


//		return caller.retrieveJsonFromRequest(serviceId, request)
//				.flatMap(el -> healthToNodeConverter.convertToNodes(serviceId, el))
//				.filter(node -> !properties.getFilteredServices().contains(node.getId()))
//				//TODO: .map(node -> springCloudEnricher.enrich(node))
//				.doOnNext(el -> logger.info("Health node {} discovered in url: {}", el.getId(), url))
//				.doOnError(e -> logger.error("Error during healthnode fetching: ", e))
//				.doOnCompleted(() -> logger.info("Completed emission of a health node observable from url: " + url))
//				.onErrorResumeNext(Observable.empty());
	}


	private Object getOutboundSecurityObject() {
		if (securityStrategyFactory != null) {
			return securityStrategyFactory.getStrategy(AGGREGATOR_KEY).getOutboundSecurityObjectProvider().getOutboundSecurityObject();
		} else {
			return null;
		}
	}

	private void applyOutboundSecurityStrategyOnRequest(HttpClientRequest<ByteBuf> request, Object outboundSecurityObject) {
		if (outboundSecurityObject != null) {
			securityStrategyFactory.getStrategy(AGGREGATOR_KEY).getOutboundSecurityStrategy().apply(request, outboundSecurityObject);
		}
	}
}
