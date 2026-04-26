package io.github.teilabs.meshnet.core.transport;

import io.github.teilabs.meshnet.core.exception.MeshValidationException;
import io.github.teilabs.meshnet.core.util.Logger;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Implementation of {@link NodesManager} using HashSet to store nodes.
 */
public class HashSetNodesManager implements NodesManager {
    private static final String TAG = "HashSetNodesManager";

    // TODO: send handshakes to all nodes with some interval
    private final Set<Long> nodes = Collections.synchronizedSet(new HashSet<Long>());

    private final Logger logger;

    public HashSetNodesManager(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void addNode(long nodeRoutingId) throws MeshValidationException {
        // Checks if node is already exists to prevent collisions
        if (nodes.contains(nodeRoutingId)) {
            logger.w(TAG, "Node already stored: " + nodeRoutingId);
            throw new MeshValidationException("Node already stored");
        }

        nodes.add(nodeRoutingId);
        logger.i(TAG, "Node added: " + nodeRoutingId);
    }

    @Override
    public void removeNode(long nodeRoutingId) {
        boolean removed = nodes.remove(nodeRoutingId);
        if (removed) {
            logger.i(TAG, "Node removed: " + nodeRoutingId);
            return;
        }
        logger.w(TAG, "Attempted to remove unknown node: " + nodeRoutingId);
    }

    @Override
    public boolean checkConnectionToNode(long nodeRoutingId) {
        if (!nodes.contains(nodeRoutingId)) {
            logger.d(TAG, "No direct connection to node: " + nodeRoutingId);
            return false;
        }
        // TODO: maybe send handshake
        logger.d(TAG, "Direct connection confirmed for node: " + nodeRoutingId);
        return true;
    }
}
