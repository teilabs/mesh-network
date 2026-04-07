package io.github.teilabs.meshnet.core.routing;

public interface TunnelManager {
    Tunnel getTunnel(long dstRoutingId);

    void addTunnel(Tunnel tunnel);

    void removeTunnel(long dstRoutingId);
}
