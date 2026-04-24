package io.github.teilabs.meshnet.core.transport;

import io.github.teilabs.meshnet.core.crypto.Ed25519KeyPair;
import java.util.Arrays;

public final class TransportMessage {
    /**
     * Message used to check connection status between this node and neighbor node.
     */
    public static final byte TYPE_HANDSHAKE = 0;
    /**
     * Message used to transfer frame between this node and neighbor node.
     */
    public static final byte TYPE_FRAME = 1;
    /**
     * Message used to notify all devices (nodes) in connection radius that this
     * node
     * is online.
     */
    public static final byte TYPE_ADVERTISING = 2;

    /** Version of protocol used in this TransportMessage. */
    private final byte version;

    /** Type of TransportMessage. (see TYPE_*) */
    private final byte type;

    /**
     * Routing Id (see {@link Ed25519KeyPair#generateRoutingId}) of device which
     * sent this message.
     */
    private final long senderRoutingId;

    /**
     * Routing Id (see {@link Ed25519KeyPair#generateRoutingId}) of device which
     * should receive this message.
     * <br>
     * 0 - if all devices should receive this message (necessary for advertising).
     */
    private final long targetRoutingId;

    /**
     * Message payload. Different for each type of message.
     */
    private final byte[] payload;

    public TransportMessage(byte version, byte type, long senderRoutingId, long targetRoutingId, byte[] payload) {
        this.version = version;
        this.type = type;
        this.senderRoutingId = senderRoutingId;
        this.targetRoutingId = targetRoutingId;
        this.payload = payload.clone();

        validateFields();
        validateByType();
    }

    /** Validates non type related fields. */
    private void validateFields() {
        if (version < 1) {
            throw new IllegalArgumentException("Version must be higher that 0");
        }
        if (type < 0 || type > 2) {
            throw new IllegalArgumentException("Type must be in range from 0 to 2");
        }
    }

    /** Validates type related fields. */
    private void validateByType() {
        if (type == TYPE_ADVERTISING) {
            if (targetRoutingId != 0) {
                throw new IllegalArgumentException(
                        "Advertising message must target routing id equaled 0.");
            }
        }
    }

    public byte getVersion() {
        return version;
    }

    public byte getType() {
        return type;
    }

    public long getSenderRoutingId() {
        return senderRoutingId;
    }

    public long getTargetRoutingId() {
        return targetRoutingId;
    }

    public byte[] getPayload() {
        return payload.clone();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + version;
        result = prime * result + type;
        result = prime * result + (int) (senderRoutingId ^ (senderRoutingId >>> 32));
        result = prime * result + (int) (targetRoutingId ^ (targetRoutingId >>> 32));
        result = prime * result + Arrays.hashCode(payload);
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
        TransportMessage other = (TransportMessage) obj;
        if (version != other.version)
            return false;
        if (type != other.type)
            return false;
        if (senderRoutingId != other.senderRoutingId)
            return false;
        if (targetRoutingId != other.targetRoutingId)
            return false;
        if (!Arrays.equals(payload, other.payload))
            return false;
        return true;
    }
}
