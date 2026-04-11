package io.github.teilabs.meshnet.core.transport;

import io.github.teilabs.meshnet.core.frame.Frame;
import java.util.concurrent.CompletableFuture;

public interface TransportProvider {
    void sendFrame(Frame frame, long nodeRoutingId);

    void sendFrameToEveryone(Frame frame);

    CompletableFuture<Boolean> sendHandhsake(long nodeRoutingId);

    void startAdvertising(int intervalMs);

    void stopAdvertising();

    void onBytesRecieved(byte[] bytes);
}
