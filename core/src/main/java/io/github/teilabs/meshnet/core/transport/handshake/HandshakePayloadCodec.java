package io.github.teilabs.meshnet.core.transport.handshake;

/**
 * Interface for parsing and serializing {@link HandshakePayload}.
 */
public interface HandshakePayloadCodec {
    /**
     * Serializes {@link HandshakePayload} to bytes.
     * 
     * @param handshakePayload HandShakePayload to serialize.
     * @return Serialized HandShakePayload bytes.
     */
    byte[] serialize(HandshakePayload handshakePayload);

    /**
     * Parses bytes to {@link HandshakePayload}.
     * 
     * @param bytes HandShakePayload bytes.
     * @return Parsed HandShakePayload.
     */
    HandshakePayload parse(byte[] bytes);
}
