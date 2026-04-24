package io.github.teilabs.meshnet.core.config;

import io.github.teilabs.meshnet.core.frame.Frame;

/**
 * Interface for getting configuration values set up in client app.
 */
public interface Config {
    /**
     * Mode, configuring what should we do with incoming {@link Frame#TYPE_DATA data
     * frames} when
     * this node isn't final destination.
     */
    enum TransitMode {
        /**
         * No transit, frame will be dropped
         */
        NONE,
        /**
         * Frame will be transited, but without storing
         */
        RELAY,
        /**
         * Frame will be transited and stored fro distribution
         */
        STORE
    }

    /**
     * Mode, configuring what should we do with incoming
     * {@link Frame#TYPE_OPEN_TUNNEL tunnel frames} when
     * this node isn't final destination. Does not apply to frames in tunnels opened
     * before mode changing.
     */
    enum TunnelMode {
        /**
         * Reject tunnel opening through this node
         */
        NONE,
        /**
         * Support tunnel opening through this node
         */
        RELAY
    }

    /**
     * @return configured timeout before handshake will fail with timeout
     */
    int handshakeTimeoutSec();

    /**
     * @return configured count of tunnels, this node can have (including our tunnel
     *         and tunnels where we are transit node)
     */
    int maxTunnelsCount();

    /**
     * @return configured {@link TransitMode}
     */
    TransitMode transitMode();

    /**
     * @return configured {@link TunnelMode}
     */
    TunnelMode tunnelMode();

    /**
     * @return configured count of frames, this node can store for distributing
     */
    int maxStoredFrames();

    /**
     * @return configured path of folder where frames will be stored
     */
    String storedFramesFolderPath();
}