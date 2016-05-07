package be.ordina.msdashboard.aggregator.health.eureka;

import be.ordina.msdashboard.aggregator.health.Constants;
import be.ordina.msdashboard.node.Node;
import com.netflix.appinfo.InstanceInfo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.cloud.netflix.eureka.EurekaDiscoveryClient;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.powermock.api.support.membermodification.MemberMatcher.method;

@RunWith(PowerMockRunner.class)
@PrepareForTest(EurekaHealthAggregationTask.class)
public class EurekaHealthAggregationTaskTest {

    @Test
    public void noHealthCheckUrlShouldReturnNull() {
        EurekaDiscoveryClient.EurekaServiceInstance eurekaServiceInstance = Mockito.mock(EurekaDiscoveryClient.EurekaServiceInstance.class);
        EurekaHealthAggregationTask aggregationTask = new EurekaHealthAggregationTask(eurekaServiceInstance);

        when(eurekaServiceInstance.getServiceId()).thenReturn("service");

        InstanceInfo instanceInfo = Mockito.mock(InstanceInfo.class);
        when(eurekaServiceInstance.getInstanceInfo()).thenReturn(instanceInfo);

        Node node = aggregationTask.getNode();

        assertThat(node).isNull();
    }

    @Test
    public void noResponseShouldReturnNull() throws Exception {
        EurekaDiscoveryClient.EurekaServiceInstance eurekaServiceInstance = Mockito.mock(EurekaDiscoveryClient.EurekaServiceInstance.class);
        EurekaHealthAggregationTask aggregationTask = PowerMockito.spy(new EurekaHealthAggregationTask(eurekaServiceInstance));

        when(eurekaServiceInstance.getServiceId()).thenReturn("service");

        InstanceInfo instanceInfo = Mockito.mock(InstanceInfo.class);
        when(eurekaServiceInstance.getInstanceInfo()).thenReturn(instanceInfo);

        when(instanceInfo.getHealthCheckUrl()).thenReturn("http://localhost:8080/health");

        RestTemplate restTemplate = Mockito.mock(RestTemplate.class);
        PowerMockito.doReturn(restTemplate)
                    .when(aggregationTask, method(EurekaHealthAggregationTask.class, "getRestTemplate"))
                    .withNoArguments();

        ResponseEntity<EurekaHealthInfo> response = Mockito.mock(ResponseEntity.class);
        when(restTemplate.exchange("http://localhost:8080/health", HttpMethod.GET, null, EurekaHealthInfo.class)).thenReturn(response);
        when(response.getStatusCode()).thenReturn(HttpStatus.SERVICE_UNAVAILABLE);

        Node node = aggregationTask.getNode();

        assertThat(node).isNull();
    }

    @Test
    public void shouldReturnANode() throws Exception {
        EurekaDiscoveryClient.EurekaServiceInstance eurekaServiceInstance = Mockito.mock(EurekaDiscoveryClient.EurekaServiceInstance.class);
        EurekaHealthAggregationTask aggregationTask = PowerMockito.spy(new EurekaHealthAggregationTask(eurekaServiceInstance));

        when(eurekaServiceInstance.getServiceId()).thenReturn("service");

        InstanceInfo instanceInfo = Mockito.mock(InstanceInfo.class);
        when(eurekaServiceInstance.getInstanceInfo()).thenReturn(instanceInfo);

        when(instanceInfo.getHealthCheckUrl()).thenReturn("http://localhost:8080/health");

        RestTemplate restTemplate = Mockito.mock(RestTemplate.class);
        PowerMockito.doReturn(restTemplate)
                    .when(aggregationTask, method(EurekaHealthAggregationTask.class, "getRestTemplate"))
                    .withNoArguments();

        ResponseEntity<EurekaHealthInfo> response = Mockito.mock(ResponseEntity.class);
        when(restTemplate.exchange("http://localhost:8080/health", HttpMethod.GET, null, EurekaHealthInfo.class)).thenReturn(response);
        when(response.getStatusCode()).thenReturn(HttpStatus.OK);
        when(response.hasBody()).thenReturn(true);

        EurekaHealthInfo eurekaHealthInfo = new EurekaHealthInfo();
        eurekaHealthInfo.setDescription("Description");
        eurekaHealthInfo.setStatus("UP");
        when(response.getBody()).thenReturn(eurekaHealthInfo);

        Node node = aggregationTask.getNode();

        assertThat(node).isNotNull();
        assertThat(node.getId()).isEqualTo("service");
        assertThat(node.getDetails()).isNotEmpty();
        assertThat(node.getDetails().get(Constants.STATUS)).isEqualTo("UP");
    }

}
