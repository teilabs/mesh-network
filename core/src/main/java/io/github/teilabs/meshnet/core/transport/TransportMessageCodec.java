package io.github.teilabs.meshnet.core.transport;

import io.github.teilabs.meshnet.core.exception.MeshProtocolException;

/**
 * Interface for parsing and serializing {@link TransportMessage} according to
 * its version.
 */
public interface TransportMessageCodec {
    /**
     * Serializes {@link TransportMessage} to bytes.
     * 
     * @param message TransportMessage to serialize.
     * @return Serialized TransportMessage bytes.
     * @throws MeshProtocolException if the message version is unsupported.
     */
    byte[] serialize(TransportMessage message) throws MeshProtocolException;

    /**
     * Parses bytes to {@link TransportMessage}.
     * 
     * @param bytes TransportMessage bytes.
     * @return Parsed TransportMessage.
     * @throws MeshProtocolException if the message version is unsupported.
     */
    TransportMessage parse(byte[] bytes) throws MeshProtocolException;
}
