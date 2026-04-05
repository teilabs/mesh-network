package io.github.teilabs.meshnet.core.frame;

import java.util.Arrays;

public final class Frame {
    public static final byte TYPE_DATA = 0;
    public static final byte TYPE_OPEN_TUNNEL = 1;
    public static final byte TYPE_DATA_TUNNEL = 2;
    public static final byte TYPE_CLOSE_TUNNEL = 3;

    private final byte version;

    private final byte type;

    private final int timestamp;

    private final short srcAppId;
    private final short dstAppId;

    private final byte[] srcPubKey;
    private final long dstRoutingId;

    private final byte[] signature;

    private final long[] path;

    private final byte[] encryptedData;

    public Frame(byte version, byte type, int timestamp, short srcAppId, short dstAppId, byte[] srcPubKey,
            long dstRoutingId,
            byte[] signature,
            long[] path, byte[] encryptedData) {
        this.version = version;
        this.type = type;
        this.timestamp = timestamp;
        this.srcAppId = srcAppId;
        this.dstAppId = dstAppId;
        this.srcPubKey = (srcPubKey != null) ? srcPubKey.clone() : new byte[0];
        this.dstRoutingId = dstRoutingId;
        this.signature = (signature != null) ? signature.clone() : new byte[0];
        this.path = (path != null) ? path.clone() : new long[0];
        this.encryptedData = (encryptedData != null) ? encryptedData.clone() : new byte[0];

        validateFields();
        validateByType();
        // TODO: maybe validate signature
    }

    private void validateFields() {
        if (version < 1) {
            throw new IllegalArgumentException("Version must be higher that 0");
        }
        if (type < 0 || type > 3) {
            throw new IllegalArgumentException("Type must be in range from 0 to 3");
        }
        if (srcPubKey.length != FrameConstants.PUBLICK_KEY_SIZE) {
            throw new IllegalArgumentException(
                    "Src public key must have length of " + FrameConstants.PUBLICK_KEY_SIZE + " bytes");
        }
        if (signature.length != FrameConstants.SIGNATURE_SIZE) {
            throw new IllegalArgumentException(
                    "Signature must have length of " + FrameConstants.SIGNATURE_SIZE + " bytes");
        }
    }

    private void validateByType() {
        if (type == TYPE_DATA) {
            if (path.length != 0 || encryptedData.length == 0) {
                throw new IllegalArgumentException("Data frame mustn't have path and must have encrypted data");
            }
        } else if (type == TYPE_OPEN_TUNNEL) {
            if (path.length == 0 || encryptedData.length != 0) {
                throw new IllegalArgumentException("Open tunnel frame must have path and mustn't have encrypted data");
            }
        } else if (type == TYPE_DATA_TUNNEL) {
            if (path.length == 0 || encryptedData.length == 0) {
                throw new IllegalArgumentException("Data tunnel frame must have path and must have encrypted data");
            }
        } else if (type == TYPE_CLOSE_TUNNEL) {
            if (path.length == 0 || encryptedData.length != 0) {
                throw new IllegalArgumentException("Close tunnel frame must have path and mustn't have encrypted data");
            }
        }
    }

    public byte getVersion() {
        return version;
    }

    public byte getType() {
        return type;
    }

    public int getTimestamp() {
        return timestamp;
    }

    public short getSrcAppId() {
        return srcAppId;
    }

    public short getDstAppId() {
        return dstAppId;
    }

    public byte[] getSrcPubKey() {
        return srcPubKey.clone();
    }

    public long getDstRoutingId() {
        return dstRoutingId;
    }

    public byte[] getSignature() {
        return signature.clone();
    }

    public long[] getPath() {
        return path.clone();
    }

    public byte[] getEncryptedData() {
        return encryptedData.clone();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Frame frame))
            return false;
        return version == frame.version && type == frame.type && srcAppId == frame.srcAppId
                && dstAppId == frame.dstAppId
                && timestamp == frame.timestamp && dstRoutingId == frame.dstRoutingId
                && Arrays.equals(srcPubKey, frame.srcPubKey)
                && Arrays.equals(signature, frame.signature)
                && Arrays.equals(path, frame.path)
                && Arrays.equals(encryptedData, frame.encryptedData);
    }

    @Override
    public int hashCode() {
        int result = version;
        result = 31 * result + type;
        result = 31 * result + srcAppId;
        result = 31 * result + dstAppId;
        result = 31 * result + timestamp;
        result = 31 * result + Long.hashCode(dstRoutingId);
        result = 31 * result + Arrays.hashCode(srcPubKey);
        result = 31 * result + Arrays.hashCode(signature);
        result = 31 * result + Arrays.hashCode(path);
        result = 31 * result + Arrays.hashCode(encryptedData);
        return result;
    }
}
