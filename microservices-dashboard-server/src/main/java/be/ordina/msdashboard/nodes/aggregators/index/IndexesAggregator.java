/*
 * Copyright 2012-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package be.ordina.msdashboard.nodes.aggregators.index;

import be.ordina.msdashboard.nodes.aggregators.NettyServiceCaller;
import be.ordina.msdashboard.nodes.aggregators.NodeAggregator;
import be.ordina.msdashboard.nodes.model.Node;
import be.ordina.msdashboard.nodes.model.NodeEvent;
import be.ordina.msdashboard.nodes.model.SystemEvent;
import be.ordina.msdashboard.nodes.uriresolvers.UriResolver;
import be.ordina.msdashboard.security.strategies.SecurityProtocolStrategy;
import be.ordina.msdashboard.security.strategies.StrategyFactory;
import be.ordina.msdashboard.security.strategy.SecurityProtocol;
import io.netty.buffer.ByteBuf;
import io.reactivex.netty.protocol.http.client.HttpClientRequest;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import rx.Observable;
import rx.schedulers.Schedulers;

import java.util.List;
import java.util.Map;

/**
 * @author Tim Ysewyn
 * @author Andreas Evers
 * @author Kevin van Houtte
 */
public class IndexesAggregator implements NodeAggregator {

	private static final Logger logger = LoggerFactory.getLogger(IndexesAggregator.class);

	private final DiscoveryClient discoveryClient;
	private final IndexToNodeConverter indexToNodeConverter;
	private final ApplicationEventPublisher publisher;
	private final IndexProperties properties;
	private final UriResolver uriResolver;
	private final NettyServiceCaller caller;
	private final StrategyFactory strategyFactory;

	public IndexesAggregator(IndexToNodeConverter indexToNodeConverter, DiscoveryClient discoveryClient,
							 UriResolver uriResolver, IndexProperties properties,
							 ApplicationEventPublisher publisher, NettyServiceCaller caller, StrategyFactory strategyFactory) {
		this.indexToNodeConverter = indexToNodeConverter;
		this.discoveryClient = discoveryClient;
		this.uriResolver = uriResolver;
		this.properties = properties;
		this.publisher = publisher;
		this.caller = caller;
		this.strategyFactory = strategyFactory;
	}

	@Override
	public Observable<Node> aggregateNodes() {
		final Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		return getServicesFromDiscoveryClient()
				.flatMap(this::getFirstInstanceForService)
				.flatMap((ServiceInstance serviceInstance) -> getIndexFromServiceInstance(serviceInstance, auth))
				.doOnNext(el -> logger.debug("Emitting node with id '{}'", el.getId()))
				.doOnError(e -> {
					String error = "Error while emitting a node: " + e.getMessage();
					logger.error(error);
					publisher.publishEvent(new SystemEvent(error, e));
				})
				.doOnCompleted(() -> logger.info("Completed emitting all index nodes"));
	}

	private Observable<String> getServicesFromDiscoveryClient() {
		logger.info("Discovering services");
		return Observable.from(discoveryClient.getServices()).subscribeOn(Schedulers.io()).publish().autoConnect()
				.map(String::toLowerCase)
				.doOnNext(s -> logger.debug("Service discovered: " + s))
				.doOnError(e -> {
					String error = "Error retrieving services: " + e.getMessage();
					logger.error(error);
					publisher.publishEvent(new SystemEvent(error, e));
				})
				.retry();
	}

	private Observable<ServiceInstance> getFirstInstanceForService(String serviceId) {
		logger.debug("Getting first instance for service '{}'", serviceId);

		List<ServiceInstance> instances = discoveryClient.getInstances(serviceId);

		Observable<ServiceInstance> observableServiceInstance;

		if (instances.isEmpty()) {
			String warning = "No instances found for service '" + serviceId + "'";
			logger.warn(warning);
			publisher.publishEvent(new NodeEvent(serviceId, warning));
			observableServiceInstance = Observable.empty();
		} else {
			observableServiceInstance = Observable.just(instances.get(0));
		}

		return observableServiceInstance;
	}

	private Observable<Node> getIndexFromServiceInstance(ServiceInstance serviceInstance, final Authentication authentication) {
		final String url = uriResolver.resolveHomePageUrl(serviceInstance);
		final String serviceId = serviceInstance.getServiceId().toLowerCase();
		HttpClientRequest<ByteBuf> request = HttpClientRequest.createGet(url);
		SecurityContextHolder.getContext().setAuthentication(authentication);
		SecurityProtocol securityProtocol = SecurityProtocol.valueOf(properties.getSecurity().toUpperCase());
		strategyFactory.getStrategy(SecurityProtocolStrategy.class, securityProtocol).apply(request);
		for (Map.Entry<String, String> header : properties.getRequestHeaders().entrySet()) {
			request.withHeader(header.getKey(), header.getValue());
		}

		return caller.retrieveJsonFromRequest(serviceId, request)
				.map(JSONObject::new)
				.concatMap(source -> indexToNodeConverter.convert(serviceInstance.getServiceId().toLowerCase(), url, source))
				.filter(node -> !properties.getFilteredServices().contains(node.getId()))
				.doOnNext(el -> logger.info("Index node {} discovered in url: {}", el.getId(), url))
				.doOnError(e -> logger.error("Error while fetching node: ", e))
				.doOnCompleted(() -> logger.info("Completed emissions of an index node observable for url: " + url))
				.onErrorResumeNext(Observable.empty());
	}
}
