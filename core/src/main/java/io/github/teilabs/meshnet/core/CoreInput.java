package io.github.teilabs.meshnet.core;

import io.github.teilabs.meshnet.core.api.MeshOutgoingMessage;

/**
 * Events that {@link MeshCore} can handle.
 */
public interface CoreInput {
    /**
     * Called by daemon when it receives bytes from another node.
     * 
     * @param bytes received bytes
     */
    void onBytesReceived(byte[] bytes);

    /**
     * Called by daemon when app want to send message through mesh network.
     * 
     * @param message message that app want to send
     */
    void onAppSendMessage(MeshOutgoingMessage message);
}
