package be.ordina.msdashboard.events;

import java.io.Serializable;

/**
 * Simple node event when something goes wrong
 *
 * @author Andreas Evers
 */
public class NodeEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String nodeId;
    private String message;
    private Throwable throwable;

    public NodeEvent(final String nodeId) {
        this.nodeId = nodeId;
    }

    public String getNodeId() {
        return nodeId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public void setThrowable(Throwable throwable) {
        this.throwable = throwable;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NodeEvent nodeEvent = (NodeEvent) o;

        if (nodeId != null ? !nodeId.equals(nodeEvent.nodeId) : nodeEvent.nodeId != null) return false;
        if (message != null ? !message.equals(nodeEvent.message) : nodeEvent.message != null) return false;
        return throwable != null ? throwable.equals(nodeEvent.throwable) : nodeEvent.throwable == null;

    }

    @Override
    public int hashCode() {
        int result = nodeId != null ? nodeId.hashCode() : 0;
        result = 31 * result + (message != null ? message.hashCode() : 0);
        result = 31 * result + (throwable != null ? throwable.hashCode() : 0);
        return result;
    }
}
