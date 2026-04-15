package io.github.teilabs.meshnet.core.transport.advertising;

import io.github.teilabs.meshnet.core.frame.FrameConstants;
import java.util.Arrays;

public final class AdvertisingPayload {
    /** Ed25519 public key of source device. */
    private final byte[] srcPubKey;

    /**
     * Ed25519 signature of srcPubKey.
     */
    private final byte[] signature;

    public AdvertisingPayload(byte[] srcPubKey, byte[] signature) {
        this.srcPubKey = srcPubKey.clone();
        this.signature = signature.clone();

        validateFields();
    }

    /** Validates fields values. */
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
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(srcPubKey);
        result = prime * result + Arrays.hashCode(signature);
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
        AdvertisingPayload other = (AdvertisingPayload) obj;
        if (!Arrays.equals(srcPubKey, other.srcPubKey))
            return false;
        if (!Arrays.equals(signature, other.signature))
            return false;
        return true;
    }
}
