package io.github.teilabs.meshnet.core.crypto;

import io.github.teilabs.meshnet.core.frame.FrameConstants;
import java.security.SecureRandom;

/**
 * Class for generating nonce.
 */
public class NonceGenerator {
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private NonceGenerator() {
    }

    /**
     * Generates nonce for {@link io.github.teilabs.meshnet.core.frame.Frame}.
     * 
     * @return generated nonce bytes
     */
    public static byte[] generate() {
        byte[] nonce = new byte[FrameConstants.NONCE_SIZE_v1];
        SECURE_RANDOM.nextBytes(nonce);
        return nonce;
    }
}
