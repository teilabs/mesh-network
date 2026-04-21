package io.github.teilabs.meshnet.core.transport;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Implementation of {@link NodesManager} using HashSet to store nodes.
 */
public class HashSetNodesManager implements NodesManager {
    // TODO: send handshakes to all nodes with some interval
    private final Set<Long> nodes = Collections.synchronizedSet(new HashSet<Long>());

    @Override
    public void addNode(long nodeRoutingId) {
        // Checks if node is already exists to prevent collisions
        if (nodes.contains(nodeRoutingId)) {
            throw new IllegalArgumentException("Node already stored");
        }

        nodes.add(nodeRoutingId);
    }

    @Override
    public void removeNode(long nodeRoutingId) {
        nodes.remove(nodeRoutingId);
    }

    @Override
    public boolean checkConnectionToNode(long nodeRoutingId) {
        if (!nodes.contains(nodeRoutingId)) {
            return false;
        }
        // TODO: maybe send handshake
        return true;
    }
}
