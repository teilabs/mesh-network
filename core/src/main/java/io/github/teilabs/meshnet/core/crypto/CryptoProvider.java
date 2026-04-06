package io.github.teilabs.meshnet.core.crypto;

import org.bouncycastle.crypto.InvalidCipherTextException;

public interface CryptoProvider {
    Ed25519KeyPair generateKeyPair();

    byte[] sign(byte[] data, byte[] privateKey);

    boolean verify(byte[] data, byte[] signature, byte[] publicKey);

    byte[] encrypt(byte[] key, byte[] nonce, byte[] aad, byte[] plainData) throws IllegalStateException, InvalidCipherTextException;

    byte[] decrypt(byte[] key, byte[] nonce, byte[] aad, byte[] cipherData);

    byte[] generateNonce();
}
