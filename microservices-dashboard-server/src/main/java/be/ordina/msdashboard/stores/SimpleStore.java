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
package be.ordina.msdashboard.stores;

import be.ordina.msdashboard.converters.JsonToObjectConverter;
import be.ordina.msdashboard.model.Node;
import rx.Observable;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Andreas Evers
 */
public class SimpleStore implements NodeStore {

    private final ConcurrentHashMap<String, Node> map = new ConcurrentHashMap<>();

    @Override
    public Collection<Node> getAllNodes() {
        return map.values();
    }

    @Override
    public Observable<Node> getAllNodesAsObservable() {
        return Observable.from(getAllNodes());
    }

    @Override
    public void saveNode(String nodeData) {
        Node node = getNode(nodeData);
        String nodeId = node.getId();
        node.getDetails().put(VIRTUAL_FLAG, true);
        map.put(KEY_PREFIX + nodeId, node);
    }

    @Override
    public void deleteNode(String nodeId) {
        map.remove(nodeId);
    }

    @Override
    public void deleteAllNodes() {
        map.clear();
    }

    @Override
    public void flushDB() {
        map.clear();
    }

    private Node getNode(String nodeData) {
        JsonToObjectConverter<Node> converter = new JsonToObjectConverter<>(Node.class);
        return converter.convert(nodeData);
    }
}
