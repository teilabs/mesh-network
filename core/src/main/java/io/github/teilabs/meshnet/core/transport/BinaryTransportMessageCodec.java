package io.github.teilabs.meshnet.core.transport;

import java.nio.ByteBuffer;

import io.github.teilabs.meshnet.core.exception.MeshProtocolException;

public class BinaryTransportMessageCodec implements TransportMessageCodec {
    @Override
    public byte[] serialize(TransportMessage message) throws MeshProtocolException {
        // serializing transport message according to version
        switch (message.getVersion()) {
            case 1: {
                ByteBuffer buffer = ByteBuffer
                        .allocate(TransportMessageConstants.HEADER_SIZE_v1 + message.getPayload().length);

                buffer
                        .put(message.getVersion())
                        .put(message.getType())
                        .putLong(message.getSenderRoutingId())
                        .putLong(message.getTargetRoutingId())
                        .put(message.getPayload());

                return buffer.array();
            }
            default:
                throw new MeshProtocolException("Unsupported transport message version.");
        }
    }

    @Override
    public TransportMessage parse(byte[] bytes) throws MeshProtocolException {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);

        byte version = buffer.get();
        // parsing transport message according to version
        switch (version) {
            case 1: {
                byte type = buffer.get();
                long senderRoutingId = buffer.getLong();
                long targetRoutingId = buffer.getLong();
                byte[] payload = new byte[buffer.remaining()];

                return new TransportMessage(
                        version,
                        type,
                        senderRoutingId,
                        targetRoutingId,
                        payload);
            }
            default:
                throw new MeshProtocolException("Unsupported transport message version.");
        }
    }

}
