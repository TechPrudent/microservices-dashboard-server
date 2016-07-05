package be.ordina.msdashboard;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.common.SingleRootFileSource;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import java.io.IOException;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

public class InMemoryWireMock {

    @Value("${eureka.client.serviceUrl.defaultZone")
    private String eurekaPath;
    //: http://localhost:7878/serviceregistry/eureka/}

    private WireMockServer wireMockServer;

    @PostConstruct
    public void startWireMock() throws IOException {
        wireMockServer = new WireMockServer(wireMockConfig().port(8089).fileSource(new SingleRootFileSource("src/main/resources/mocks")));
        wireMockServer.start();
    }

    @PreDestroy
    public void stopWireMock() {
        wireMockServer.stop();
    }
}
