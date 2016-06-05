package be.ordina.msdashboard;

import be.ordina.msdashboard.MicroservicesDashboardServerApplicationTest.TestMicroservicesDashboardServerApplication;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.util.Arrays;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.SpringRunner;
import rx.Observable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import static be.ordina.msdashboard.JsonHelper.load;
import static be.ordina.msdashboard.JsonHelper.removeBlankNodes;
import static be.ordina.msdashboard.constants.Constants.ID;
import static be.ordina.msdashboard.constants.Constants.LINKS;
import static be.ordina.msdashboard.constants.Constants.NODES;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

/**
 * @author Andreas Evers
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = RANDOM_PORT, properties = { "spring.cloud.config.enabled=false" },
        classes = { TestMicroservicesDashboardServerApplication.class, InMemoryMockedConfiguration.class })
public class MicroservicesDashboardServerApplicationTest {

    @Value("${local.server.port}")
    private int port = 0;

    @Test
    public void contextLoads() {
    }

    @Test
    public void exposesGraph() throws IOException, InterruptedException {
        long startTime = System.currentTimeMillis();
        @SuppressWarnings("rawtypes")
        ResponseEntity<String> entity = new TestRestTemplate()
                .getForEntity("http://localhost:" + port + "/graph", String.class);
        long totalTime = System.currentTimeMillis() - startTime;
        assertThat(HttpStatus.OK).isEqualTo(entity.getStatusCode());
        String body = removeBlankNodes(entity.getBody());
        // System.out.println("BODY: " + body);
        System.out.println("Time spent waiting for /graph: " + totalTime);
        JSONAssert.assertEquals(removeBlankNodes(load("src/test/resources/MicroservicesDashboardServerApplicationTestResponse.json")),
                body, JSONCompareMode.LENIENT);
        ObjectMapper m = new ObjectMapper();
        Map<String, List> r = m.readValue(body, Map.class);
        assertLinkBetweenIds(r, "svc1:svc1rsc1", "service1");
        assertLinkBetweenIds(r, "svc1:svc1rsc2", "service1");
        assertLinkBetweenIds(r, "svc1:svc1rsc3", "service1");
        assertLinkBetweenIds(r, "svc3:svc3rsc1", "service3");
        assertLinkBetweenIds(r, "svc3:svc3rsc2", "service3");
        assertLinkBetweenIds(r, "svc4:svc4rsc1", "service4");
        assertLinkBetweenIds(r, "svc4:svc4rsc2", "service4");
        assertLinkBetweenIds(r, "service1", "backend2");
        assertLinkBetweenIds(r, "service1", "discoveryComposite");
        assertLinkBetweenIds(r, "service1", "backend1");
        assertLinkBetweenIds(r, "service1", "svc3:svc3rsc1");
        assertLinkBetweenIds(r, "service1", "svc4:svc4rsc1");
        assertLinkBetweenIds(r, "service3", "discoveryComposite");
        assertLinkBetweenIds(r, "service3", "backend1");
        assertLinkBetweenIds(r, "service3", "backend3");
        assertLinkBetweenIds(r, "service3", "backend4");
        assertLinkBetweenIds(r, "service4", "discoveryComposite");
        assertLinkBetweenIds(r, "service4", "backend4");
        assertLinkBetweenIds(r, "service4", "backend10");
        assertLinkBetweenIds(r, "service4", "loyalty-program");
        assertLinkBetweenIds(r, "service4", "backend9");
        assertLinkBetweenIds(r, "service4", "db");
        assertThat(((List<Map>) r.get(LINKS)).size()).isEqualTo(22);
        assertThat(totalTime).isLessThan(10000);
        // assertThat(totalTime).isLessThan(4500); // should be the case after reactive improvements
    }

    private static void assertLinkBetweenIds(Map<String, List> r, String source, String target) throws IOException {
        List<Object> nodes = (List<Object>) r.get(NODES);
        List<Map<String, Integer>> links = (List<Map<String, Integer>>) r.get(LINKS);
        int sourceId = -1;
        int targetId = -1;
        for (int i = 0; i < nodes.size(); i++) {
            if (((Map) nodes.get(i)).get(ID).equals(source)) {
                sourceId = i;
            } else if (((Map) nodes.get(i)).get(ID).equals(target)) {
                targetId = i;
            }
        }
        final int s = sourceId;
        final int t = targetId;
        assertThat(links.stream().anyMatch(link -> link.get("source") == s && link.get("target") == t)).isTrue();
    }

    @Configuration
    @EnableAutoConfiguration
    @ComponentScan(basePackages = "be.ordina")
    @EnableMicroservicesDashboardServer
    public static class TestMicroservicesDashboardServerApplication {

        public static void main(String[] args) {
        }
    }
}