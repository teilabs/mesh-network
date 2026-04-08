package io.github.teilabs.meshnet.core.frame;

/**
 * Interface for parsing and serializing {@link Frame} according to its version.
 */
public interface FrameCodec {
    /**
     * Parses bytes to {@link Frame}.
     * 
     * @param rawFrame Raw frame bytes.
     * @return Parsed frame.
     */
    Frame parse(byte[] rawFrame);

    /**
     * Serializes {@link Frame} to bytes.
     * 
     * @param frame Frame to serialize.
     * @return Serialized frmae bytes.
     */
    byte[] serialize(Frame frame);

    /**
     * Serializes only {@link Frame} header to bytes.
     * 
     * @param frame Frame to serialize.
     * @return Serialized frame header bytes.
     */
    byte[] serializeHeader(Frame frame);
}
