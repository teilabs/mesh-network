package io.github.teilabs.meshnet.core.transport.advertising;

import io.github.teilabs.meshnet.core.frame.FrameConstants;
import java.nio.ByteBuffer;

public class BinaryAdvertisingPayloadCodec implements AdvertisingPayloadCodec {

    @Override
    public byte[] serialize(AdvertisingPayload advertisingPayload) {
        ByteBuffer buffer = ByteBuffer.allocate(FrameConstants.PUBLICK_KEY_SIZE_v1 + FrameConstants.SIGNATURE_SIZE_v1);

        buffer.put(advertisingPayload.getSrcPubKey());
        buffer.put(advertisingPayload.getSignature());

        return buffer.array();
    }

    @Override
    public AdvertisingPayload parse(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);

        byte[] srcPubKey = new byte[FrameConstants.PUBLICK_KEY_SIZE_v1];
        buffer.get(srcPubKey);
        byte[] signature = new byte[FrameConstants.SIGNATURE_SIZE_v1];
        buffer.get(signature);

        return new AdvertisingPayload(srcPubKey, signature);
    }

}
