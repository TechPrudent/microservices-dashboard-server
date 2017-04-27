/*
 * Copyright 2012-2017 the original author or authors.
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
package be.ordina.msdashboard.wiremock;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.SingleRootFileSource;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

public class InMemoryOAuth2WireMock {

    private final static int HTTP_PORT = 5085;
    private final static int HTTPS_HTTP_PORT = 5087;
    private final static int HTTPS_PORT = 5086;

    private WireMockServer eurekaServer;
    private WireMockServer secureServer;

    @PostConstruct
    public void startServers() throws IOException {
        WireMockConfiguration eurekaServerConfig = wireMockConfig().port(HTTP_PORT).fileSource(new SingleRootFileSource("src/test/resources/mocks/eureka"));
        eurekaServer = new WireMockServer(eurekaServerConfig);
        stubEureka();
        eurekaServer.start();


        WireMockConfiguration secureConfig = wireMockConfig().port(HTTPS_HTTP_PORT).httpsPort(HTTPS_PORT).fileSource(new SingleRootFileSource("src/test/resources/mocks/secure"));
        secureServer = new WireMockServer(secureConfig);
        stubPact();
        secureServer.start();

    }

    @PreDestroy
    public void stopServers() {
        eurekaServer.stop();
        secureServer.stop();
    }

    public void stubPact() {
        secureServer.stubFor(WireMock.get(urlPathMatching("/pacts/latest"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withFixedDelay(2000)
                        .withBodyFile("pacts-latest-oauth2.json")
                        .withHeader("Content-Type", "application/json")));
    }

    public void stubEureka() {
        eurekaServer.stubFor(WireMock.get(urlPathMatching("/eureka/apps/"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withFixedDelay(2000)
                        .withBodyFile("get_eureka_apps_oauth2.json")
                        .withHeader("Content-Type", "application/json")));
    }
}
