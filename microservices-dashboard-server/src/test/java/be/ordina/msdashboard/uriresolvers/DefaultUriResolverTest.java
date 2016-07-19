package be.ordina.msdashboard.uriresolvers;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.net.URI;

import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.cloud.client.ServiceInstance;

public class DefaultUriResolverTest {

	private DefaultUriResolver defaultUriResolver = new DefaultUriResolver();
	
    @Test
    public void resolveHomePageUrl() throws Exception {
    	ServiceInstance serviceInstance = Mockito.mock(ServiceInstance.class);
    	when(serviceInstance.getUri()).thenReturn(new URI("http://localhost"));
    	assertEquals("http://localhost", defaultUriResolver.resolveHomePageUrl(serviceInstance));
    }

    @Test
    public void resolveHealthCheckUrl() throws Exception {
    	ServiceInstance serviceInstance = Mockito.mock(ServiceInstance.class);
    	when(serviceInstance.getUri()).thenReturn(new URI("http://localhost"));
    	assertEquals("http://localhost/health", defaultUriResolver.resolveHealthCheckUrl(serviceInstance));
    }
}
