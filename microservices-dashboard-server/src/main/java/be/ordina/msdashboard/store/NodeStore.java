package be.ordina.msdashboard.store;

import be.ordina.msdashboard.model.Node;

import java.util.Collection;

/**
 * @author Andreas Evers
 */
public interface NodeStore {

    String KEY_PREFIX = "virtual:";
    String VIRTUAL_FLAG = "virtual";

    Collection<Node> getAllNodes();

    void saveNode(String nodeData);

    void deleteNode(String nodeId);

    void deleteAllNodes();

    void flushDB();
}
