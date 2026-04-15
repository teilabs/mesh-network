package io.github.teilabs.meshnet.core.transport;

/**
 * Interface for parsing and serializing {@link TransportMessage} according to its version.
 */
public interface TransportMessageCodec {
    /**
     * Serializes {@link TransportMessage} to bytes.
     * 
     * @param message TransportMessage to serialize.
     * @return Serialized TransportMessage bytes.
     */
    byte[] serialize(TransportMessage message);

    /**
     * Parses bytes to {@link TransportMessage}.
     * 
     * @param bytes TransportMessage bytes.
     * @return Parsed TransportMessage.
     */
    TransportMessage parse(byte[] bytes);
}
