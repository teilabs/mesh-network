package io.github.teilabs.meshnet.core.routing;

import io.github.teilabs.meshnet.core.api.MeshIncomingMessage;
import io.github.teilabs.meshnet.core.buffer.FrameBuffer;
import io.github.teilabs.meshnet.core.frame.Frame;

/**
 * Functions that {@link FrameBuffer} can call.
 */
public interface FrameRouterEvents {
    /**
     * Sends a frame to a specific node.
     *
     * @param frame frame to send
     * @param nodeRoutingId    routingId of node to send
     */
    void sendFrame(Frame frame, long nodeRoutingId);

    /**
     * Sends a frame to all connected nodes.
     *
     * @param frame frame to send
     */
    void sendFrameToEveryone(Frame frame);

    /**
     * Sends information to the destination app that message for it come and gives the message.
     * 
     * @param message message to transfer
     */
    void transferMessageToApp(MeshIncomingMessage message);

    /**
     * Checks if node is connected 
     * @param nodeRoutingId routing id of node to check
     * @return true - if node is connecte, false - otherwise
     */
    boolean checkConnectionToNode(long nodeRoutingId);
}
