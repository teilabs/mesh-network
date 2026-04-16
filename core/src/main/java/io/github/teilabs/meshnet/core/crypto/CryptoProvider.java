package io.github.teilabs.meshnet.core.crypto;

/**
 * Interface for crypto operations used by mesh core.
 */
public interface CryptoProvider {
    /**
     * Generates new Ed25519 key pair.
     * 
     * @return generated key pair
     */
    Ed25519KeyPair generateKeyPair();

    /**
     * Signs bytes with private key.
     * 
     * @param data       bytes to sign
     * @param privateKey private key to sign with
     * @return signature bytes
     */
    byte[] sign(byte[] data, byte[] privateKey);

    /**
     * Verifies signature.
     * 
     * @param data      signed bytes
     * @param signature signature to verify
     * @param publicKey public key to verify with
     * @return true - if signature is valid, false - otherwise
     */
    boolean verify(byte[] data, byte[] signature, byte[] publicKey);

    /**
     * Encrypts bytes.
     * 
     * @param key       key to encrypt with
     * @param nonce     nonce to encrypt with
     * @param aad       additional authenticated data
     * @param plainData plain bytes to encrypt
     * @return encrypted bytes
     */
    byte[] encrypt(byte[] key, byte[] nonce, byte[] aad, byte[] plainData);

    /**
     * Decrypts bytes.
     * 
     * @param key        key to decrypt with
     * @param nonce      nonce used for encryption
     * @param aad        additional authenticated data
     * @param cipherData encrypted bytes
     * @return decrypted bytes
     */
    byte[] decrypt(byte[] key, byte[] nonce, byte[] aad, byte[] cipherData);

    /**
     * Generates nonce.
     * 
     * @return generated nonce bytes
     */
    byte[] generateNonce();
}
