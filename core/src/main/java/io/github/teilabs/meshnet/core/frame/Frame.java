package io.github.teilabs.meshnet.core.frame;

import java.util.Arrays;

/**
 * Class for incoming and outgoing messages.
 */
public final class Frame {
    /**
     * Simple Frame that distributes through all neighbours and stores in memory for
     * future distributing.
     */
    public static final byte TYPE_DATA = 0;
    /**
     * Frame witout any data except path. This frame is used to open tunnel (usually
     * shortest ordered way that connects two devices through other nodes without
     * self intersections).
     */
    public static final byte TYPE_OPEN_TUNNEL = 1;
    /**
     * Frame with data and path. This frame is used to send data using already
     * opened tunnel.
     */
    public static final byte TYPE_DATA_TUNNEL = 2;
    /** Frame without any data except path. This frame is used to close tunnel. */
    public static final byte TYPE_CLOSE_TUNNEL = 3;

    /** Version of protocol used in this Frame. */
    private final byte version;

    /** Type of Frame. (see TYPE_*) */
    private final byte type;

    /** Timestamp of Frame creation on source device. */
    private final int timestamp;

    /** Id of application that initialized this Frame sending from source device. */
    private final short srcAppId;
    /**
     * Id of application that will receive this Frame on destination device. Should
     * be 0 for system frames like open and close tunnel.
     */
    private final short dstAppId;

    /** Ed25519 public key of source device. */
    private final byte[] srcPubKey;
    /**
     * Routing Id (just first 8 bytes of Ed25519 publick key) of destination device.
     */
    private final long dstRoutingId;

    /** 12 byte nonce used for encrypting this Frame data. */
    private final byte[] nonce;

    /**
     * Ed25519 signature of this Frame header (part of frame, see
     * {@link FrameCodec.serializeHeader} implementations).
     */
    private final byte[] signature;

    /**
     * Ordered array of routing Ids (just first 8 bytes of Ed25519 publick key) of
     * devices that passed this Frame. Is not empty only if this Frame has tunnel
     * related type.
     */
    private final long[] path;

    /** Frame body encrypted with AEAD (use header for aad and nonce for nonce). */
    private final byte[] encryptedData;

    public Frame(byte version, byte type, int timestamp, short srcAppId, short dstAppId, byte[] srcPubKey,
            long dstRoutingId,
            byte[] nonce,
            byte[] signature,
            long[] path, byte[] encryptedData) {
        this.version = version;
        this.type = type;
        this.timestamp = timestamp;
        this.srcAppId = srcAppId;
        this.dstAppId = dstAppId;
        this.srcPubKey = (srcPubKey != null) ? srcPubKey.clone() : new byte[0];
        this.dstRoutingId = dstRoutingId;
        this.nonce = nonce;
        this.signature = (signature != null) ? signature.clone() : new byte[0];
        this.path = (path != null) ? path.clone() : new long[0];
        this.encryptedData = (encryptedData != null) ? encryptedData.clone() : new byte[0];

        validateFields();
        validateByType();
    }

    /** Validates non type related fields. */
    private void validateFields() {
        if (version < 1) {
            throw new IllegalArgumentException("Version must be higher that 0");
        }
        if (type < 0 || type > 3) {
            throw new IllegalArgumentException("Type must be in range from 0 to 3");
        }
        if (srcPubKey.length != FrameConstants.PUBLICK_KEY_SIZE_v1) {
            throw new IllegalArgumentException(
                    "Src public key must have length of " + FrameConstants.PUBLICK_KEY_SIZE_v1 + " bytes");
        }
        if (nonce.length != FrameConstants.NONCE_SIZE_v1) {
            throw new IllegalArgumentException(
                    "Nonce must have length of " + FrameConstants.NONCE_SIZE_v1 + " bytes");
        }
        if (signature.length != FrameConstants.SIGNATURE_SIZE_v1) {
            throw new IllegalArgumentException(
                    "Signature must have length of " + FrameConstants.SIGNATURE_SIZE_v1 + " bytes");
        }
    }

    /** Validates type related fields. */
    private void validateByType() {
        if (type == TYPE_DATA) {
            if (path.length != 0 || encryptedData.length == 0) {
                throw new IllegalArgumentException("Data frame mustn't have path and must have encrypted data");
            }
        } else if (type == TYPE_OPEN_TUNNEL) {
            if (path.length == 0 || encryptedData.length != 0 || dstAppId != 0) {
                throw new IllegalArgumentException(
                        "Open tunnel frame must have path and mustn't have encrypted data. dstAppId must equal 0");
            }
        } else if (type == TYPE_DATA_TUNNEL) {
            if (path.length == 0 || encryptedData.length == 0) {
                throw new IllegalArgumentException("Data tunnel frame must have path and must have encrypted data");
            }
        } else if (type == TYPE_CLOSE_TUNNEL) {
            if (path.length == 0 || encryptedData.length != 0 || dstAppId != 0) {
                throw new IllegalArgumentException(
                        "Close tunnel frame must have path and mustn't have encrypted data. dstAppId must equal 0");
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

    public byte[] getNonce() {
        return nonce.clone();
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
