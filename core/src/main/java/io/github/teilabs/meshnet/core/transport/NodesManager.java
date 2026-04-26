package io.github.teilabs.meshnet.core.transport;

import io.github.teilabs.meshnet.core.exception.MeshValidationException;

/**
 * Interface for storing connected nodes.
 */
public interface NodesManager {
    /**
     * Adds a node to the list of connected nodes.
     * 
     * @param nodeRoutingId The routing ID of the node to add.
     * @throws MeshValidationException If the node is already stored.
     */
    void addNode(long nodeRoutingId) throws MeshValidationException;

    /**
     * Removes a node from the list of connected nodes.
     * 
     * @param nodeRoutingId The routing ID of the node to remove.
     */
    void removeNode(long nodeRoutingId);

    /**
     * Checks if this node have direct connection to the neighbor node.
     * 
     * @param nodeRoutingId The routing ID of the neighbor node.
     * @return true - if this node have direct connection to the neighbor node,
     *         false - otherwise.
     */
    boolean checkConnectionToNode(long nodeRoutingId);
}
