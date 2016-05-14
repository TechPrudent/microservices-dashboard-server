package be.ordina.msdashboard.controllers;

import be.ordina.msdashboard.cache.CacheCleaningBean;
import be.ordina.msdashboard.model.Node;
import be.ordina.msdashboard.services.DependenciesResourceService;
import be.ordina.msdashboard.store.NodeStore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpEntity;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class NodesControllerTest {

    @InjectMocks
    private NodesController nodesController;

    @Mock
    private DependenciesResourceService dependenciesResourceService;

    @Mock
    private NodeStore redisService;

    @Mock
    private CacheCleaningBean cacheCleaningBean;

    @Test
    public void getDependenciesGraphJson() {
        doReturn(Collections.emptyMap()).when(dependenciesResourceService).getDependenciesGraphResourceJson();

        HttpEntity<Map<String, Object>> httpEntity = nodesController.getDependenciesGraphJson();

        assertThat(httpEntity.getBody()).isEmpty();
    }

    @Test
    public void getDependenciesJson() {
        doReturn(new Node()).when(dependenciesResourceService).getDependenciesResourceJson();

        HttpEntity<Node> httpEntity = nodesController.getDependenciesJson();

        assertThat(httpEntity.getBody()).isNotNull();
    }

    @Test
    public void saveNode() {
        nodesController.saveNode("nodeAsJson");

        verify(redisService).saveNode("nodeAsJson");
    }

    @Test
    public void getAllNodes() {
        doReturn(Collections.emptyList()).when(redisService).getAllNodes();

        Collection<Node> nodes = nodesController.getAllNodes();

        assertThat(nodes).isEmpty();
    }

    @Test
    public void deleteNode() {
        nodesController.deleteNode("nodeId");

        verify(redisService).deleteNode("nodeId");
    }

    @Test
    public void deleteAllNodes() {
        nodesController.deleteAllNodes();

        verify(redisService).deleteAllNodes();
    }

    @Test
    public void flushAll() {
        nodesController.flushAll();

        verify(redisService).flushDB();
    }

    @Test
    public void evictCache() {
        nodesController.evictCache();

    }

}
