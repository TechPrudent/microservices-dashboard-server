package be.ordina.msdashboard.aggregator.index;

import be.ordina.msdashboard.aggregator.EurekaBasedAggregator;
import be.ordina.msdashboard.constants.Constants;
import be.ordina.msdashboard.model.Node;
import be.ordina.msdashboard.model.NodeBuilder;
import be.ordina.msdashboard.model.Service;
import io.reactivex.netty.RxNetty;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import rx.Observable;
import rx.Observer;
import rx.schedulers.Schedulers;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.*;

/**
 * @author Tim Ysewyn
 */
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
                indexesNode.withLinkedNodes(value.getLinkedToNodes());
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

    // REACTIVE WAY

    public Observable<Node> fetchIndexesAsObservable() {
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
                         .observeOn(Schedulers.computation())
                         .concatMap(this::parseRequestIntoNode)
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

    private Observable<ImmutablePair<ServiceInstance, JSONObject>> getIndexFromServiceInstance(ServiceInstance serviceInstance) {
        String uri = serviceInstance.getUri().toString();
        if (logger.isDebugEnabled()) {
            logger.debug("Creating GET request to '{}'...", uri);
        }

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
                      .flatMap(r -> r.getContent()
                                     .map(bb -> new JSONObject(bb.toString(Charset.defaultCharset()))))
                      .onErrorReturn(throwable -> {
                          logger.error("Skipping the result of service '{}' because of an error: {}",
                                  serviceInstance.getServiceId(),
                                  throwable.getMessage());
                          return null;
                      })
                      .filter(Objects::nonNull)
                      .map(json -> ImmutablePair.of(serviceInstance, json));
    }

    @SuppressWarnings("unchecked")
    private Observable<Node> parseRequestIntoNode(ImmutablePair<ServiceInstance, JSONObject> pair) {
        ServiceInstance serviceInstance = pair.getLeft();
        JSONObject source = pair.getRight();

        NodeBuilder serviceNode = NodeBuilder.node().withId(serviceInstance.getServiceId());

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
                                                         .withLinkedToNodeId(serviceInstance.getServiceId())
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
