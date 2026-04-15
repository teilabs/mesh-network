package io.github.teilabs.meshnet.core.routing;

import io.github.teilabs.meshnet.core.frame.Frame;

/**
 * Interface for managing incoming and outgoin {@link Frame frames}.
 */
public interface FrameRouter {
    /**
     * Processes incoming {@link Frame frame}.
     * 
     * @param frame incoming frame
     */
    void onFrameReceived(Frame frame, long prevNodeRoutingId);

    /**
     * Sends a {@link Frame frame}.
     * 
     * @param frame outgoing frame
     */
    void sendFrame(Frame frame);
}
