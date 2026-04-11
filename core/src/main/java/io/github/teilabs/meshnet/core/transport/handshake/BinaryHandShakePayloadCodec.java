package io.github.teilabs.meshnet.core.transport.handshake;

import io.github.teilabs.meshnet.core.frame.FrameConstants;
import java.nio.ByteBuffer;

public class BinaryHandShakePayloadCodec implements HandShakePayloadCodec {
    @Override
    public HandShakePayload parse(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);

        byte[] srcPubKey = new byte[FrameConstants.PUBLICK_KEY_SIZE_v1];
        buffer.get(srcPubKey);
        byte[] signature = new byte[FrameConstants.SIGNATURE_SIZE_v1];
        buffer.get(signature);

        return new HandShakePayload(srcPubKey, signature);
    }

    @Override
    public byte[] serialize(HandShakePayload handShakePayload) {
        ByteBuffer buffer = ByteBuffer.allocate(FrameConstants.PUBLICK_KEY_SIZE_v1 + FrameConstants.SIGNATURE_SIZE_v1);

        buffer.put(handShakePayload.getSrcPubKey());
        buffer.put(handShakePayload.getSignature());

        return buffer.array();
    }
}
