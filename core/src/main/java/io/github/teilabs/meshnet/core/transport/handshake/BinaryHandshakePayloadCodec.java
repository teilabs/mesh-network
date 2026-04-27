package io.github.teilabs.meshnet.core.transport.handshake;

import io.github.teilabs.meshnet.core.frame.FrameConstants;
import java.nio.ByteBuffer;

public class BinaryHandshakePayloadCodec implements HandshakePayloadCodec {
    @Override
    public HandshakePayload parse(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);

        byte[] srcPubKey = new byte[FrameConstants.PUBLIC_KEY_SIZE_v1];
        buffer.get(srcPubKey);
        byte[] signature = new byte[FrameConstants.SIGNATURE_SIZE_v1];
        buffer.get(signature);

        return new HandshakePayload(srcPubKey, signature);
    }

    @Override
    public byte[] serialize(HandshakePayload handshakePayload) {
        ByteBuffer buffer = ByteBuffer.allocate(FrameConstants.PUBLIC_KEY_SIZE_v1 + FrameConstants.SIGNATURE_SIZE_v1);

        buffer.put(handshakePayload.getSrcPubKey());
        buffer.put(handshakePayload.getSignature());

        return buffer.array();
    }
}
