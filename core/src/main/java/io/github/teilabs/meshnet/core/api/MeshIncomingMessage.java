package io.github.teilabs.meshnet.core.api;

import io.github.teilabs.meshnet.core.frame.FrameConstants;
import java.util.Arrays;

/**
 * Represents incoming {@link Frame} by storing only neccessary fields to give it to app
 * through client library.
 */
public final class MeshIncomingMessage {
    /** Timestamp of Frame creation on source device. */

    private final int timestamp;

    /** Id of application that initialized this Frame sending from source device. */
    private final short srcAppId;
    /** Id of application that will receive this Frame on destination device. */
    private final short dstAppId;

    /** Ed25519 public key of source device. */
    private final byte[] srcPubKey;

    /** Decrypted data from frame. */
    private final byte[] data;

    public MeshIncomingMessage(int timestamp, short srcAppId, short dstAppId, byte[] srcPubKey,
            byte[] data) {

        this.timestamp = timestamp;
        this.srcAppId = srcAppId;
        this.dstAppId = dstAppId;
        this.srcPubKey = (srcPubKey != null) ? srcPubKey.clone() : new byte[0];
        this.data = (data != null) ? data.clone() : new byte[0];

        validateFields();
    }

    /** Validates non type related fields. */
    private void validateFields() {
        if (srcPubKey.length != FrameConstants.PUBLIC_KEY_SIZE_v1) {
            throw new IllegalArgumentException(
                    "Src public key must have length of " + FrameConstants.PUBLIC_KEY_SIZE_v1 + " bytes");
        }
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

    public byte[] getdata() {
        return data.clone();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + timestamp;
        result = prime * result + srcAppId;
        result = prime * result + dstAppId;
        result = prime * result + Arrays.hashCode(srcPubKey);
        result = prime * result + Arrays.hashCode(data);
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
        MeshIncomingMessage other = (MeshIncomingMessage) obj;
        if (timestamp != other.timestamp)
            return false;
        if (srcAppId != other.srcAppId)
            return false;
        if (dstAppId != other.dstAppId)
            return false;
        if (!Arrays.equals(srcPubKey, other.srcPubKey))
            return false;
        if (!Arrays.equals(data, other.data))
            return false;
        return true;
    }
}
