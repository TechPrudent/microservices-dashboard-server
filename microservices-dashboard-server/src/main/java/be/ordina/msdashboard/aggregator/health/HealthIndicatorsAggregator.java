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
package be.ordina.msdashboard.aggregator.health;

import be.ordina.msdashboard.aggregator.NodeAggregator;
import be.ordina.msdashboard.model.Node;
import io.reactivex.netty.RxNetty;
import io.reactivex.netty.protocol.http.AbstractHttpContentHolder;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.json.JacksonJsonParser;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import rx.Observable;
import rx.schedulers.Schedulers;

import java.nio.charset.Charset;
import java.util.Objects;

import static be.ordina.msdashboard.constants.Constants.*;

/**
 * @author Andreas Evers
 */
public class HealthIndicatorsAggregator implements NodeAggregator {

	private static final Logger LOG = LoggerFactory.getLogger(HealthIndicatorsAggregator.class);

	protected static final String ZUUL_ID = "zuul";

	private DiscoveryClient discoveryClient;

	public HealthIndicatorsAggregator(DiscoveryClient discoveryClient) {
		this.discoveryClient = discoveryClient;
	}

	//TODO: Caching
	//@Cacheable(value = Constants.HEALTH_CACHE_NAME, keyGenerator = "simpleKeyGenerator")
	@Override
	public Observable<Node> aggregateNodes() {
		Observable<Observable<Node>> observableObservable = getServiceIdsFromDiscoveryClient()
				//TODO: getUri() on ServiceInstance is not including managementContextPath or contextRoot
				//We should include it when the service has a managementContextPath
				//Also, if the service has a contextRoot, we should add that as well!
				.map(id -> new ImmutablePair<String, String>(id, discoveryClient.getInstances(id).get(0).getUri().toString() + "/health"))
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

	//TODO: Add hook for headers on GET (e.g. globalId)
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
				//TODO: .map(node -> toolBoxDependenciesModifier.modify(node))
				.doOnNext(el -> LOG.info("Health node discovered in url: " + url))
				.doOnNext(el -> LOG.info("Node added: " + el.getId()))
				.doOnCompleted(() -> LOG.info("Completed emission of a health node observable from url: " + url));
	}
}