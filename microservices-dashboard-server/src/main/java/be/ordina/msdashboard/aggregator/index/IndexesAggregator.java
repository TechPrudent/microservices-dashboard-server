package be.ordina.msdashboard.aggregator.index;

import be.ordina.msdashboard.aggregator.EurekaBasedAggregator;
import be.ordina.msdashboard.constants.Constants;
import be.ordina.msdashboard.model.Node;
import be.ordina.msdashboard.model.NodeBuilder;
import be.ordina.msdashboard.model.Service;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.nio.client.methods.HttpAsyncMethods;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import rx.Observable;
import rx.apache.http.ObservableHttp;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.*;

public class IndexesAggregator extends EurekaBasedAggregator<Node> {

    private static final Logger logger = LoggerFactory.getLogger(IndexesAggregator.class);

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

    @Cacheable(value = Constants.INDEX_CACHE_NAME, keyGenerator = "simpleKeyGenerator")
    public Node fetchIndexes() {
        NodeBuilder indexesNode = new NodeBuilder();
        for (FutureTask<Node> task : getFutureTasks()) {
            String key = null;
            try {
                key = ((IdentifiableFutureTask) task).getId();
                Node value = task.get(17_000L, TimeUnit.MILLISECONDS);
                logger.debug("Task {} is done: {}", key, task.isDone());
                indexesNode.withLinkedNodes(value.getLinkedNodes());
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                logger.warn("Problem getting results for task: {} caused by: {}", key, e.toString());
            }
        }
        logger.debug("Finished fetching combined indexes");
        return indexesNode.build();
    }

    @Override
    protected Callable<Node> instantiateAggregatorTask(final HttpServletRequest originRequest, final Service service) {
        return new SingleServiceIndexCollectorTask(service, originRequest);
    }

    public Observable<Node> fetchIndexesWithObservable() {
        return Observable.from(discoveryClient.getServices())
                         .flatMap(service -> {
                             if (logger.isDebugEnabled()) {
                                 logger.debug("Getting instance for service {}", service);
                             }
                             ServiceInstance instance = discoveryClient.getInstances(service)
                                                                       .get(0);

                             String uri = instance.getUri()
                                                  .toString();
                             if (logger.isDebugEnabled()) {
                                 logger.debug("Calling {}...", uri);
                             }

                             CloseableHttpAsyncClient client = HttpAsyncClients.createDefault();
                             client.start();
                             return ObservableHttp.createRequest(HttpAsyncMethods.createGet(uri), client)
                                                  .toObservable()
                                                  .filter(observableHttpResponse -> observableHttpResponse.getResponse().getStatusLine().getStatusCode() < 400)
                                                  .flatMap(observableHttpResponse -> observableHttpResponse.getContent()
                                                                                                           .map(bytes -> {
                                                                                                               String response = new String(bytes);
                                                                                                               try {
                                                                                                                   return new ImmutableTriple<>(service, instance, new JSONObject(response));
                                                                                                               } catch (JSONException e) {
                                                                                                                   logger.error("An exception occurred: {}", e.getStackTrace());
                                                                                                                   logger.error("Response: {}", response);
                                                                                                                   return null;
                                                                                                               }
                                                                                                           }))
                                                  .filter(source -> Objects.nonNull(source));
                         })
                         .map(triple -> {
                             NodeBuilder node = NodeBuilder.node()
                                                           .withId(triple.getLeft());

                             JSONObject source = triple.getRight();

                             if (!source.has(LINKS)) {
                                 logger.error("Index deserialization fails because no HAL _links was found at the root");
                             } else {
                                 JSONObject links = source.getJSONObject(LINKS);

                                 ((Set<String>) links.keySet())
                                         .stream()
                                         .filter(linkKey -> !CURIES.equals(linkKey))
                                         .forEach(linkKey -> {
                                             JSONObject link = links.getJSONObject(linkKey);

                                             NodeBuilder nodeBuilder = NodeBuilder.node()
                                                                                  .withId(linkKey)
                                                                                  .withLane(1)
                                                                                  .withDetail("url", link.getString(HREF))
                                                                                  .withDetail("type", RESOURCE)
                                                                                  .withDetail("status", UP);

                                             if (links.has(CURIES)) {
                                                 String namespace = linkKey.substring(0, linkKey.indexOf(":"));

                                                 JSONArray curies = links.getJSONArray(CURIES);
                                                 for (int i = 0; i < curies.length();) {
                                                     JSONObject curie = curies.getJSONObject(i);

                                                     if (curie.has(CURIE_NAME) && curie.getString(CURIE_NAME).equals(namespace)) {
                                                         ServiceInstance instance = triple.getMiddle();
                                                         String docs = instance.getUri().toString() + curie.getString(HREF).replace("{rel}", linkKey.substring(linkKey.indexOf(":") + 1));
                                                         nodeBuilder.withDetail("docs", docs);
                                                         break;
                                                     }
                                                 }
                                             }

                                             node.withLinkedNode(nodeBuilder.build());
                                         });
                             }

                             return node.build();
                         })
                         .reduce(new Node(), (node, nextNode) -> {
                             node.getLinkedNodes().add(nextNode);
                             return node;
                         });
    }
}
