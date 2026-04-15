package io.github.teilabs.meshnet.core.transport;

import io.github.teilabs.meshnet.core.frame.Frame;
import java.util.concurrent.CompletableFuture;

/**
 * Interface for managing all data incoming from other nodes and all data going
 * to toher nodes.
 */
public interface TransportProvider {
    public static final int HANDSHAKE_TIMEOUT_SEC = 10;

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
     *         recieved with true, or with false if response handshake isn't
     *         recieved in {@value #HANDSHAKE_TIMEOUT_SEC} seconds.
     */
    CompletableFuture<Boolean> sendHandhsake(long nodeRoutingId);

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
