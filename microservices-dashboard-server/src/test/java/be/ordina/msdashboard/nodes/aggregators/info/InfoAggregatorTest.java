package be.ordina.msdashboard.nodes.aggregators.info;

import java.net.URI;
import java.util.Map;

import org.junit.Test;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.reactive.function.client.WebClient;

public class InfoAggregatorTest {

    @Test
    public void getInfoNodesFromService() {
        ParameterizedTypeReference<Map<String, Object>> typeRef = new ParameterizedTypeReference<Map<String, Object>>() {};

        WebClient
                .create()
                .get()
                .uri(URI.create("http://localhost:8081/actuator/info"))
                .retrieve()
                .bodyToFlux(typeRef)
                .subscribe(System.out::println);

    }
}
