package be.ordina.msdashboard.aggregator.health.eureka;

import be.ordina.msdashboard.aggregator.HealthAggregator;
import be.ordina.msdashboard.node.Node;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.netflix.eureka.EurekaDiscoveryClient;

import java.util.ArrayList;
import java.util.List;

/**
 * A health aggregator that uses eureka for node discovery.
 *
 * @author Tim Ysewyn
 */
public class EurekaHealthAggregator extends HealthAggregator {

    private DiscoveryClient discoveryClient;

    public EurekaHealthAggregator(DiscoveryClient discoveryClient) {
        this.discoveryClient = discoveryClient;
    }

    @Override
    protected List<Node> fetchNodes() {
        List<EurekaHealthAggregationTask> aggregationTasks = new ArrayList<>();
        discoveryClient.getServices().stream().forEach(
                serviceId -> discoveryClient.getInstances(serviceId).stream().forEach(
                        serviceInstance -> aggregationTasks.add(createAggregationTask(serviceInstance))
                )
        );

        List<Node> nodes = new ArrayList<>();
        aggregationTasks.parallelStream().forEach(
                aggregationTask -> {
                    Node node = aggregationTask.getNode();
                    if (node != null) {
                        nodes.add(node);
                    }
                }
        );

        return nodes;
    }

    private EurekaHealthAggregationTask createAggregationTask(ServiceInstance serviceInstance) {
        return new EurekaHealthAggregationTask((EurekaDiscoveryClient.EurekaServiceInstance)serviceInstance);
    }
}
