package io.github.teilabs.meshnet.core.transport;

import io.github.teilabs.meshnet.core.frame.Frame;

/**
 * Functions that {@link TransportProvider} can call.
 */
public interface TransportProviderEvents {
    /**
     * Sends bytes to everyone.
     *
     * @param bytes bytes to send
     */
    void sendBytesToEveryone(byte[] bytes);

    /**
     * Processes the {@link Frame} received from a specific node.
     *
     * @param frame             received frame
     * @param prevNodeRoutingId routing id of the node that sent the frame
     */
    void onFrameReceived(Frame frame, long prevNodeRoutingId);

    /**
     * Starts advertising bytes on all available interfaces to connect new nodes.
     *
     * @param bytes      bytes to advertise
     * @param intervalMs interval between advertisement
     */
    void startAdvertising(byte[] bytes, int intervalMs);

    /**
     * Stops advertising bytes.
     */
    void stopAdvertising();
}
