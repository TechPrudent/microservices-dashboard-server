package be.ordina.msdashboard.uriresolvers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.springframework.cloud.netflix.eureka.EurekaDiscoveryClient;

import com.netflix.appinfo.InstanceInfo;

public class EurekaUriResolverTest {

	private EurekaUriResolver eurekaUriResolver = new EurekaUriResolver();
    @Test
    public void resolveHomePageUrl() {
    	EurekaDiscoveryClient.EurekaServiceInstance instance = mock(EurekaDiscoveryClient.EurekaServiceInstance.class);
    	InstanceInfo instanceInfo = mock(InstanceInfo.class);
    	when(instance.getInstanceInfo()).thenReturn(instanceInfo);
    	when(instanceInfo.getHomePageUrl()).thenReturn("http://homepage:1000");
    	assertThat(eurekaUriResolver.resolveHomePageUrl(instance)).isEqualTo("http://homepage:1000");
    }

    @Test
    public void resolveHealthCheckUrl() {
    	EurekaDiscoveryClient.EurekaServiceInstance instance = mock(EurekaDiscoveryClient.EurekaServiceInstance.class);
    	InstanceInfo instanceInfo = mock(InstanceInfo.class);
    	when(instance.getInstanceInfo()).thenReturn(instanceInfo);
    	when(instanceInfo.getHealthCheckUrl()).thenReturn("http://homepage:1000/health");
    	assertThat(eurekaUriResolver.resolveHealthCheckUrl(instance)).isEqualTo("http://homepage:1000/health");
    }
}
