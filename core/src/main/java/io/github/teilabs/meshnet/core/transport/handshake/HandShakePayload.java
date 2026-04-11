package io.github.teilabs.meshnet.core.transport.handshake;

import io.github.teilabs.meshnet.core.frame.FrameConstants;
import java.util.Arrays;

public final class HandShakePayload {
    /** Ed25519 public key of source device. */
    private final byte[] srcPubKey;

    /**
     * Ed25519 signature of srcPubKey.
     */
    private final byte[] signature;

    public HandShakePayload(byte[] srcPubKey, byte[] signature) {
        this.srcPubKey = srcPubKey.clone();
        this.signature = signature.clone();

        validateFields();
    }

    private void validateFields() {
        if (srcPubKey.length != FrameConstants.PUBLICK_KEY_SIZE_v1) {
            throw new IllegalArgumentException(
                    "Src public key must have length of " + FrameConstants.PUBLICK_KEY_SIZE_v1 + " bytes");
        }
        if (signature.length != FrameConstants.SIGNATURE_SIZE_v1) {
            throw new IllegalArgumentException(
                    "Signature must have length of " + FrameConstants.SIGNATURE_SIZE_v1 + " bytes");
        }
    }

    public byte[] getSrcPubKey() {
        return srcPubKey;
    }

    public byte[] getSignature() {
        return signature;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof HandShakePayload that))
            return false;
        return Arrays.equals(srcPubKey, that.srcPubKey) && Arrays.equals(signature, that.signature);
    }

    @Override
    public int hashCode() {
        int r = Arrays.hashCode(srcPubKey);
        r = 31 * r + Arrays.hashCode(signature);
        return r;
    }
}
