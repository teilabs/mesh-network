package io.github.teilabs.meshnet.core.routing;

/**
 * Interface for storing {@link Tunnel tunnels} and managing them.
 */
public interface TunnelManager {
    /**
     * Gets tunnel by id.
     * 
     * @param tunnelId id of a tunnel
     * @return tunnel with this id
     * @throws IllegalArgumentException if no tunnel with this id
     */
    Tunnel getTunnel(long tunnelId) throws IllegalArgumentException;

    /**
     * Adds tunnel. If tunnel with same id already exists, appIds from new tunnel
     * will be added to existing tunnel.
     * 
     * @param tunnel tunnel to add
     * @throws RuntimeException if tunnel can't be opened
     */
    void addTunnel(Tunnel tunnel) throws RuntimeException;

    /**
     * Removes tunnel. AppIds from given tunnel will be removed from existed tunnel
     * with same id and if after that appIds of existed tunnel will be empty, tunnel
     * will be removed.
     * 
     * @param tunnel tunnel to remove
     */
    void removeTunnel(Tunnel tunnel);

    /**
     * Checks if tunnel exist and its appIds contains given appIds.
     * 
     * @param tunnelId       id of tunnel to check
     * @param endpoint1AppId first appId in pair
     * @param endpoint2AppId second appId in pair
     * @return true - if tunnel exist and its appIds contains given appIds, false -
     *         otherwise
     */
    boolean containsTunnel(long tunnelId, short endpoint1AppId, short endpoint2AppId);

    /**
     * Gets pending tunnel by id.
     * 
     * @param tunnelId id of a tunnel
     * @return pending tunnel with this id
     * @throws IllegalArgumentException if no tunnel with this id
     */
    Tunnel getPendingTunnel(long tunnelId) throws IllegalArgumentException;

    /**
     * Adds pending tunnel. If tunnel with same id already exists, appIds from new
     * tunnel will be added to existing tunnel.
     * 
     * @param tunnel pending tunnel to add
     * @throws RuntimeException if tunnel can't be opened
     */
    void addPendingTunnel(Tunnel tunnel);

    /**
     * Removes pending tunnel. AppIds from given tunnel will be removed from existed
     * tunnel with same id and if after that appIds of existed tunnel will be empty,
     * tunnel will be removed.
     * 
     * @param tunnel pending tunnel to remove
     */
    void removePendingTunnel(Tunnel tunnel);

    /**
     * Checks if pending tunnel exist and its appIds contains given appIds.
     * 
     * @param tunnelId       id of pending tunnel to check
     * @param endpoint1AppId first appId in pair
     * @param endpoint2AppId second appId in pair
     * @return true - if pending tunnel exist and its appIds contains given appIds,
     *         false - otherwise
     */
    boolean containsPendingTunnel(long tunnelId, short endpoint1AppId, short endpoint2AppId);
}
