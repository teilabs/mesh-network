package io.github.teilabs.meshnet.core.crypto;

import org.bouncycastle.crypto.modes.ChaCha20Poly1305;
import org.bouncycastle.crypto.params.AEADParameters;
import org.bouncycastle.crypto.params.KeyParameter;

public final class AeadCipher {
    public static byte[] encrypt(byte[] key, byte[] nonce, byte[] aad, byte[] plainData) {
        try {
            ChaCha20Poly1305 cipher = new ChaCha20Poly1305();
            cipher.init(true, new AEADParameters(new KeyParameter(key), 128, nonce, aad));

            byte[] out = new byte[cipher.getOutputSize(plainData.length)];
            int len = cipher.processBytes(plainData, 0, plainData.length, out, 0);
            len += cipher.doFinal(out, len);
            return java.util.Arrays.copyOf(out, len);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] decrypt(byte[] key, byte[] nonce, byte[] aad, byte[] cipherData) {
        try {
            ChaCha20Poly1305 cipher = new ChaCha20Poly1305();
            cipher.init(false, new AEADParameters(new KeyParameter(key), 128, nonce, aad));

            byte[] out = new byte[cipher.getOutputSize(cipherData.length)];
            int len = cipher.processBytes(cipherData, 0, cipherData.length, out, 0);
            len += cipher.doFinal(out, len);
            return java.util.Arrays.copyOf(out, len);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
