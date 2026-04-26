package io.github.teilabs.meshnet.core.routing;

import io.github.teilabs.meshnet.core.exception.MeshRoutingException;
import io.github.teilabs.meshnet.core.exception.MeshValidationException;
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
     * @throws MeshValidationException if the frame is invalid.
     * @throws MeshRoutingException if there's a routing error.
     */
    void onFrameReceived(Frame frame, long prevNodeRoutingId) throws MeshValidationException, MeshRoutingException;

    /**
     * Sends a {@link Frame frame}.
     * 
     * @param frame outgoing frame
     * @throws MeshValidationException if the frame is invalid.
     * @throws MeshRoutingException if there's a routing error.
     */
    void sendFrame(Frame frame) throws MeshValidationException, MeshRoutingException;
}
