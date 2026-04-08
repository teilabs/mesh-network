package io.github.teilabs.meshnet.core.routing;

/**
 * Interface for storing {@link Tunnel tunnels} and finding tunnel to
 * destination node.
 */
public interface TunnelManager {
    /**
     * Gets tunnel to destination node.
     * 
     * @param dstRoutingId routing id of destination node
     * @return tunnel to destination node
     * @throws IllegalArgumentException if no tunnel to destination node exists
     */
    Tunnel getTunnel(long dstRoutingId) throws IllegalArgumentException;

    /**
     * Adds tunnel to destination node.
     * 
     * @param tunnel tunnel to destination node
     * @throws IllegalArgumentException if tunnel to destination node already exists
     */
    void addTunnel(Tunnel tunnel) throws IllegalArgumentException;

    /**
     * Removes tunnel to destination node.
     * 
     * @param dstRoutingId routing id of destination node
     * @param dstRoutingId
     */
    void removeTunnel(long dstRoutingId);
}
