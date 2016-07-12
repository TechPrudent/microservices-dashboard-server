/*
 * Copyright 2012-2016 the original author or authors.
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
package be.ordina.msdashboard.aggregators.health;

import be.ordina.msdashboard.aggregators.ErrorHandler;
import be.ordina.msdashboard.aggregators.NettyServiceCaller;
import be.ordina.msdashboard.aggregators.NodeAggregator;
import be.ordina.msdashboard.model.Node;
import be.ordina.msdashboard.uriresolvers.UriResolver;
import io.netty.buffer.ByteBuf;
import io.reactivex.netty.protocol.http.client.HttpClientRequest;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import rx.Observable;
import rx.schedulers.Schedulers;

import java.util.Map.Entry;

import static be.ordina.msdashboard.constants.Constants.*;

/**
 * Aggregates nodes from health information exposed by Spring Boot's Actuator.
 * <p>
 * In case Spring Boot is not used for a microservice, the following format
 * should be exposed under the <code>/health</code> endpoint:
 *
 * TODO: document desired format
 * <pre>
 * {
 * "store":
 * {
 * "book":
 * [
 * {
 * "category": "reference",
 * "author": "Nigel Rees",
 * "title": "Sayings of the Century",
 * "price": 8.95
 * },
 * {
 * "category": "fiction",
 * "author": "Evelyn Waugh",
 * "title": "Sword of Honour",
 * "price": 12.99
 * }
 * ],
 * "bicycle":
 * {
 * "color": "red",
 * "price": 19.95
 * }
 * }
 * }
 * </pre>
 *
 * @author Andreas Evers
 * @see <a href="http://docs.spring.io/spring-boot/docs/current-SNAPSHOT/reference/htmlsingle/#production-ready">
 *     Spring Boot Actuator</a>
 */
public class HealthIndicatorsAggregator implements NodeAggregator {

	protected static final String ZUUL_ID = "zuul";

	private static final Logger logger = LoggerFactory.getLogger(HealthIndicatorsAggregator.class);

	private DiscoveryClient discoveryClient;
	private UriResolver uriResolver;
	private HealthProperties properties;
	private NettyServiceCaller caller;
	private ErrorHandler errorHandler;

	public HealthIndicatorsAggregator(final DiscoveryClient discoveryClient, final UriResolver uriResolver,
									  final HealthProperties properties, final NettyServiceCaller caller,
									  final ErrorHandler errorHandler) {
		this.discoveryClient = discoveryClient;
		this.uriResolver = uriResolver;
		this.properties = properties;
		this.caller = caller;
		this.errorHandler = errorHandler;
	}

	@Override
	public Observable<Node> aggregateNodes() {
		Observable<Observable<Node>> observableObservable = getServiceIdsFromDiscoveryClient()
				.map(id -> new ImmutablePair<String, String>(id, uriResolver.resolveHealthCheckUrl(discoveryClient.getInstances(id).get(0))))
				.doOnNext(pair -> logger.info("Creating health observable: " + pair))
				.map(pair -> getHealthNodesFromService(pair.getLeft(), pair.getRight()))
				.doOnNext(el -> logger.debug("Unmerged health observable: " + el))
				.doOnCompleted(() -> logger.info("Completed getting all health observables"));
		return Observable.merge(observableObservable)
				.doOnNext(el -> logger.debug("Merged health node: " + el.getId()))
				.doOnCompleted(() -> logger.info("Completed merging all health observables"));
	}

	protected Observable<String> getServiceIdsFromDiscoveryClient() {
		logger.info("Discovering services for health");
		return Observable.from(discoveryClient.getServices()).subscribeOn(Schedulers.io())
				.doOnError(e -> errorHandler.handleSystemError("Error retrieving services: " + e.getMessage(), e))
				.onErrorResumeNext(Observable.empty())
				.doOnNext(s -> logger.debug("Service discovered: " + s))
				.map(id -> id.toLowerCase())
				.filter(id -> !id.equals(ZUUL_ID));
	}

	protected Observable<Node> getHealthNodesFromService(String serviceId, String url) {
		HttpClientRequest<ByteBuf> request = HttpClientRequest.createGet(url);
		for (Entry<String, String> header : properties.getRequestHeaders().entrySet()) {
			request.withHeader(header.getKey(), header.getValue());
		}
		return caller.retrieveJsonFromRequest(serviceId, request)
				.map(source -> HealthToNodeConverter.convertToNodes(serviceId, source))
				.flatMap(el -> el)
				.filter(node -> !HYSTRIX.equals(node.getId()) && !DISK_SPACE.equals(node.getId())
						&& !DISCOVERY.equals(node.getId()) && !CONFIGSERVER.equals(node.getId()))
				//TODO: .map(node -> toolBoxDependenciesModifier.modify(node))
				.doOnNext(el -> logger.info("Health node {} discovered in url: {}", el.getId(), url))
				.doOnCompleted(() -> logger.info("Completed emission of a health node observable from url: " + url))
				.retry();
	}
}