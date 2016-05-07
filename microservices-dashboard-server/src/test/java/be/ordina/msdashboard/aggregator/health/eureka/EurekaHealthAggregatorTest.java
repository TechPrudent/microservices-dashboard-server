package be.ordina.msdashboard.aggregator.health.eureka;

import be.ordina.msdashboard.node.Node;
import be.ordina.msdashboard.node.NodeBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.netflix.eureka.EurekaDiscoveryClient;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.powermock.api.support.membermodification.MemberMatcher.method;

@RunWith(PowerMockRunner.class)
@PrepareForTest(EurekaHealthAggregator.class)
public class EurekaHealthAggregatorTest {

    @Test
    public void shouldReturnEmptyList() {
        DiscoveryClient discoveryClient = Mockito.mock(EurekaDiscoveryClient.class);
        EurekaHealthAggregator eurekaHealthAggregator = new EurekaHealthAggregator(discoveryClient);

        List<Node> nodes = eurekaHealthAggregator.getNodes();

        assertThat(nodes).isNotNull();
        assertThat(nodes).isEmpty();
    }

    @Test
    public void shouldReturnTwoNodes() throws Exception {
        DiscoveryClient discoveryClient = Mockito.mock(EurekaDiscoveryClient.class);
        EurekaHealthAggregator eurekaHealthAggregator = PowerMockito.spy(new EurekaHealthAggregator(discoveryClient));

        when(discoveryClient.getServices()).thenReturn(Arrays.asList("serviceA", "serviceB"));

        EurekaDiscoveryClient.EurekaServiceInstance instanceServiceA = Mockito.mock(EurekaDiscoveryClient.EurekaServiceInstance.class);
        when(discoveryClient.getInstances("serviceA")).thenReturn(Collections.singletonList(instanceServiceA));
        EurekaHealthAggregationTask aggregationTaskInstanceServiceA = Mockito.mock(EurekaHealthAggregationTask.class);
        PowerMockito.doReturn(aggregationTaskInstanceServiceA)
                    .when(eurekaHealthAggregator, method(EurekaHealthAggregator.class, "createAggregationTask", ServiceInstance.class))
                    .withArguments(instanceServiceA);
        when(aggregationTaskInstanceServiceA.getNode()).thenReturn(NodeBuilder.node().withId("instanceServiceA").build());

        EurekaDiscoveryClient.EurekaServiceInstance firstInstanceServiceB = Mockito.mock(EurekaDiscoveryClient.EurekaServiceInstance.class);
        EurekaDiscoveryClient.EurekaServiceInstance secondInstanceServiceB = Mockito.mock(EurekaDiscoveryClient.EurekaServiceInstance.class);
        when(discoveryClient.getInstances("serviceB")).thenReturn(Arrays.asList(firstInstanceServiceB, secondInstanceServiceB));

        EurekaHealthAggregationTask firstAggregationTaskInstanceServiceB = Mockito.mock(EurekaHealthAggregationTask.class);
        PowerMockito.doReturn(firstAggregationTaskInstanceServiceB)
                    .when(eurekaHealthAggregator, method(EurekaHealthAggregator.class, "createAggregationTask", ServiceInstance.class))
                    .withArguments(firstInstanceServiceB);
        when(firstAggregationTaskInstanceServiceB.getNode()).thenReturn(null);

        EurekaHealthAggregationTask secondAggregationTaskInstanceServiceB = Mockito.mock(EurekaHealthAggregationTask.class);
        PowerMockito.doReturn(secondAggregationTaskInstanceServiceB)
                    .when(eurekaHealthAggregator, method(EurekaHealthAggregator.class, "createAggregationTask", ServiceInstance.class))
                    .withArguments(secondInstanceServiceB);
        when(secondAggregationTaskInstanceServiceB.getNode()).thenReturn(NodeBuilder.node().withId("secondInstanceServiceB").build());

        List<Node> nodes = eurekaHealthAggregator.getNodes();

        assertThat(nodes).isNotNull();
        assertThat(nodes).isNotEmpty();
        assertThat(nodes.size()).isEqualTo(2);
    }

    @Test
    public void shouldReturnThreeNodes() throws Exception {
        DiscoveryClient discoveryClient = Mockito.mock(EurekaDiscoveryClient.class);
        EurekaHealthAggregator eurekaHealthAggregator = PowerMockito.spy(new EurekaHealthAggregator(discoveryClient));

        when(discoveryClient.getServices()).thenReturn(Arrays.asList("serviceA", "serviceB"));

        EurekaDiscoveryClient.EurekaServiceInstance instanceServiceA = Mockito.mock(EurekaDiscoveryClient.EurekaServiceInstance.class);
        when(discoveryClient.getInstances("serviceA")).thenReturn(Collections.singletonList(instanceServiceA));
        EurekaHealthAggregationTask aggregationTaskInstanceServiceA = Mockito.mock(EurekaHealthAggregationTask.class);
        PowerMockito.doReturn(aggregationTaskInstanceServiceA)
                    .when(eurekaHealthAggregator, method(EurekaHealthAggregator.class, "createAggregationTask", ServiceInstance.class))
                    .withArguments(instanceServiceA);
        when(aggregationTaskInstanceServiceA.getNode()).thenReturn(NodeBuilder.node().withId("instanceServiceA").build());

        EurekaDiscoveryClient.EurekaServiceInstance firstInstanceServiceB = Mockito.mock(EurekaDiscoveryClient.EurekaServiceInstance.class);
        EurekaDiscoveryClient.EurekaServiceInstance secondInstanceServiceB = Mockito.mock(EurekaDiscoveryClient.EurekaServiceInstance.class);
        when(discoveryClient.getInstances("serviceB")).thenReturn(Arrays.asList(firstInstanceServiceB, secondInstanceServiceB));

        EurekaHealthAggregationTask firstAggregationTaskInstanceServiceB = Mockito.mock(EurekaHealthAggregationTask.class);
        PowerMockito.doReturn(firstAggregationTaskInstanceServiceB)
                    .when(eurekaHealthAggregator, method(EurekaHealthAggregator.class, "createAggregationTask", ServiceInstance.class))
                    .withArguments(firstInstanceServiceB);
        when(firstAggregationTaskInstanceServiceB.getNode()).thenReturn(NodeBuilder.node().withId("firstInstanceServiceB").build());

        EurekaHealthAggregationTask secondAggregationTaskInstanceServiceB = Mockito.mock(EurekaHealthAggregationTask.class);
        PowerMockito.doReturn(secondAggregationTaskInstanceServiceB)
                    .when(eurekaHealthAggregator, method(EurekaHealthAggregator.class, "createAggregationTask", ServiceInstance.class))
                    .withArguments(secondInstanceServiceB);
        when(secondAggregationTaskInstanceServiceB.getNode()).thenReturn(NodeBuilder.node().withId("secondInstanceServiceB").build());

        List<Node> nodes = eurekaHealthAggregator.getNodes();

        assertThat(nodes).isNotNull();
        assertThat(nodes).isNotEmpty();
        assertThat(nodes.size()).isEqualTo(3);
    }

}
