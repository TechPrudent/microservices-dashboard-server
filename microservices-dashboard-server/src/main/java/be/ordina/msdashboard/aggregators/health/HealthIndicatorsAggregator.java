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

import be.ordina.msdashboard.aggregators.NodeAggregator;
import be.ordina.msdashboard.events.NodeEvent;
import be.ordina.msdashboard.events.SystemEvent;
import be.ordina.msdashboard.model.Node;
import be.ordina.msdashboard.uriresolvers.UriResolver;
import io.netty.buffer.ByteBuf;
import io.reactivex.netty.RxNetty;
import io.reactivex.netty.protocol.http.AbstractHttpContentHolder;
import io.reactivex.netty.protocol.http.client.HttpClientRequest;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.json.JacksonJsonParser;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.ApplicationEventPublisher;
import rx.Observable;
import rx.schedulers.Schedulers;

import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.Map.Entry;

import static be.ordina.msdashboard.constants.Constants.*;

/**
 * @author Andreas Evers
 */
public class HealthIndicatorsAggregator implements NodeAggregator {

	protected static final String ZUUL_ID = "zuul";

	private static final Logger logger = LoggerFactory.getLogger(HealthIndicatorsAggregator.class);

	private DiscoveryClient discoveryClient;
	private UriResolver uriResolver;
	private HealthProperties properties;
	private ApplicationEventPublisher publisher;

	public HealthIndicatorsAggregator(final DiscoveryClient discoveryClient, final UriResolver uriResolver,
									  final HealthProperties properties, final ApplicationEventPublisher publisher) {
		this.discoveryClient = discoveryClient;
		this.uriResolver = uriResolver;
		this.properties = properties;
		this.publisher = publisher;
	}

	//TODO: Caching
	//@Cacheable(value = Constants.HEALTH_CACHE_NAME, keyGenerator = "simpleKeyGenerator")
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

	private Observable<String> getServiceIdsFromDiscoveryClient() {
		logger.info("Discovering services for health");
		return Observable.from(discoveryClient.getServices()).subscribeOn(Schedulers.io())
				.doOnError(e -> {
					String error = "Error retrieving services: " + e.getMessage();
					logger.error(error);
					publisher.publishEvent(new SystemEvent(error, e));
				})
				.onErrorResumeNext(Observable.empty())
				.doOnNext(s -> logger.debug("Service discovered: " + s))
				.map(id -> id.toLowerCase())
				.filter(id -> !id.equals(ZUUL_ID));
	}

	//TODO: Add hook for properties on GET (e.g. globalId)
	private Observable<Node> getHealthNodesFromService(String serviceId, String url) {
		HttpClientRequest<ByteBuf> request = HttpClientRequest.createGet(url);
		for (Entry<String, String> header : properties.getRequestHeaders().entrySet()) {
			request.withHeader(header.getKey(), header.getValue());
		}
		return RxNetty.createHttpRequest(request)
				.doOnError(el -> {
					String error = MessageFormat.format("Error retrieving healthnodes in url {} with headers {}: {}",
							request.getUri(), request.getHeaders().entries(), el);
					logger.error(error);
					publisher.publishEvent(new NodeEvent(serviceId, error));
				})
				.onErrorResumeNext(Observable.empty())
				.filter(r -> {
					if (r.getStatus().code() < 400) {
						return true;
					} else {
						String warning = "Exception " + r.getStatus() + " for call " + url + " with headers " + r.getHeaders().entries();
						logger.warn(warning);
						publisher.publishEvent(new NodeEvent(serviceId, warning));
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
				//TODO: .map(node -> toolBoxDependenciesModifier.modify(node))
				.doOnNext(el -> logger.info("Health node {} discovered in url: {}", el.getId(), url))
				.doOnCompleted(() -> logger.info("Completed emission of a health node observable from url: " + url));
	}
}