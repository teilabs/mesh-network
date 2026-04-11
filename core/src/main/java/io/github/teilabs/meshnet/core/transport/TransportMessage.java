package io.github.teilabs.meshnet.core.transport;

import java.util.Arrays;

public final class TransportMessage {
    public static final byte TYPE_HANDSHAKE = 0;
    public static final byte TYPE_FRAME = 1;
    public static final byte TYPE_ADVERTISING = 2;

    private final byte version;

    private final byte type;

    private final long targetRoutingId;

    private final byte[] payload;

    public TransportMessage(byte version, byte type, long targetRoutingId, byte[] payload) {
        this.version = version;
        this.type = type;
        this.targetRoutingId = targetRoutingId;
        this.payload = payload.clone();

        validateFields();
    }

    /** Validates non type related fields. */
    private void validateFields() {
        if (version < 1) {
            throw new IllegalArgumentException("Version must be higher that 0");
        }
        if (type < 0 || type > 3 / 2) {
            throw new IllegalArgumentException("Type must be in range from 0 to 2");
        }
    }

    public byte getVersion() {
        return version;
    }

    public byte getType() {
        return type;
    }

    public long getTargetRoutingId() {
        return targetRoutingId;
    }

    public byte[] getPayload() {
        return payload.clone();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof TransportMessage that))
            return false;
        return version == that.version && type == that.type && targetRoutingId == that.targetRoutingId
                && Arrays.equals(payload, that.payload);
    }

    @Override
    public int hashCode() {
        int r = Byte.hashCode(version);
        r = 31 * r + Byte.hashCode(type);
        r = 31 * r + Long.hashCode(targetRoutingId);
        r = 31 * r + Arrays.hashCode(payload);
        return r;
    }
}
