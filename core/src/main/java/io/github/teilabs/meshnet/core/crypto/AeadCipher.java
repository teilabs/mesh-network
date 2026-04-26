package io.github.teilabs.meshnet.core.crypto;

import org.bouncycastle.crypto.modes.ChaCha20Poly1305;
import org.bouncycastle.crypto.params.AEADParameters;
import org.bouncycastle.crypto.params.KeyParameter;

import io.github.teilabs.meshnet.core.exception.MeshSecurityException;

/**
 * Class for encrypting and decrypting bytes with ChaCha20-Poly1305.
 */
public final class AeadCipher {
    private AeadCipher() {
    }

    /**
     * Encrypts bytes.
     * 
     * @param key       key to encrypt with
     * @param nonce     nonce to encrypt with
     * @param aad       additional authenticated data
     * @param plainData plain bytes to encrypt
     * @return encrypted bytes
     * @throws MeshSecurityException if encryption fails.
     */
    public static byte[] encrypt(byte[] key, byte[] nonce, byte[] aad, byte[] plainData) throws MeshSecurityException {
        try {
            ChaCha20Poly1305 cipher = new ChaCha20Poly1305();
            cipher.init(true, new AEADParameters(new KeyParameter(key), 128, nonce, aad));

            byte[] out = new byte[cipher.getOutputSize(plainData.length)];
            int len = cipher.processBytes(plainData, 0, plainData.length, out, 0);
            len += cipher.doFinal(out, len);
            return java.util.Arrays.copyOf(out, len);
        } catch (Exception e) {
            throw new MeshSecurityException("Encryption failed", e);
        }
    }

    /**
     * Decrypts bytes.
     * 
     * @param key        key to decrypt with
     * @param nonce      nonce used for encryption
     * @param aad        additional authenticated data
     * @param cipherData encrypted bytes
     * @return decrypted bytes
     * @throws MeshSecurityException if decryption fails.
     */
    public static byte[] decrypt(byte[] key, byte[] nonce, byte[] aad, byte[] cipherData) throws MeshSecurityException {
        try {
            ChaCha20Poly1305 cipher = new ChaCha20Poly1305();
            cipher.init(false, new AEADParameters(new KeyParameter(key), 128, nonce, aad));

            byte[] out = new byte[cipher.getOutputSize(cipherData.length)];
            int len = cipher.processBytes(cipherData, 0, cipherData.length, out, 0);
            len += cipher.doFinal(out, len);
            return java.util.Arrays.copyOf(out, len);
        } catch (Exception e) {
            throw new MeshSecurityException("Decryption failed", e);
        }
    }
}
