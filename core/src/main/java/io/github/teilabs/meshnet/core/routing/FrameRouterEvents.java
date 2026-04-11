package io.github.teilabs.meshnet.core.routing;

import io.github.teilabs.meshnet.core.api.MeshIncomingMessage;
import io.github.teilabs.meshnet.core.buffer.FrameBuffer;

/**
 * Functions that {@link FrameBuffer} can call.
 */
public interface FrameRouterEvents {
    /**
     * Sends information to the destination app that message for it come and gives
     * the message.
     * 
     * @param message message to transfer
     */
    void transferMessageToApp(MeshIncomingMessage message);
}
