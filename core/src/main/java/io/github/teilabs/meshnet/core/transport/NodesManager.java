package io.github.teilabs.meshnet.core.transport;

/**
 * Interface for storing connected nodes.
 */
public interface NodesManager {
    /**
     * Adds a node to the list of connected nodes.
     * 
     * @param nodeRoutingId The routing ID of the node to add.
     * @throws IllegalArgumentException If the node is already stored.
     */
    void addNode(long nodeRoutingId) throws IllegalArgumentException;

    /**
     * Removes a node from the list of connected nodes.
     * 
     * @param nodeRoutingId The routing ID of the node to remove.
     */
    void removeNode(long nodeRoutingId);

    /**
     * Checks if this node have direct connection to the neighbour node.
     * 
     * @param nodeRoutingId The routing ID of the neighbour node.
     * @return true - if this node have direct connection to the neighbour node, false - otherwise.
     */
    boolean checkConnectionToNode(long nodeRoutingId);
}
