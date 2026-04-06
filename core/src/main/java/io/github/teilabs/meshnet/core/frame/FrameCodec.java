package io.github.teilabs.meshnet.core.frame;

/** Interface for parsing and serializing {@link Frame} according to its version. */
public interface FrameCodec {
    /** Parses bytes to {@link Frame}. */
    Frame parse(byte[] rawFrame);

    /** Serializes {@link Frame} to bytes. */
    byte[] serialize(Frame frame);
    
    /** Serializes only {@link Frame} header to bytes. */
    byte[] serializeHeader(Frame frame);
}
