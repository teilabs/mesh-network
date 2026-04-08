package io.github.teilabs.meshnet.core.api;

import io.github.teilabs.meshnet.core.frame.Frame;

/** Interface for converting {@link Frame} to {@link MeshIncomingMessage} and {@link MeshOutgoingMessage} to {@link Frame}. */
public interface MeshMessageCodec {
    /**
     * Parse a {@link Frame} to {@link MeshIncomingMessage}.
     *
     * @param frame The frame to parse.
     * @return The parsed message.
     */
    MeshIncomingMessage parseIncomingFrame(Frame frame);

    /**
     * Generate a {@link Frame} from a {@link MeshOutgoingMessage}.
     *
     * @param message The message to generate the frame from.
     * @return The generated frame.
     */
    Frame generateOutgoingFrame(MeshOutgoingMessage message);
}
