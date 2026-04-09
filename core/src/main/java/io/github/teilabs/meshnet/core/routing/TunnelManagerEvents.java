package io.github.teilabs.meshnet.core.routing;

/**
 * Functions that {@link TunnelManager} can call.
 */
public interface TunnelManagerEvents {
    /**
     * Asks user to open the tunnel.
     * 
     * @param tunnel tunnel that we want to open
     * @return true - if user accepted to open this tunnel, false - otherwise
     */
    boolean checkTunnelOpenAccess(Tunnel tunnel);
}
