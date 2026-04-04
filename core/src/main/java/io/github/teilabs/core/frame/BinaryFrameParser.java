package io.github.teilabs.core.frame;

import java.nio.ByteBuffer;

public class BinaryFrameParser implements FrameParser<byte[]> {

    @Override
    public Frame parse(byte[] rawFrame) {
        ByteBuffer buffer = ByteBuffer.wrap(rawFrame);

        byte version = buffer.get();
        byte type = buffer.get();
        int timestamp = buffer.getInt();
        short appId = buffer.getShort();
        byte[] senderPubKey = new byte[FrameConstants.PUBLICK_KEY_SIZE];
        buffer.get(senderPubKey);
        long dstRoutingId = buffer.getLong();
        byte[] signature = new byte[FrameConstants.SIGNATURE_SIZE];
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
                appId,
                senderPubKey,
                dstRoutingId,
                signature,
                path,
                encryptedData);
    }

    @Override
    public byte[] serialize(Frame frame) {
        ByteBuffer buffer = ByteBuffer
                .allocate(FrameConstants.HEADER_SIZE + 8 * frame.getPath().length + frame.getEncryptedData().length);

        buffer
                .put(frame.getVersion())
                .put(frame.getType())
                .putInt(frame.getTimestamp())
                .putShort(frame.getAppId())
                .put(frame.getSenderPubKey())
                .putLong(frame.getDstRoutingId())
                .put(frame.getSignature())
                .putShort((short) frame.getPath().length);
        for (long l : frame.getPath()) {
            buffer.putLong(l);
        }
        buffer.put(frame.getEncryptedData());

        return buffer.array();
    }

}
