/*
 * Copyright 2012-2016 the original author or authors.
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
package be.ordina.msdashboard.controllers;

import be.ordina.msdashboard.graph.GraphRetriever;
import be.ordina.msdashboard.model.Node;
import be.ordina.msdashboard.stores.NodeStore;
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
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class NodesControllerTest {

    @InjectMocks
    private NodesController nodesController;

    @Mock
    private GraphRetriever graphRetriever;

    @Mock
    private NodeStore redisService;

    @Test
    public void getDependenciesGraphJson() {
        doReturn(Collections.emptyMap()).when(graphRetriever).retrieve();

        HttpEntity<Map<String, Object>> httpEntity = nodesController.getDependenciesGraphJson();

        assertThat(httpEntity.getBody()).isEmpty();
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
