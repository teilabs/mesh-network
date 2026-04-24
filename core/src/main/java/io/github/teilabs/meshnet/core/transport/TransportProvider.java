package io.github.teilabs.meshnet.core.transport;

import io.github.teilabs.meshnet.core.config.Config;
import io.github.teilabs.meshnet.core.frame.Frame;
import java.util.concurrent.CompletableFuture;

/**
 * Interface for managing all data incoming from other nodes and all data going
 * to other nodes.
 */
public interface TransportProvider {
    /**
     * Sends {@link Frame} to a specific node.
     * 
     * @param frame         frame to send
     * @param nodeRoutingId routing id of the node to send to
     */
    void sendFrame(Frame frame, long nodeRoutingId);

    /**
     * Sends {@link Frame} to all connected nodes.
     * 
     * @param frame frame to send
     */
    void sendFrameToEveryone(Frame frame);

    /**
     * Sends handshake to a specific node.
     * 
     * @param nodeRoutingId routing id of the node to send to
     * @return a future that will be completed when the response handshake is
     *         received with true, or with false if response handshake isn't
     *         received in {@link Config#handshakeTimeoutSec} seconds.
     */
    CompletableFuture<Boolean> sendHandshake(long nodeRoutingId);

    /**
     * Starts advertising with a specific interval between messages.
     * 
     * @param intervalMs interval in milliseconds
     */
    void startAdvertising(int intervalMs);

    /**
     * Stops advertising.
     */
    void stopAdvertising();

    /**
     * Processes received bytes.
     * 
     * @param bytes received bytes
     */
    void onBytesReceived(byte[] bytes);
}
