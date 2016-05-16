package be.ordina.msdashboard;

import be.ordina.msdashboard.MicroservicesDashboardServerApplicationTest.TestMicroservicesDashboardServerApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.TestRestTemplate;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

import static be.ordina.msdashboard.JsonHelper.load;
import static be.ordina.msdashboard.JsonHelper.removeBlankNodes;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Andreas Evers
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = { TestMicroservicesDashboardServerApplication.class, InMemoryMockedConfiguration.class })
@WebIntegrationTest({ "server.port=0", "spring.cloud.config.enabled=false" })
public class MicroservicesDashboardServerApplicationTest {

    @Value("${local.server.port}")
    private int port = 0;

    @Test
    public void contextLoads() {
    }

    @Test
    public void exposesGraph() throws IOException {
        long startTime = System.currentTimeMillis();
        @SuppressWarnings("rawtypes")
        ResponseEntity<String> entity = new TestRestTemplate()
                .getForEntity("http://localhost:" + port + "/graph", String.class);
        long totalTime = System.currentTimeMillis() - startTime;
        assertThat(HttpStatus.OK).isEqualTo(entity.getStatusCode());
        String body = entity.getBody();
        JSONAssert.assertEquals(removeBlankNodes(load("src/test/resources/MicroservicesDashboardServerApplicationTestResponse.json")),
                removeBlankNodes(body), JSONCompareMode.LENIENT);
        assertThat(totalTime).isLessThan(10000);
        // assertThat(totalTime).isLessThan(4500); // should be the case after reactive improvements
        System.out.println("Time spent waiting for /graph: " + totalTime);
    }

    @Configuration
    @EnableAutoConfiguration
    @ComponentScan(basePackages = "be.ordina")
    @EnableMicroservicesDashboardServer
    public static class TestMicroservicesDashboardServerApplication {
    }
}
