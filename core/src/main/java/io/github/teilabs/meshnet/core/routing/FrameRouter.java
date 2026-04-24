package io.github.teilabs.meshnet.core.routing;

import io.github.teilabs.meshnet.core.frame.Frame;

/**
 * Interface for managing incoming and outgoing {@link Frame frames}.
 */
public interface FrameRouter {
    /**
     * Processes incoming {@link Frame frame}.
     * 
     * @param frame incoming frame
     * @param prevNodeRoutingId routing id of the node from which we received the frame
     */
    void onFrameReceived(Frame frame, long prevNodeRoutingId);

    /**
     * Sends a {@link Frame frame}.
     * 
     * @param frame outgoing frame
     */
    void sendFrame(Frame frame);
}
