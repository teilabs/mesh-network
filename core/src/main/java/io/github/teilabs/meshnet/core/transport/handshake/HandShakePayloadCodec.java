package io.github.teilabs.meshnet.core.transport.handshake;

/**
 * Interface for parsing and serializing {@link HandShakePayload}.
 */
public interface HandShakePayloadCodec {
    /**
     * Serializes {@link HandShakePayload} to bytes.
     * 
     * @param handShakePayload HandShakePayload to serialize.
     * @return Serialized HandShakePayload bytes.
     */
    byte[] serialize(HandShakePayload handShakePayload);

    /**
     * Parses bytes to {@link HandShakePayload}.
     * 
     * @param bytes HandShakePayload bytes.
     * @return Parsed HandShakePayload.
     */
    HandShakePayload parse(byte[] bytes);
}
