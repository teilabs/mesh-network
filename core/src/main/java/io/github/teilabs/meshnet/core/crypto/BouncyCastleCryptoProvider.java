package io.github.teilabs.meshnet.core.crypto;

public final class BouncyCastleCryptoProvider implements CryptoProvider {
    @Override
    public Ed25519KeyPair generateKeyPair() {
        return Ed25519KeyPair.generate();
    }

    @Override
    public byte[] sign(byte[] data, byte[] privateKey) {
        return Ed25519Crypto.sign(data, privateKey);
    }

    @Override
    public boolean verify(byte[] data, byte[] signature, byte[] publicKey) {
        return Ed25519Crypto.verify(data, signature, publicKey);
    }

    @Override
    public byte[] encrypt(byte[] key, byte[] nonce, byte[] aad, byte[] plainData) {
        return AeadCipher.encrypt(key, nonce, aad, plainData);
    }

    @Override
    public byte[] decrypt(byte[] key, byte[] nonce, byte[] aad, byte[] cipherData) {
        return AeadCipher.decrypt(key, nonce, aad, cipherData);
    }

    @Override
    public byte[] generateNonce() {
        return NonceGenerator.generate();
    }

}
