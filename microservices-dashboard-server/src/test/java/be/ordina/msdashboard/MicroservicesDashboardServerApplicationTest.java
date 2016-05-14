package be.ordina.msdashboard;

import be.ordina.msdashboard.MicroservicesDashboardServerApplicationTest.TestMicroservicesDashboardServerApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author Andreas Evers
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = TestMicroservicesDashboardServerApplication.class)
@WebIntegrationTest({ "server.port=0", "spring.cloud.config.enabled=false" })
public class MicroservicesDashboardServerApplicationTest {

    @Value("${local.server.port}")
    private int port = 0;

    @Test
    public void contextLoads() {
    }

    @Configuration
    @EnableAutoConfiguration
    @EnableMicroservicesDashboardServer
    public static class TestMicroservicesDashboardServerApplication {
    }
}
