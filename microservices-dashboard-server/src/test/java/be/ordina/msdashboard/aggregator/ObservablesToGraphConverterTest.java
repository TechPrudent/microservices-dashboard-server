package be.ordina.msdashboard.aggregator;

import be.ordina.msdashboard.aggregator.health.HealthIndicatorsAggregator;
import be.ordina.msdashboard.aggregator.index.IndexesAggregator;
import be.ordina.msdashboard.aggregator.pact.PactsAggregator;
import be.ordina.msdashboard.converters.ObjectToJsonConverter;
import be.ordina.msdashboard.model.Node;
import be.ordina.msdashboard.model.NodeBuilder;
import be.ordina.msdashboard.store.NodeStore;
import com.google.common.collect.Sets;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import rx.Observable;

import java.io.FileNotFoundException;
import java.util.Map;

import static be.ordina.msdashboard.JsonHelper.load;
import static be.ordina.msdashboard.JsonHelper.removeBlankNodes;
import static be.ordina.msdashboard.model.NodeBuilder.node;
import static com.google.common.collect.Sets.newHashSet;

/**
 * @author Andreas Evers
 */
@RunWith(MockitoJUnitRunner.class)
public class ObservablesToGraphConverterTest {

    @InjectMocks
    private ObservablesToGraphConverter observablesToGraphConverter;

    @Mock
    private HealthIndicatorsAggregator healthIndicatorsAggregator;
    @Mock
    private IndexesAggregator indexesAggregator;
    @Mock
    private PactsAggregator pactsAggregator;
    @Mock
    private NodeStore redisService;

    @Test
    public void retrieveGraph() throws FileNotFoundException {
        Mockito.when(healthIndicatorsAggregator.fetchCombinedDependencies())
                .thenReturn(node().withId("masterNode").havingLinkedToNodes(newHashSet(
                        node().withId("service1").havingLinkedToNodeIds(newHashSet("backend1")).build(),
                        node().withId("service2").havingLinkedToNodeIds(newHashSet("backend2")).build(),
                        node().withId("backend1").build(),
                        node().withId("backend2").build())).build());
        Mockito.when(indexesAggregator.fetchIndexesWithObservable())
                .thenReturn(Observable.from(newHashSet(
                        node().withId("svc1rsc1").havingLinkedToNodeIds(newHashSet("service1")).build(),
                        node().withId("svc1rsc2").havingLinkedToNodeIds(newHashSet("service1")).build(),
                        node().withId("svc2rsc1").havingLinkedToNodeIds(newHashSet("service2")).build(),
                        node().withId("service1").withDetail("test", "test").build(),
                        node().withId("service2").build())));
        Mockito.when(pactsAggregator.fetchPactNodesAsObservable())
                .thenReturn(Observable.from(newHashSet(
                        node().withId("svc1rsc2").build(),
                        node().withId("svc2rsc1").build(),
                        node().withId("service1").havingLinkedToNodeIds(newHashSet("svc2rsc1")).build(),
                        node().withId("service2").havingLinkedToNodeIds(newHashSet("svc1rsc2")).build())));
        Mockito.when(redisService.getAllNodes())
                .thenReturn(newHashSet());

        Map<String, Object> graph = observablesToGraphConverter.build();

        ObjectToJsonConverter<Map<String, Object>> converter = new ObjectToJsonConverter<>();
        String nodeAsJson = converter.convert(graph);
        JSONAssert.assertEquals(removeBlankNodes(load("src/test/resources/ObservablesToGraphConverterTestResponse.json")),
                removeBlankNodes(nodeAsJson), JSONCompareMode.LENIENT);
    }
}
