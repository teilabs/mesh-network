package io.github.teilabs.meshnet.core.frame;

import io.github.teilabs.meshnet.core.exception.MeshProtocolException;

/**
 * Interface for parsing and serializing {@link Frame} according to its version.
 */
public interface FrameCodec {
    /**
     * Parses bytes to {@link Frame}.
     * 
     * @param rawFrame Raw frame bytes.
     * @return Parsed frame.
     * @throws MeshProtocolException if the frame version is unsupported or data is invalid.
     */
    Frame parse(byte[] rawFrame) throws MeshProtocolException;

    /**
     * Serializes {@link Frame} to bytes.
     * 
     * @param frame Frame to serialize.
     * @return Serialized frame bytes.
     * @throws MeshProtocolException if the frame version is unsupported.
     */
    byte[] serialize(Frame frame) throws MeshProtocolException;

    /**
     * Serializes only {@link Frame} header to bytes.
     * 
     * @param frame Frame to serialize.
     * @return Serialized frame header bytes.
     * @throws MeshProtocolException if the frame version is unsupported.
     */
    byte[] serializeHeader(Frame frame) throws MeshProtocolException;
}
