package io.github.teilabs.meshnet.core;

import io.github.teilabs.meshnet.core.api.MeshOutgoingMessage;

/**
 * Events that {@link MeshCore} can handle.
 */
public interface CoreInput {
    /**
     * Called by daemon when it recieves frame bytes from another node.
     * 
     * @param rawFrame recieved frame bytes
     */
    void onRawFrameRecieved(byte[] rawFrame);

    /**
     * Called by daemon when app want to send messgae through mesh network.
     * 
     * @param message message that app want to send
     */
    void onAppSendMessage(MeshOutgoingMessage message);
}
