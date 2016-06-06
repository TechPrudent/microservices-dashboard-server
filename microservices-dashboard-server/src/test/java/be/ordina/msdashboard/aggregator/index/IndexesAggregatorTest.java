package be.ordina.msdashboard.aggregator.index;

import be.ordina.msdashboard.model.Node;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.nio.client.HttpAsyncClient;
import org.apache.http.nio.protocol.HttpAsyncRequestProducer;
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
import rx.apache.http.ObservableHttp;
import rx.apache.http.ObservableHttpResponse;
import rx.observers.TestSubscriber;

import java.net.URI;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(PowerMockRunner.class)
@PrepareForTest({HttpAsyncClients.class, ObservableHttp.class})
public class IndexesAggregatorTest {

    private DiscoveryClient discoveryClient;
    private IndexesAggregator indexesAggregator;

    @Before
    public void setup() {
        discoveryClient = Mockito.mock(DiscoveryClient.class);
        indexesAggregator = new IndexesAggregator(discoveryClient);
        PowerMockito.mockStatic(HttpAsyncClients.class);
        PowerMockito.mockStatic(ObservableHttp.class);
    }

    @Test
    public void shouldReturnThreeNodes() {
        Mockito.when(discoveryClient.getServices()).thenReturn(Collections.singletonList("service"));
        ServiceInstance instance = Mockito.mock(ServiceInstance.class);
        Mockito.when(discoveryClient.getInstances("service")).thenReturn(Collections.singletonList(instance));

        Mockito.when(instance.getUri()).thenReturn(URI.create("http://localhost:8089/service"));

        CloseableHttpAsyncClient client = Mockito.mock(CloseableHttpAsyncClient.class);
        Mockito.when(HttpAsyncClients.createDefault()).thenReturn(client);
        ObservableHttp<ObservableHttpResponse> observableHttp = Mockito.mock(ObservableHttp.class);
        Mockito.when(ObservableHttp.createRequest(Mockito.any(HttpAsyncRequestProducer.class), Mockito.eq(client))).thenReturn(observableHttp);

        ObservableHttpResponse observableHttpResponse = Mockito.mock(ObservableHttpResponse.class);
        Mockito.when(observableHttp.toObservable()).thenReturn(Observable.just(observableHttpResponse));

        HttpResponse httpResponse = Mockito.mock(HttpResponse.class);
        Mockito.when(observableHttpResponse.getResponse()).thenReturn(httpResponse);
        StatusLine statusLine = Mockito.mock(StatusLine.class);
        Mockito.when(httpResponse.getStatusLine()).thenReturn(statusLine);
        Mockito.when(statusLine.getStatusCode()).thenReturn(200);

        String response = "{\n" +
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
                "}";
        Mockito.when(observableHttpResponse.getContent()).thenReturn(Observable.just(response.getBytes()));

        TestSubscriber<Node> testSubscriber = new TestSubscriber<>();
        indexesAggregator.fetchIndexesAsObservable().toBlocking().subscribe(testSubscriber);
        testSubscriber.assertNoErrors();

        List<Node> nodes = testSubscriber.getOnNextEvents();
        assertThat(nodes).hasSize(3);

        Iterator<Node> iterator = nodes.iterator();
        Node serviceNode = iterator.next();
        assertThat(serviceNode.getId()).isEqualTo("service");
        assertThat(serviceNode.getLinkedFromNodeIds()).contains("svc1:svc1rsc1", "svc1:svc1rsc2");

        checkResource(iterator.next(), "svc1:svc1rsc1", "http://host0015.local:8301/svc1rsc1", "http://localhost:8089/service/generated-docs/api-guide.html#resources-svc1rsc1");
        checkResource(iterator.next(), "svc1:svc1rsc2", "http://host0015.local:8301/svc1rsc2", "http://localhost:8089/service/generated-docs/api-guide.html#resources-svc1rsc2");

        PowerMockito.verifyStatic();
        ObservableHttp.createRequest(Mockito.any(HttpAsyncRequestProducer.class), Mockito.any(HttpAsyncClient.class));
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
