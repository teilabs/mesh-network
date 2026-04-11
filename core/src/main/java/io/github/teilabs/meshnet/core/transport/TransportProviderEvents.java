package io.github.teilabs.meshnet.core.transport;

import io.github.teilabs.meshnet.core.frame.Frame;

public interface TransportProviderEvents {
    void sendBytes(byte[] bytes, long nodeRoutingId);

    void sendBytesToEveryone(byte[] bytes);

    void onFrameRecieved(Frame frame);

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
