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

import be.ordina.msdashboard.model.Node;
import be.ordina.msdashboard.model.NodeBuilder;
import be.ordina.msdashboard.uriresolvers.DefaultUriResolver;
import be.ordina.msdashboard.uriresolvers.UriResolver;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.netty.RxNetty;
import io.reactivex.netty.protocol.http.client.HttpClientRequest;
import io.reactivex.netty.protocol.http.client.HttpClientResponse;
import io.reactivex.netty.protocol.http.client.HttpResponseHeaders;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.ApplicationEventPublisher;
import rx.Observable;
import rx.observers.TestSubscriber;

import java.net.URI;
import java.util.Collections;
import java.util.List;

import static io.netty.handler.codec.http.HttpResponseStatus.SERVICE_UNAVAILABLE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({RxNetty.class})
public class IndexesAggregatorTest {

    private DiscoveryClient discoveryClient;
    private IndexToNodeConverter indexToNodeConverter;
    private IndexesAggregator indexesAggregator;
    @Mock
    private IndexProperties indexProperties;
    @Mock
    private ApplicationEventPublisher publisher;

    @Before
    public void setUp() {
        discoveryClient = mock(DiscoveryClient.class);
        indexToNodeConverter = mock(IndexToNodeConverter.class);
        indexesAggregator = new IndexesAggregator(indexToNodeConverter, discoveryClient, new DefaultUriResolver(), indexProperties, publisher);

        PowerMockito.mockStatic(RxNetty.class);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldReturnThreeNodes() throws InterruptedException {
        when(discoveryClient.getServices()).thenReturn(Collections.singletonList("service"));
        ServiceInstance instance = mock(ServiceInstance.class);
        when(discoveryClient.getInstances("service")).thenReturn(Collections.singletonList(instance));

        when(instance.getServiceId()).thenReturn("service");
        when(instance.getUri()).thenReturn(URI.create("http://localhost:8089/service"));

        HttpClientResponse<ByteBuf> response = mock(HttpClientResponse.class);
        when(RxNetty.createHttpRequest(any(HttpClientRequest.class))).thenReturn(Observable.just(response));

        when(response.getStatus()).thenReturn(HttpResponseStatus.OK);
        ByteBuf byteBuf = (new PooledByteBufAllocator()).directBuffer();
        ByteBufUtil.writeUtf8(byteBuf, "source");
        when(response.getContent()).thenReturn(Observable.just(byteBuf));

        Node node = new NodeBuilder().withId("service").build();

        when(indexToNodeConverter.convert("service", "http://localhost:8089/service", "source")).thenReturn(Observable.just(node));

        TestSubscriber<Node> testSubscriber = new TestSubscriber<>();
        indexesAggregator.aggregateNodes().toBlocking().subscribe(testSubscriber);
        //testSubscriber.assertNoErrors();

        List<Node> nodes = testSubscriber.getOnNextEvents();
        assertThat(nodes).hasSize(1);

        assertThat(nodes.get(0).getId()).isEqualTo("service");
    }

    @Test
    public void noServicesShouldReturnZeroNodes() throws InterruptedException {
        when(discoveryClient.getServices()).thenReturn(Collections.emptyList());

        TestSubscriber<Node> testSubscriber = new TestSubscriber<>();
        indexesAggregator.aggregateNodes().toBlocking().subscribe(testSubscriber);
        testSubscriber.assertNoErrors();

        List<Node> nodes = testSubscriber.getOnNextEvents();
        assertThat(nodes).hasSize(0);
    }

    @Test
    public void noInstancesShouldReturnZeroNodes() throws InterruptedException {
        when(discoveryClient.getServices()).thenReturn(Collections.singletonList("service"));
        when(discoveryClient.getInstances("service")).thenReturn(Collections.emptyList());

        TestSubscriber<Node> testSubscriber = new TestSubscriber<>();
        indexesAggregator.aggregateNodes().toBlocking().subscribe(testSubscriber);
        testSubscriber.assertNoErrors();

        List<Node> nodes = testSubscriber.getOnNextEvents();
        assertThat(nodes).hasSize(0);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void failedIndexCallShouldReturnZeroNodes() throws InterruptedException {
        when(discoveryClient.getServices()).thenReturn(Collections.singletonList("service"));
        ServiceInstance instance = mock(ServiceInstance.class);
        when(discoveryClient.getInstances("service")).thenReturn(Collections.singletonList(instance));

        when(instance.getServiceId()).thenReturn("service");
        when(instance.getUri()).thenReturn(URI.create("http://localhost:8089/service"));

        HttpClientResponse<ByteBuf> response = mock(HttpClientResponse.class);
        when(RxNetty.createHttpGet("http://localhost:8089/service")).thenReturn(Observable.just(response));

        when(response.getStatus()).thenReturn(SERVICE_UNAVAILABLE);
        when(response.getHeaders()).thenReturn(mock(HttpResponseHeaders.class));
        when(response.getCookies()).thenReturn(Collections.emptyMap());

        TestSubscriber<Node> testSubscriber = new TestSubscriber<>();
        indexesAggregator.aggregateNodes().toBlocking().subscribe(testSubscriber);

        List<Node> nodes = testSubscriber.getOnNextEvents();
        assertThat(nodes).hasSize(0);
    }

}
