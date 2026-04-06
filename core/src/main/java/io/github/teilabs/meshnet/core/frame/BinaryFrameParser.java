package io.github.teilabs.meshnet.core.frame;

import java.nio.ByteBuffer;

public class BinaryFrameParser implements FrameParser {
    @Override
    public Frame parse(byte[] rawFrame) {
        ByteBuffer buffer = ByteBuffer.wrap(rawFrame);

        byte version = buffer.get();
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
                byte[] signature = new byte[FrameConstants.SIGNATURE_SIZE_v1];
                buffer.get(signature);
                short pathLenght = buffer.getShort();
                long[] path = new long[pathLenght];
                for (int i = 0; i < pathLenght; i++) {
                    path[i] = buffer.getLong();
                }
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
                        signature,
                        path,
                        encryptedData);
            }
            default:
                throw new IllegalArgumentException("Unsopported frame version.");
        }
    }

    @Override
    public byte[] serialize(Frame frame) {
        switch (frame.getVersion()) {
            case 1: {
                ByteBuffer buffer = ByteBuffer
                        .allocate(FrameConstants.HEADER_SIZE_v1 + 8 * frame.getPath().length
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
                        .put(frame.getSignature())
                        .putShort((short) frame.getPath().length);
                for (long l : frame.getPath()) {
                    buffer.putLong(l);
                }
                buffer.put(frame.getEncryptedData());

                return buffer.array();
            }
            default:
                throw new IllegalArgumentException("Unsopported frame version.");
        }
    }

    @Override
    public byte[] serializeHeader(Frame frame) {
        switch (frame.getVersion()) {
            case 1: {
                ByteBuffer buffer = ByteBuffer
                .allocate(FrameConstants.HEADER_SIZE_v1 + 8 * frame.getPath().length + frame.getEncryptedData().length);

        buffer
                .put(frame.getVersion())
                .put(frame.getType())
                .putInt(frame.getTimestamp())
                .putShort(frame.getSrcAppId())
                .putShort(frame.getDstAppId())
                .put(frame.getSrcPubKey())
                .putLong(frame.getDstRoutingId())
                .put(frame.getNonce());

        return buffer.array();
            }
            default:
                throw new IllegalArgumentException("Unsopported frame version.");
        }
    }
}
