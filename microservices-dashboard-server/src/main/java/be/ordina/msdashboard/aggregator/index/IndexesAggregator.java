package be.ordina.msdashboard.aggregator.index;

import be.ordina.msdashboard.aggregator.NodeAggregator;
import be.ordina.msdashboard.model.Node;
import be.ordina.msdashboard.model.NodeBuilder;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.nio.client.methods.HttpAsyncMethods;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import rx.Observable;
import rx.apache.http.ObservableHttp;
import rx.apache.http.ObservableHttpResponse;
import rx.schedulers.Schedulers;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * @author Tim Ysewyn
 * @author Andreas Evers
 */
//TODO: Reuse code from HealthIndicatorsAggregator and apply composition
public class IndexesAggregator implements NodeAggregator {

    private static final Logger LOG = LoggerFactory.getLogger(IndexesAggregator.class);

    private static final String LINKS = "_links";
    private static final String CURIES = "curies";
    private static final String HREF = "href";
    private static final String CURIE_NAME = "name";
    private static final String RESOURCE = "RESOURCE";
    private static final String UP = "UP";

    private final DiscoveryClient discoveryClient;

    public IndexesAggregator(DiscoveryClient discoveryClient) {
        this.discoveryClient = discoveryClient;
    }

    //TODO: Caching
    //@Cacheable(value = Constants.INDEX_CACHE_NAME, keyGenerator = "simpleKeyGenerator")
    public Observable<Node> aggregateNodes() {
        return Observable.from(discoveryClient.getServices())
                .subscribeOn(Schedulers.io())
                .flatMap(this::createObservableHttpRequest)
                .concatMap(this::parseResponseIntoNode)
                .doOnNext(el -> LOG.info("Merged index node! " + el.getId()));
    }

    private Observable<ImmutableTriple<String, ServiceInstance, JSONObject>> createObservableHttpRequest(String service) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Getting instance for service {}", service);
        }
        LOG.info("Discovering services for index");
        ServiceInstance instance = discoveryClient.getInstances(service)
                                                  .get(0);
        //TODO: getUri() on ServiceInstance is not including contextRoot
        //We should include it when the service has a contextRoot
        String uri = instance.getUri()
                             .toString();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Calling {}...", uri);
        }
        LOG.info("Index url discovered: " + uri);
        CloseableHttpAsyncClient client = HttpAsyncClients.createDefault();
        client.start();
        //TODO: Add hook for headers on GET (e.g. accept = application/hal+json")
        return ObservableHttp.createRequest(HttpAsyncMethods.createGet(uri), client)
                             .toObservable()
                            //TODO: exception handling and logging
                             .filter(observableHttpResponse -> observableHttpResponse.getResponse().getStatusLine().getStatusCode() < 400)
                             .flatMap(ObservableHttpResponse::getContent)
                             .map(bytes -> {
                                 String response = new String(bytes);
                                 try {
                                     return new ImmutableTriple<>(service, instance, new JSONObject(response));
                                 } catch (JSONException e) {
                                     LOG.error("An exception occurred: {}", e.getStackTrace());
                                     LOG.error("Response: {}", response);
                                     return null;
                                 }
                             })
                             .filter(Objects::nonNull);
    }

    private Observable<Node> parseResponseIntoNode(ImmutableTriple<String, ServiceInstance, JSONObject> triple) {
        String service = triple.getLeft();
        ServiceInstance serviceInstance = triple.getMiddle();
        JSONObject source = triple.getRight();

        NodeBuilder serviceNode = NodeBuilder.node().withId(service);

        List<Node> nodes = new ArrayList<>();

        JSONObject links = source.getJSONObject(LINKS);

        ((Set<String>) links.keySet())
                .stream()
                .filter(linkKey -> !CURIES.equals(linkKey))
                .forEach(linkKey -> {
                    JSONObject link = links.getJSONObject(linkKey);

                    serviceNode.withLinkedFromNodeId(linkKey);

                    NodeBuilder nodeBuilder = NodeBuilder.node()
                                                         .withId(linkKey)
                                                         .withLane(1)
                                                         .withLinkedToNodeId(service)
                                                         .withDetail("url", link.getString(HREF))
                                                         .withDetail("type", RESOURCE)
                                                         .withDetail("status", UP);

                    if (links.has(CURIES)) {
                        String namespace = linkKey.substring(0, linkKey.indexOf(":"));

                        JSONArray curies = links.getJSONArray(CURIES);
                        for (int i = 0; i < curies.length();) {
                            JSONObject curie = curies.getJSONObject(i);

                            if (curie.has(CURIE_NAME) && curie.getString(CURIE_NAME).equals(namespace)) {
                                String docs = serviceInstance.getUri().toString() + curie.getString(HREF).replace("{rel}", linkKey.substring(linkKey.indexOf(":") + 1));
                                nodeBuilder.withDetail("docs", docs);
                                break;
                            }
                        }
                    }

                    nodes.add(nodeBuilder.build());
                });

        nodes.add(0, serviceNode.build());

        return Observable.from(nodes);
    }
}
