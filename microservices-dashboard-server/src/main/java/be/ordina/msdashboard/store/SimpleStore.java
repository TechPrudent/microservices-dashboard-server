package be.ordina.msdashboard.store;

import be.ordina.msdashboard.converters.JsonToObjectConverter;
import be.ordina.msdashboard.model.Node;

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
