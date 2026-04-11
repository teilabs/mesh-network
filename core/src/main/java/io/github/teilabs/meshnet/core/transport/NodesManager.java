package io.github.teilabs.meshnet.core.transport;

public interface NodesManager {
    void addNode(long nodeRoutingId);

    void removeNode(long nodeRoutingId);

    boolean checkConnectionToNode(long nodeRoutingId);
}
