package io.github.teilabs.meshnet.core.routing;

import io.github.teilabs.meshnet.core.exception.MeshRoutingException;
import io.github.teilabs.meshnet.core.exception.MeshSecurityException;

/**
 * Interface for storing {@link Tunnel tunnels} and managing them.
 */
public interface TunnelManager {
    /**
     * Gets tunnel by id.
     * 
     * @param tunnelId id of a tunnel
     * @return tunnel with this id
     * @throws MeshRoutingException if no tunnel with this id
     */
    Tunnel getTunnel(long tunnelId) throws MeshRoutingException;

    /**
     * Adds tunnel. If tunnel with same id already exists, appIds from new tunnel
     * will be added to existing tunnel.
     * 
     * @param tunnel tunnel to add
     * @throws MeshRoutingException  if tunnel count is exceeded
     * @throws MeshSecurityException if tunnel open access is denied
     */
    void addTunnel(Tunnel tunnel) throws MeshRoutingException, MeshSecurityException;

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
     * @throws MeshRoutingException if no tunnel with this id
     */
    Tunnel getPendingTunnel(long tunnelId) throws MeshRoutingException;

    /**
     * Adds pending tunnel. If tunnel with same id already exists, appIds from new
     * tunnel will be added to existing tunnel.
     * 
     * @param tunnel pending tunnel to add
     * @throws MeshRoutingException if tunnel count is exceeded
     */
    void addPendingTunnel(Tunnel tunnel) throws MeshRoutingException;

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
