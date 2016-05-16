package be.ordina.msdashboard.aggregator.index;

import be.ordina.msdashboard.EnableMicroservicesDashboardServer;
import be.ordina.msdashboard.InMemoryMockedConfiguration;
import be.ordina.msdashboard.model.Node;
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
import rx.Subscriber;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = { IndexesAggregatorTest.TestMicroservicesDashboardServerApplication.class, InMemoryMockedConfiguration.class })
@WebIntegrationTest({ "server.port=0", "spring.cloud.config.enabled=false" })
public class IndexesAggregatorTest {

    private static final Logger logger = LoggerFactory.getLogger(IndexesAggregatorTest.class);

    @Autowired
    private IndexesAggregator indexesAggregator;

    @Test
    public void shouldReturnThreeNode() {
        indexesAggregator.fetchIndexesWithObservable()
                         .subscribe(new Subscriber<Node>() {

                             private int countNodes = 0;

                             @Override
                             public void onCompleted() {
                                 assertThat(countNodes).isEqualTo(3);
                             }

                             @Override
                             public void onError(Throwable e) {
                                 fail("should not reach here", e);
                             }

                             @Override
                             public void onNext(Node node) {
                                 ++countNodes;
                                 assertThat(node).isNotNull();
                                 logger.info("Node: {}", node);
                             }
                         });
    }

    @Configuration
    @EnableAutoConfiguration
    @EnableMicroservicesDashboardServer
    public static class TestMicroservicesDashboardServerApplication {
    }

}
