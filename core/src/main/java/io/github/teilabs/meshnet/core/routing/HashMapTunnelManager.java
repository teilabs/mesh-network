package io.github.teilabs.meshnet.core.routing;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementation of {@link TunnelManager} using HashMap to store {@link Tunnel
 * tunnels}.
 */
public class HashMapTunnelManager implements TunnelManager {
    private final Map<Long, Tunnel> tunnels = new ConcurrentHashMap<>();

    @Override
    public Tunnel getTunnel(long dstRoutingId) {
        if (!tunnels.containsKey(dstRoutingId)) {
            throw new IllegalArgumentException("Tunnel not found");
        }
        return tunnels.get(dstRoutingId);
    }

    @Override
    public void addTunnel(Tunnel tunnel) {
        if (tunnels.containsKey(tunnel.getPath()[tunnel.getPath().length - 1])) {
            throw new IllegalArgumentException("Tunnel already exists");
        }
        // TODO: ask user for permission to open tunnel
        tunnels.computeIfAbsent(tunnel.getPath()[tunnel.getPath().length - 1], k -> tunnel);
    }

    @Override
    public void removeTunnel(long dstRoutingId) {
        tunnels.remove(dstRoutingId);
    }

}
