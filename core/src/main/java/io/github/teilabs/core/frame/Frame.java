package io.github.teilabs.core.frame;

import java.util.Arrays;

public final class Frame {
    public static final byte TYPE_DATA = 0;
    public static final byte TYPE_OPEN_TUNNEL = 1;
    public static final byte TYPE_DATA_TUNNEL = 2;
    public static final byte TYPE_CLOSE_TUNNEL = 3;

    private final byte version;

    private final byte type;

    private final int timestamp;

    private final short appId;

    private final byte[] senderPubKey;
    private final long dstRoutingId;

    private final byte[] signature;

    private final long[] path;

    private final byte[] encryptedData;

    public Frame(byte version, byte type, int timestamp, short appId, byte[] senderPubKey, long dstRoutingId,
            byte[] signature,
            long[] path, byte[] encryptedData) {
        this.version = version;
        this.type = type;
        this.timestamp = timestamp;
        this.appId = appId;
        this.senderPubKey = senderPubKey.clone();
        this.dstRoutingId = dstRoutingId;
        this.signature = signature.clone();
        this.path = (path != null) ? path.clone() : new long[0];
        this.encryptedData = (encryptedData != null) ? encryptedData.clone() : new byte[0];
        
        // TODO: validate fields

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

    public short getAppId() {
        return appId;
    }

    public byte[] getSenderPubKey() {
        return senderPubKey.clone();
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
        if (this == o) return true;
        if (!(o instanceof Frame frame)) return false;
        return version == frame.version && type == frame.type && appId == frame.appId
                && timestamp == frame.timestamp && dstRoutingId == frame.dstRoutingId
                && Arrays.equals(senderPubKey, frame.senderPubKey)
                && Arrays.equals(signature, frame.signature)
                && Arrays.equals(path, frame.path)
                && Arrays.equals(encryptedData, frame.encryptedData);
    }

    @Override
    public int hashCode() {
        int result = version;
        result = 31 * result + type;
        result = 31 * result + appId;
        result = 31 * result + timestamp;
        result = 31 * result + Long.hashCode(dstRoutingId);
        result = 31 * result + Arrays.hashCode(senderPubKey);
        result = 31 * result + Arrays.hashCode(signature);
        result = 31 * result + Arrays.hashCode(path);
        result = 31 * result + Arrays.hashCode(encryptedData);
        return result;
    }
}
