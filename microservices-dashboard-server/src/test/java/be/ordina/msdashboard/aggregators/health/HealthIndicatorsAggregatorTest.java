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
package be.ordina.msdashboard.aggregators.health;

import be.ordina.msdashboard.aggregators.ErrorHandler;
import be.ordina.msdashboard.aggregators.NettyServiceCaller;
import be.ordina.msdashboard.uriresolvers.UriResolver;
import io.reactivex.netty.protocol.http.client.HttpClientRequest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import rx.Observable;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

/**
 * @author Andreas Evers
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(HealthToNodeConverter.class)
public class HealthIndicatorsAggregatorTest {

    @InjectMocks
    private HealthIndicatorsAggregator aggregator;

    @Mock
    private DiscoveryClient discoveryClient;
    @Mock
    private UriResolver uriResolver;
    @Mock
    private HealthProperties properties;
    @Mock
    private NettyServiceCaller caller;
    @Mock
    private ErrorHandler errorHandler;
    @Captor
    private ArgumentCaptor<HttpClientRequest> requestCaptor;

    @Before
    public void setUp() throws Exception {
        mockStatic(HealthToNodeConverter.class);
    }

    @Test
    public void aggregatesEverything() {

    }

    @Test
    public void shouldGetHealthNodesFromService() {
        when(properties.getRequestHeaders()).thenReturn(correctRequestHeaders());
        PowerMockito.when(HealthToNodeConverter.convertToNodes(anyString(), anyMap())).thenReturn(Observable.empty());
        when(caller.retrieveJsonFromRequest(anyString(), any(HttpClientRequest.class))).thenReturn(Observable.empty());

        aggregator.getHealthNodesFromService("testService", "testUrl");

        verify(caller, times(1)).retrieveJsonFromRequest(eq("testService"), requestCaptor.capture());
        // assertThat(requestCaptor.getValue().getHeaders().entries()).usingElementComparator().containsExactlyElementsOf(correctRequestHeaders().entrySet());
    }

    private Map<String,String> correctRequestHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "application/hal+json");
        headers.put("Accept-Language", "en-us,en;q=0.5");
        return headers;
    }
}
