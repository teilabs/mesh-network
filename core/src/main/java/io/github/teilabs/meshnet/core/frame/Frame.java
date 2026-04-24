package io.github.teilabs.meshnet.core.frame;

import io.github.teilabs.meshnet.core.crypto.Ed25519KeyPair;
import java.util.Arrays;

/**
 * Class for all messages sent between nodes that contains data that app sends
 * or tunnel open/close requests (that are in fact initiated by app).
 */
public final class Frame {
    /**
     * Simple Frame that distributes through all neighbors and stores in memory for
     * future distributing.
     */
    public static final byte TYPE_DATA = 0;
    /**
     * Frame without any data. This frame is used to open tunnel (usually
     * shortest ordered way that connects two devices through other nodes without
     * self intersections).
     */
    public static final byte TYPE_OPEN_TUNNEL = 1;
    /**
     * This frame is used to send data using already
     * opened tunnel.
     */
    public static final byte TYPE_DATA_TUNNEL = 2;
    /** Frame without any data. This frame is used to close tunnel. */
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
     * be 0 for frames to core.
     */
    private final short dstAppId;

    /** Ed25519 public key of source device. */
    private final byte[] srcPubKey;
    /**
     * Routing Id (see {@link Ed25519KeyPair#generateRoutingId}) of destination
     * device.
     */
    private final long dstRoutingId;

    /** 12 byte nonce used for encrypting this Frame data. */
    private final byte[] nonce;

    /**
     * Direction of this frame in {@link #TYPE_OPEN_TUNNEL open tunnel request}.
     * Should be false - if this frame goes from tunnel initiator, true - if this
     * frame goes back to tunnel initiator.
     */
    private final boolean direction;

    /**
     * Ed25519 signature of this Frame header (part of frame, see
     * {@link FrameCodec#serializeHeader} implementations).
     */
    private final byte[] signature;

    /** Frame body encrypted with AEAD (use header for aad and nonce for nonce). */
    private final byte[] encryptedData;

    public Frame(byte version, byte type, int timestamp, short srcAppId, short dstAppId, byte[] srcPubKey,
            long dstRoutingId,
            byte[] nonce,
            boolean direction,
            byte[] signature,
            byte[] encryptedData) {
        this.version = version;
        this.type = type;
        this.timestamp = timestamp;
        this.srcAppId = srcAppId;
        this.dstAppId = dstAppId;
        this.srcPubKey = (srcPubKey != null) ? srcPubKey.clone() : new byte[0];
        this.dstRoutingId = dstRoutingId;
        this.nonce = nonce;
        this.direction = direction;
        this.signature = (signature != null) ? signature.clone() : new byte[0];
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
        if (srcPubKey.length != FrameConstants.PUBLIC_KEY_SIZE_v1) {
            throw new IllegalArgumentException(
                    "Src public key must have length of " + FrameConstants.PUBLIC_KEY_SIZE_v1 + " bytes");
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
            if (encryptedData.length == 0) {
                throw new IllegalArgumentException(
                        "Data frame must have encrypted data.");
            }
        } else if (type == TYPE_OPEN_TUNNEL) {
            if (encryptedData.length != 0) {
                throw new IllegalArgumentException(
                        "Open tunnel frame mustn't have encrypted data.");
            }
        } else if (type == TYPE_DATA_TUNNEL) {
            if (encryptedData.length == 0) {
                throw new IllegalArgumentException("Data tunnel frame must have encrypted data");
            }
        } else if (type == TYPE_CLOSE_TUNNEL) {
            if (encryptedData.length != 0) {
                throw new IllegalArgumentException(
                        "Close tunnel frame mustn't have encrypted data.");
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

    public boolean getDirection() {
        return direction;
    }

    public byte[] getSignature() {
        return signature.clone();
    }

    public byte[] getEncryptedData() {
        return encryptedData.clone();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + version;
        result = prime * result + type;
        result = prime * result + timestamp;
        result = prime * result + srcAppId;
        result = prime * result + dstAppId;
        result = prime * result + Arrays.hashCode(srcPubKey);
        result = prime * result + (int) (dstRoutingId ^ (dstRoutingId >>> 32));
        result = prime * result + Arrays.hashCode(nonce);
        result = prime * result + (direction ? 1231 : 1237);
        result = prime * result + Arrays.hashCode(signature);
        result = prime * result + Arrays.hashCode(encryptedData);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Frame other = (Frame) obj;
        if (version != other.version)
            return false;
        if (type != other.type)
            return false;
        if (timestamp != other.timestamp)
            return false;
        if (srcAppId != other.srcAppId)
            return false;
        if (dstAppId != other.dstAppId)
            return false;
        if (!Arrays.equals(srcPubKey, other.srcPubKey))
            return false;
        if (dstRoutingId != other.dstRoutingId)
            return false;
        if (!Arrays.equals(nonce, other.nonce))
            return false;
        if (direction != other.direction)
            return false;
        if (!Arrays.equals(signature, other.signature))
            return false;
        if (!Arrays.equals(encryptedData, other.encryptedData))
            return false;
        return true;
    }
}
