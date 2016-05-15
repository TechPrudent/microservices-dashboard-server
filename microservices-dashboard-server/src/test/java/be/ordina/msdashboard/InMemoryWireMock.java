package be.ordina.msdashboard;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.common.SingleRootFileSource;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

public class InMemoryWireMock {

    private WireMockServer wireMockServer;

    @PostConstruct
    public void startWireMock() throws IOException {
        wireMockServer = new WireMockServer(wireMockConfig().port(8089).fileSource(new SingleRootFileSource("src/test/resources/mocks")));
        wireMockServer.start();
    }

    @PreDestroy
    public void stopWireMock() {
        wireMockServer.stop();
    }
}
