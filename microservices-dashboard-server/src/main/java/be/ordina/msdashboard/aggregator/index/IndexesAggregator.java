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
package be.ordina.msdashboard.aggregator.index;

import be.ordina.msdashboard.aggregator.NodeAggregator;
import be.ordina.msdashboard.model.Node;
import io.reactivex.netty.RxNetty;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import rx.Observable;
import rx.Observer;
import rx.schedulers.Schedulers;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Objects;

/**
 * @author Tim Ysewyn
 * @author Andreas Evers
 */
//TODO: Reuse code from HealthIndicatorsAggregator and apply composition
public class IndexesAggregator implements NodeAggregator {

    private static final Logger logger = LoggerFactory.getLogger(IndexesAggregator.class);

    private final DiscoveryClient discoveryClient;
    private final IndexToNodeConverter indexToNodeConverter;

    public IndexesAggregator(IndexToNodeConverter indexToNodeConverter, DiscoveryClient discoveryClient) {
        this.indexToNodeConverter = indexToNodeConverter;
        this.discoveryClient = discoveryClient;
    }

    //TODO: Caching
    //@Cacheable(value = Constants.INDEX_CACHE_NAME, keyGenerator = "simpleKeyGenerator")
    public Observable<Node> aggregateNodes() {
        return Observable.from(discoveryClient.getServices())
                .observeOn(Schedulers.io())
                .doOnNext(service -> {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Sending instance retrieval request for service '{}'", service);
                    }
                })
                .map(this::getFirstInstanceForService)
                .filter(Objects::nonNull)
                .flatMap(this::getIndexFromServiceInstance)
                .doOnEach(new Observer<Node>() {

                    private int totalNodesEmitted = 0;

                    @Override
                    public void onCompleted() {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Emitted {} nodes", totalNodesEmitted);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {}

                    @Override
                    public void onNext(Node node) {
                        ++totalNodesEmitted;

                        if (logger.isDebugEnabled()) {
                            logger.debug("Emitting node with id '{}'", node.getId());
                            logger.debug("{}", node);
                        }
                    }
                });
    }

    private ServiceInstance getFirstInstanceForService(String service) {
        if (logger.isDebugEnabled()) {
            logger.debug("Getting first instance for service '{}'", service);
        }

        List<ServiceInstance> instances = discoveryClient.getInstances(service);

        if (instances.size() > 0) {
            return instances.get(0);
        } else {
            logger.warn("No instances found for service '{}'", service);
            return null;
        }
    }

    private Observable<Node> getIndexFromServiceInstance(ServiceInstance serviceInstance) {
        //TODO: getUri() on ServiceInstance is not including contextRoot
        //We should include it when the service has a contextRoot
        final String uri = serviceInstance.getUri().toString();
        if (logger.isDebugEnabled()) {
            logger.debug("Creating GET request to '{}'...", uri);
        }

        //TODO: Add hook for headers on GET (e.g. accept = application/hal+json")
        return RxNetty.createHttpGet(uri)
                .filter(r -> {
                    if (r.getStatus().code() < 400) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("'GET {}' returned '{}'", uri, r.getStatus());
                        }
                        return true;
                    } else {
                        logger.warn("'GET {}' returned '{}'", uri, r.getStatus());
                        if (logger.isDebugEnabled()) {
                            logger.debug("Headers: {}", r.getHeaders().entries());
                            logger.debug("Cookies: {}", r.getCookies().entrySet());
                        }
                        return false;
                    }
                })
                .flatMap(r -> r.getContent().map(bb -> bb.toString(Charset.defaultCharset())))
                .observeOn(Schedulers.computation())
                .concatMap(source -> indexToNodeConverter.convert(serviceInstance.getServiceId(), uri, source));
    }
}
