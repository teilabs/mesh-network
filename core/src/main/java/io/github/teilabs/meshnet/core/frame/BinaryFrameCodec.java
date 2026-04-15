package io.github.teilabs.meshnet.core.frame;

import java.nio.ByteBuffer;

public class BinaryFrameCodec implements FrameCodec {
    @Override
    public Frame parse(byte[] rawFrame) {
        ByteBuffer buffer = ByteBuffer.wrap(rawFrame);

        byte version = buffer.get();
        // parsing frame according to version
        switch (version) {
            case 1: {
                byte type = buffer.get();
                int timestamp = buffer.getInt();
                short srcAppId = buffer.getShort();
                short dstAppId = buffer.getShort();
                byte[] srcPubKey = new byte[FrameConstants.PUBLICK_KEY_SIZE_v1];
                buffer.get(srcPubKey);
                long dstRoutingId = buffer.getLong();
                byte[] nonce = new byte[FrameConstants.NONCE_SIZE_v1];
                buffer.get(nonce);
                long tunnelId = buffer.getLong();
                byte[] signature = new byte[FrameConstants.SIGNATURE_SIZE_v1];
                buffer.get(signature);
                byte[] encryptedData = new byte[buffer.remaining()];

                return new Frame(
                        version,
                        type,
                        timestamp,
                        srcAppId,
                        dstAppId,
                        srcPubKey,
                        dstRoutingId,
                        nonce,
                        tunnelId,
                        signature,
                        encryptedData);
            }
            default:
                throw new IllegalArgumentException("Unsopported frame version.");
        }
    }

    @Override
    public byte[] serialize(Frame frame) {
        // serializing frame according to version
        switch (frame.getVersion()) {
            case 1: {
                ByteBuffer buffer = ByteBuffer
                        .allocate(FrameConstants.HEADER_SIZE_v1 + FrameConstants.SIGNATURE_SIZE_v1
                                + frame.getEncryptedData().length);

                buffer
                        .put(frame.getVersion())
                        .put(frame.getType())
                        .putInt(frame.getTimestamp())
                        .putShort(frame.getSrcAppId())
                        .putShort(frame.getDstAppId())
                        .put(frame.getSrcPubKey())
                        .putLong(frame.getDstRoutingId())
                        .put(frame.getNonce())
                        .putLong(frame.getTunnelId())
                        .put(frame.getSignature())
                        .put(frame.getEncryptedData());

                return buffer.array();
            }
            default:
                throw new IllegalArgumentException("Unsopported frame version.");
        }
    }

    @Override
    public byte[] serializeHeader(Frame frame) {
        // serializing frame header according to version
        switch (frame.getVersion()) {
            case 1: {
                ByteBuffer buffer = ByteBuffer
                        .allocate(FrameConstants.HEADER_SIZE_v1);

                buffer
                        .put(frame.getVersion())
                        .put(frame.getType())
                        .putInt(frame.getTimestamp())
                        .putShort(frame.getSrcAppId())
                        .putShort(frame.getDstAppId())
                        .put(frame.getSrcPubKey())
                        .putLong(frame.getDstRoutingId())
                        .put(frame.getNonce())
                        .putLong(frame.getTunnelId());

                return buffer.array();
            }
            default:
                throw new IllegalArgumentException("Unsopported frame version.");
        }
    }
}
