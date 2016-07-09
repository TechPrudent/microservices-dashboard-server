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
package be.ordina.msdashboard.aggregators.index;

import be.ordina.msdashboard.aggregators.NodeAggregator;
import be.ordina.msdashboard.events.NodeEvent;
import be.ordina.msdashboard.events.SystemEvent;
import be.ordina.msdashboard.model.Node;
import be.ordina.msdashboard.uriresolvers.UriResolver;
import io.netty.buffer.ByteBuf;
import io.reactivex.netty.RxNetty;
import io.reactivex.netty.protocol.http.client.HttpClientRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.ApplicationEventPublisher;
import rx.Observable;
import rx.Observer;
import rx.schedulers.Schedulers;

import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
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
    private final ApplicationEventPublisher publisher;
    private final IndexProperties properties;
    private UriResolver uriResolver;

    public IndexesAggregator(IndexToNodeConverter indexToNodeConverter, DiscoveryClient discoveryClient,
                             UriResolver uriResolver, final IndexProperties properties,
                             final ApplicationEventPublisher publisher) {
        this.indexToNodeConverter = indexToNodeConverter;
        this.discoveryClient = discoveryClient;
        this.uriResolver = uriResolver;
        this.properties = properties;
        this.publisher = publisher;
    }

    //TODO: Caching
    //@Cacheable(value = Constants.INDEX_CACHE_NAME, keyGenerator = "simpleKeyGenerator")
    public Observable<Node> aggregateNodes() {
        return Observable.from(discoveryClient.getServices())
                .observeOn(Schedulers.io())
                .doOnError(e -> {
                    String error = "Error retrieving services: " + e.getMessage();
                    logger.error(error);
                    publisher.publishEvent(new SystemEvent(error, e));
                })
                .onErrorResumeNext(Observable.empty())
                .doOnNext(service -> {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Sending instance retrieval request for serviceId '{}'", service);
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
                    public void onError(Throwable e) {
                        String error = "Error retrieving a node: " + e.getMessage();
                        logger.error(error);
                        publisher.publishEvent(new SystemEvent(error, e));
                    }

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

    private ServiceInstance getFirstInstanceForService(String serviceId) {
        if (logger.isDebugEnabled()) {
            logger.debug("Getting first instance for serviceId '{}'", serviceId);
        }

        List<ServiceInstance> instances = discoveryClient.getInstances(serviceId);

        if (instances.size() > 0) {
            return instances.get(0);
        } else {
            String warning = "No instances found for serviceId " + serviceId;
            logger.warn(warning);
            publisher.publishEvent(new NodeEvent(serviceId, warning));
            return null;
        }
    }

    private Observable<Node> getIndexFromServiceInstance(ServiceInstance serviceInstance) {
        final String url = uriResolver.resolveHomePageUrl(serviceInstance);
        final String serviceId = serviceInstance.getServiceId().toLowerCase();
        HttpClientRequest<ByteBuf> request = HttpClientRequest.createGet(url);
        for (Map.Entry<String, String> header : properties.getRequestHeaders().entrySet()) {
            request.withHeader(header.getKey(), header.getValue());
        }

        //TODO: Add hook for headers on GET (e.g. accept = application/hal+json")
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
                        if (logger.isDebugEnabled()) {
                            logger.debug("'GET {}' returned '{}'", url, r.getStatus());
                        }
                        return true;
                    } else {
                        String warning = "Exception " + r.getStatus() + " for call " + url + " with headers " + r.getHeaders().entries();
                        logger.warn(warning);
                        publisher.publishEvent(new NodeEvent(serviceId, warning));
                        return false;
                    }
                })
                .flatMap(r -> r.getContent().map(bb -> bb.toString(Charset.defaultCharset())))
                .observeOn(Schedulers.computation())
                .concatMap(source -> indexToNodeConverter.convert(serviceInstance.getServiceId().toLowerCase(), url, source));
    }
}
