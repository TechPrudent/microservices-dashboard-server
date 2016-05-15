package be.ordina.msdashboard.aggregator.index;

import be.ordina.msdashboard.EnableMicroservicesDashboardServer;
import be.ordina.msdashboard.InMemoryMockedServices;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = { IndexesAggregatorTest.TestMicroservicesDashboardServerApplication.class, InMemoryMockedServices.class })
@WebIntegrationTest({ "server.port=0", "spring.cloud.config.enabled=false", "services.mock=true" })
public class IndexesAggregatorTest {

    private static final Logger logger = LoggerFactory.getLogger(IndexesAggregatorTest.class);

    @Autowired
    private IndexesAggregator indexesAggregator;

    @Test
    public void shouldReturnOneNode() {
        indexesAggregator.fetchIndexesWithObservable()
                         .subscribe(node -> {
                             assertThat(node).isNotNull();
                             logger.info("Node: {}", node);
                         });
    }

    @Configuration
    @EnableAutoConfiguration
    @EnableMicroservicesDashboardServer
    public static class TestMicroservicesDashboardServerApplication {
    }

}
