package be.ordina.msdashboard.aggregator.index;

import be.ordina.msdashboard.model.Node;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.netty.RxNetty;
import io.reactivex.netty.protocol.http.client.HttpClientResponse;
import io.reactivex.netty.protocol.http.client.HttpResponseHeaders;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import rx.Observable;
import rx.observers.TestSubscriber;

import java.net.URI;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({RxNetty.class})
public class IndexesAggregatorTest {

    private DiscoveryClient discoveryClient;
    private IndexToNodeConverter indexToNodeConverter;
    private IndexesAggregator indexesAggregator;

    @Before
    public void setup() {
        discoveryClient = Mockito.mock(DiscoveryClient.class);
        // TODO mock this
        indexToNodeConverter = new IndexToNodeConverter();
        indexesAggregator = new IndexesAggregator(indexToNodeConverter, discoveryClient);

        PowerMockito.mockStatic(RxNetty.class);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldReturnThreeNodes() throws InterruptedException {
        when(discoveryClient.getServices()).thenReturn(Collections.singletonList("service"));
        ServiceInstance instance = Mockito.mock(ServiceInstance.class);
        when(discoveryClient.getInstances("service")).thenReturn(Collections.singletonList(instance));

        when(instance.getServiceId()).thenReturn("service");
        when(instance.getUri()).thenReturn(URI.create("http://localhost:8089/service"));

        HttpClientResponse<ByteBuf> response = Mockito.mock(HttpClientResponse.class);
        when(RxNetty.createHttpGet("http://localhost:8089/service")).thenReturn(Observable.just(response));

        when(response.getStatus()).thenReturn(HttpResponseStatus.OK);
        ByteBuf byteBuf = (new PooledByteBufAllocator()).directBuffer();
        ByteBufUtil.writeUtf8(byteBuf, "{\n" +
                "  \"_links\": {\n" +
                "    \"svc1:svc1rsc1\": {\n" +
                "      \"href\": \"http://host0015.local:8301/svc1rsc1\",\n" +
                "      \"templated\": true\n" +
                "    },\n" +
                "    \"svc1:svc1rsc2\": {\n" +
                "      \"href\": \"http://host0015.local:8301/svc1rsc2\",\n" +
                "      \"templated\": true\n" +
                "    },\n" +
                "    \"curies\": [\n" +
                "      {\n" +
                "        \"href\": \"/generated-docs/api-guide.html#resources-{rel}\",\n" +
                "        \"name\": \"svc1\",\n" +
                "        \"templated\": true\n" +
                "      }\n" +
                "    ]\n" +
                "  }\n" +
                "}");
        when(response.getContent()).thenReturn(Observable.just(byteBuf));

        TestSubscriber<Node> testSubscriber = new TestSubscriber<>();
        indexesAggregator.aggregateNodes().toBlocking().subscribe(testSubscriber);
        testSubscriber.assertNoErrors();

        List<Node> nodes = testSubscriber.getOnNextEvents();
        assertThat(nodes).hasSize(3);

        Iterator<Node> iterator = nodes.iterator();
        Node serviceNode = iterator.next();
        assertThat(serviceNode.getId()).isEqualTo("service");
        assertThat(serviceNode.getLinkedFromNodeIds()).contains("svc1:svc1rsc1", "svc1:svc1rsc2");

        checkResource(iterator.next(), "svc1:svc1rsc1", "http://host0015.local:8301/svc1rsc1", "http://localhost:8089/service/generated-docs/api-guide.html#resources-svc1rsc1");
        checkResource(iterator.next(), "svc1:svc1rsc2", "http://host0015.local:8301/svc1rsc2", "http://localhost:8089/service/generated-docs/api-guide.html#resources-svc1rsc2");
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
        ServiceInstance instance = Mockito.mock(ServiceInstance.class);
        when(discoveryClient.getInstances("service")).thenReturn(Collections.singletonList(instance));

        when(instance.getServiceId()).thenReturn("service");
        when(instance.getUri()).thenReturn(URI.create("http://localhost:8089/service"));

        HttpClientResponse<ByteBuf> response = Mockito.mock(HttpClientResponse.class);
        when(RxNetty.createHttpGet("http://localhost:8089/service")).thenReturn(Observable.just(response));

        when(response.getStatus()).thenReturn(HttpResponseStatus.SERVICE_UNAVAILABLE);
        when(response.getHeaders()).thenReturn(mock(HttpResponseHeaders.class));
        when(response.getCookies()).thenReturn(Collections.emptyMap());

        TestSubscriber<Node> testSubscriber = new TestSubscriber<>();
        indexesAggregator.aggregateNodes().toBlocking().subscribe(testSubscriber);
        testSubscriber.assertNoErrors();

        List<Node> nodes = testSubscriber.getOnNextEvents();
        assertThat(nodes).hasSize(0);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void wrongIndexResponseShouldReturnZeroNodes() throws InterruptedException {
        when(discoveryClient.getServices()).thenReturn(Collections.singletonList("service"));
        ServiceInstance instance = Mockito.mock(ServiceInstance.class);
        when(discoveryClient.getInstances("service")).thenReturn(Collections.singletonList(instance));

        when(instance.getServiceId()).thenReturn("service");
        when(instance.getUri()).thenReturn(URI.create("http://localhost:8089/service"));

        HttpClientResponse<ByteBuf> response = Mockito.mock(HttpClientResponse.class);
        when(RxNetty.createHttpGet("http://localhost:8089/service")).thenReturn(Observable.just(response));

        when(response.getStatus()).thenReturn(HttpResponseStatus.OK);
        ByteBuf byteBuf = (new PooledByteBufAllocator()).directBuffer();
        ByteBufUtil.writeUtf8(byteBuf, "No JSON here");
        when(response.getContent()).thenReturn(Observable.just(byteBuf));

        TestSubscriber<Node> testSubscriber = new TestSubscriber<>();
        indexesAggregator.aggregateNodes().toBlocking().subscribe(testSubscriber);
        testSubscriber.assertNoErrors();

        List<Node> nodes = testSubscriber.getOnNextEvents();
        assertThat(nodes).hasSize(0);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void malformedJsonIndexResponseShouldReturnZeroNodes() throws InterruptedException {
        when(discoveryClient.getServices()).thenReturn(Collections.singletonList("service"));
        ServiceInstance instance = Mockito.mock(ServiceInstance.class);
        when(discoveryClient.getInstances("service")).thenReturn(Collections.singletonList(instance));

        when(instance.getServiceId()).thenReturn("service");
        when(instance.getUri()).thenReturn(URI.create("http://localhost:8089/service"));

        HttpClientResponse<ByteBuf> response = Mockito.mock(HttpClientResponse.class);
        when(RxNetty.createHttpGet("http://localhost:8089/service")).thenReturn(Observable.just(response));

        when(response.getStatus()).thenReturn(HttpResponseStatus.OK);
        ByteBuf byteBuf = (new PooledByteBufAllocator()).directBuffer();
        ByteBufUtil.writeUtf8(byteBuf, "{}");
        when(response.getContent()).thenReturn(Observable.just(byteBuf));

        TestSubscriber<Node> testSubscriber = new TestSubscriber<>();
        indexesAggregator.aggregateNodes().toBlocking().subscribe(testSubscriber);
        testSubscriber.assertNoErrors();

        List<Node> nodes = testSubscriber.getOnNextEvents();
        assertThat(nodes).hasSize(0);
    }

    private void checkResource(Node resource, String id, String url, String docs) {
        assertThat(resource.getId()).isEqualTo(id);
        assertThat(resource.getLinkedToNodeIds()).contains("service");
        assertThat(resource.getLane()).isEqualTo(1);
        assertThat(resource.getDetails()).isNotEmpty();

        Map<String, Object> details = resource.getDetails();
        assertThat(details.get("status")).isEqualTo("UP");
        assertThat(details.get("type")).isEqualTo("RESOURCE");
        assertThat(details.get("url")).isEqualTo(url);
        assertThat(details.get("docs")).isEqualTo(docs);
    }

}
