package io.github.teilabs.meshnet.core.api;

import io.github.teilabs.meshnet.core.MeshCore;
import io.github.teilabs.meshnet.core.frame.Frame;
import io.github.teilabs.meshnet.core.frame.FrameConstants;
import java.util.Arrays;

/**
 * Represents message that will be converted to {@link Frame} and sent to
 * destination device.
 * <br>
 * Client library gives this class to {@link MeshCore} when app initialize
 * message sending.
 */
public final class MeshOutgoingMessage {
    /**
     * Simple Message that distributes through all neighbours and stores in memory
     * for future distribution.
     */
    public static final byte TYPE_DATA = 0;
    /**
     * Message without any data. This message is used to open tunnel (usually
     * shortest ordered way that connects two devices through other nodes without
     * self intersections).
     */
    public static final byte TYPE_OPEN_TUNNEL = 1;
    /** Message that is used to send data using already opened tunnel. */
    public static final byte TYPE_DATA_TUNNEL = 2;
    /** Message without any data path. This message is used to close tunnel. */
    public static final byte TYPE_CLOSE_TUNNEL = 3;

    /** Type of MeshOutgoingMessage. (see TYPE_*) */
    private final byte type;

    /** Id of application that initialized this Frame sending from source device. */
    private final short srcAppId;
    /**
     * Id of application that will receive this Frame on destination device. Should
     * be 0 for frames to core.
     */
    private final short dstAppId;

    /** Ed25519 public key of destination device. */
    private final byte[] dstPubKey;

    /** Data that will be sent to destination device. */
    private final byte[] data;

    /**
     * Direction of this message in {@link #TYPE_OPEN_TUNNEL open tunnel request}.
     * Should be false - if this message goes from tunnel initiator, true - if this
     * message goes back to tunnel initiator.
     */
    private final boolean direction;

    public MeshOutgoingMessage(byte type, short srcAppId, short dstAppId, byte[] dstPubKey,
            byte[] data, boolean direction) {
        this.type = type;
        this.srcAppId = srcAppId;
        this.dstAppId = dstAppId;
        this.dstPubKey = dstPubKey.clone();
        this.data = (data != null) ? data.clone() : new byte[0];
        this.direction = direction;

        validateFields();
        validateByType();
    }

    /** Validates non type related fields. */
    private void validateFields() {
        if (type < 0 || type > 3) {
            throw new IllegalArgumentException("Type must be in range from 0 to 3");
        }
        if (dstPubKey.length != FrameConstants.PUBLIC_KEY_SIZE_v1) {
            throw new IllegalArgumentException(
                    "Dst public key must have length of " + FrameConstants.PUBLIC_KEY_SIZE_v1 + " bytes");
        }
    }

    /** Validates type related fields. */
    private void validateByType() {
        if (type == TYPE_DATA) {
            if (data.length == 0) {
                throw new IllegalArgumentException(
                        "Data frame must have data.");
            }
        } else if (type == TYPE_OPEN_TUNNEL) {
            if (data.length != 0) {
                throw new IllegalArgumentException(
                        "Open tunnel frame mustn't have data.");
            }
        } else if (type == TYPE_DATA_TUNNEL) {
            if (data.length == 0) {
                throw new IllegalArgumentException("Data tunnel frame must have data");
            }
        } else if (type == TYPE_CLOSE_TUNNEL) {
            if (data.length != 0) {
                throw new IllegalArgumentException(
                        "Close tunnel frame mustn't have data.");
            }
        }
    }

    public byte getType() {
        return type;
    }

    public short getSrcAppId() {
        return srcAppId;
    }

    public short getDstAppId() {
        return dstAppId;
    }

    public byte[] getDstPubKey() {
        return dstPubKey;
    }

    public byte[] getData() {
        return data.clone();
    }

    public boolean getDirection() {
        return direction;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + type;
        result = prime * result + srcAppId;
        result = prime * result + dstAppId;
        result = prime * result + Arrays.hashCode(dstPubKey);
        result = prime * result + Arrays.hashCode(data);
        result = prime * result + (direction ? 1231 : 1237);
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
        MeshOutgoingMessage other = (MeshOutgoingMessage) obj;
        if (type != other.type)
            return false;
        if (srcAppId != other.srcAppId)
            return false;
        if (dstAppId != other.dstAppId)
            return false;
        if (!Arrays.equals(dstPubKey, other.dstPubKey))
            return false;
        if (!Arrays.equals(data, other.data))
            return false;
        if (direction != other.direction)
            return false;
        return true;
    }
}
