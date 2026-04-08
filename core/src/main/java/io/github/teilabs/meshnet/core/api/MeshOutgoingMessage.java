package io.github.teilabs.meshnet.core.api;

import io.github.teilabs.meshnet.core.frame.FrameConstants;
import java.util.Arrays;

/**
 * Repsresents message that will be converted to {@link Frame} and sended to
 * destination device.
 * <br>
 * Client library gives this class to {@link MeshCore} when app initialize message sending.
 */
public final class MeshOutgoingMessage {
    /**
     * Simple Message that distributes through all neighbours and stores in memory
     * for future distributing.
     */
    public static final byte TYPE_DATA = 0;
    /**
     * Message witout any data. This message is used to open tunnel (usually
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
     * be 0 for system frames like open and close tunnel.
     */
    private final short dstAppId;

    /** Ed25519 public key of destination device. */
    private final byte[] dstPubKey;

    /** Data that will be sent to destination device. */
    private final byte[] data;

    public MeshOutgoingMessage(byte type, short srcAppId, short dstAppId, byte[] dstPubKey,
            byte[] data) {

        this.type = type;
        this.srcAppId = srcAppId;
        this.dstAppId = dstAppId;
        this.dstPubKey = dstPubKey.clone();
        this.data = (data != null) ? data.clone() : new byte[0];

        validateFields();
        validateByType();
    }

    /** Validates non type related fields. */
    private void validateFields() {
        if (type < 0 || type > 3) {
            throw new IllegalArgumentException("Type must be in range from 0 to 3");
        }
        if (dstPubKey.length != FrameConstants.PUBLICK_KEY_SIZE_v1) {
            throw new IllegalArgumentException(
                    "Dst public key must have length of " + FrameConstants.PUBLICK_KEY_SIZE_v1 + " bytes");
        }
    }

    /** Validates type related fields. */
    private void validateByType() {
        if (type == TYPE_DATA) {
            if (data.length == 0) {
                throw new IllegalArgumentException("Data frame must have data");
            }
        } else if (type == TYPE_OPEN_TUNNEL) {
            if (data.length != 0 || dstAppId != 0) {
                throw new IllegalArgumentException(
                        "Open tunnel frame mustn't have encrypted data. dstAppId must equal 0");
            }
        } else if (type == TYPE_DATA_TUNNEL) {
            if (data.length == 0) {
                throw new IllegalArgumentException("Data tunnel frame must have data");
            }
        } else if (type == TYPE_CLOSE_TUNNEL) {
            if (data.length != 0 || dstAppId != 0) {
                throw new IllegalArgumentException("Close tunnel frame mustn't have data. dstAppId must equal 0");
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

    public byte[] getdata() {
        return data.clone();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof MeshOutgoingMessage meshOutgoingMessage))
            return false;
        return srcAppId == meshOutgoingMessage.srcAppId && dstAppId == meshOutgoingMessage.dstAppId
                && type == meshOutgoingMessage.type
                && Arrays.equals(dstPubKey, meshOutgoingMessage.dstPubKey)
                && Arrays.equals(data, meshOutgoingMessage.data);
    }

    @Override
    public int hashCode() {
        int result = srcAppId;
        result = 31 * result + dstAppId;
        result = 31 * result + type;
        result = 31 * result + Arrays.hashCode(dstPubKey);
        result = 31 * result + Arrays.hashCode(data);
        return result;
    }
}
