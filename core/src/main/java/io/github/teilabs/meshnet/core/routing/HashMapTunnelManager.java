package io.github.teilabs.meshnet.core.routing;

import io.github.teilabs.meshnet.core.crypto.Ed25519KeyPair;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import kotlin.Pair;

/**
 * Implementation of {@link TunnelManager} using HashMap to store {@link Tunnel
 * tunnels}.
 */
public class HashMapTunnelManager implements TunnelManager {
    private final TunnelManagerEvents tunnelManagerEvents;

    private final Ed25519KeyPair keyPair;

    private final Map<Long, Tunnel> tunnels = new ConcurrentHashMap<>();

    private final Map<Long, Tunnel> pendingTunnels = new ConcurrentHashMap<>();

    public HashMapTunnelManager(TunnelManagerEvents tunnelManagerEvents, Ed25519KeyPair keyPair) {
        this.tunnelManagerEvents = tunnelManagerEvents;
        this.keyPair = keyPair;
    }

    @Override
    public Tunnel getTunnel(long tunnelId) {
        if (!tunnels.containsKey(tunnelId)) {
            throw new IllegalArgumentException("Tunnel not found");
        }
        return tunnels.get(tunnelId);
    }

    @Override
    public void addTunnel(Tunnel tunnel) {
        // If current node is one of tunnel endpoints, we must request permission to
        // open tunnel
        if ((tunnel.getEndpoint2RoutingId() == keyPair.routingId()
                || tunnel.getEndpoint1RoutingId() == keyPair.routingId())
                && !tunnelManagerEvents.checkTunnelOpenAccess(tunnel)) {
            throw new RuntimeException("Tunnel open acces denied");
        }

        tunnels.computeIfPresent(
                Tunnel.generateTunnelId(tunnel.getEndpoint1RoutingId(), tunnel.getEndpoint2RoutingId()),
                (k, v) -> {
                    // If tunnel with this tunnelId already exists, we should add all new app pairs
                    // to it and rewrite it in storage
                    Set<Pair<Short, Short>> appIds = Collections.synchronizedSet(v.getAppIds());
                    tunnel.getAppIds().forEach((p) -> appIds.add(p));
                    return new Tunnel(v.getEndpoint1RoutingId(), v.getEndpoint2RoutingId(), v.getPrevRoutingId(),
                            v.getNextRoutingId(), appIds);
                });
        tunnels.computeIfAbsent(Tunnel.generateTunnelId(tunnel.getEndpoint1RoutingId(), tunnel.getEndpoint2RoutingId()),
                k -> tunnel);
    }

    @Override
    public void removeTunnel(Tunnel tunnel) {
        tunnels.computeIfPresent(
                Tunnel.generateTunnelId(tunnel.getEndpoint1RoutingId(), tunnel.getEndpoint2RoutingId()),
                (k, v) -> {
                    // Remove from tunnel all app pairs that we want to delete
                    Set<Pair<Short, Short>> appIds = Collections.synchronizedSet(v.getAppIds());
                    tunnel.getAppIds().forEach((p) -> appIds.remove(p));

                    // If there are no more app pairs, remove tunnel
                    if (appIds.isEmpty())
                        return null;

                    // Otherwise, rewrite it in storage
                    return new Tunnel(v.getEndpoint1RoutingId(), v.getEndpoint2RoutingId(), v.getPrevRoutingId(),
                            v.getNextRoutingId(), appIds);
                });

    }

    @Override
    public boolean containsTunnel(long tunnelId, short endpoint1AppId, short endpoint2AppId) {
        if (!tunnels.containsKey(tunnelId)) {
            return false;
        }
        return tunnels.get(tunnelId).getAppIds().contains(new Pair<Short, Short>(endpoint1AppId, endpoint2AppId));
    }

    @Override
    public Tunnel getPendingTunnel(long tunnelId) throws IllegalArgumentException {
        if (!pendingTunnels.containsKey(tunnelId)) {
            throw new IllegalArgumentException("Tunnel not found");
        }
        return pendingTunnels.get(tunnelId);
    }

    @Override
    public void addPendingTunnel(Tunnel tunnel) throws RuntimeException {
        pendingTunnels.computeIfPresent(
                Tunnel.generateTunnelId(tunnel.getEndpoint1RoutingId(), tunnel.getEndpoint2RoutingId()),
                (k, v) -> {
                    // If tunnel with this tunnelId already exists, we should add all new app pairs
                    // to it and rewrite it in storage
                    Set<Pair<Short, Short>> appIds = Collections.synchronizedSet(v.getAppIds());
                    tunnel.getAppIds().forEach((p) -> appIds.add(p));
                    return new Tunnel(v.getEndpoint1RoutingId(), v.getEndpoint2RoutingId(), v.getPrevRoutingId(),
                            v.getNextRoutingId(), appIds);
                });
        pendingTunnels.computeIfAbsent(
                Tunnel.generateTunnelId(tunnel.getEndpoint1RoutingId(), tunnel.getEndpoint2RoutingId()),
                k -> tunnel);
    }

    @Override
    public void removePendingTunnel(Tunnel tunnel) {
        pendingTunnels.computeIfPresent(
                Tunnel.generateTunnelId(tunnel.getEndpoint1RoutingId(), tunnel.getEndpoint2RoutingId()),
                (k, v) -> {
                    // Remove from tunnel all app pairs that we want to delete
                    Set<Pair<Short, Short>> appIds = Collections.synchronizedSet(v.getAppIds());
                    tunnel.getAppIds().forEach((p) -> appIds.remove(p));

                    // If there are no more app pairs, remove tunnel
                    if (appIds.isEmpty())
                        return null;

                    // Otherwise, rewrite it in storage
                    return new Tunnel(v.getEndpoint1RoutingId(), v.getEndpoint2RoutingId(), v.getPrevRoutingId(),
                            v.getNextRoutingId(), appIds);
                });
    }

    @Override
    public boolean containsPendingTunnel(long tunnelId, short endpoint1AppId, short endpoint2AppId) {
        if (!pendingTunnels.containsKey(tunnelId)) {
            return false;
        }
        return pendingTunnels.get(tunnelId).getAppIds()
                .contains(new Pair<Short, Short>(endpoint1AppId, endpoint2AppId));
    }
}
