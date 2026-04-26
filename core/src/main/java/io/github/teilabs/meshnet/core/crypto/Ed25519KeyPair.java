package io.github.teilabs.meshnet.core.crypto;

import io.github.teilabs.meshnet.core.exception.MeshValidationException;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Base64;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.generators.Ed25519KeyPairGenerator;
import org.bouncycastle.crypto.params.Ed25519KeyGenerationParameters;
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters;
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters;

/**
 * Record with Ed25519 key pair and routing id.
 * 
 * @param privateKey Ed25519 private key
 * @param publicKey  Ed25519 public key
 * @param routingId  routing id generated from public key
 * @throws MeshValidationException if keys are not 32 bytes.
 */
public record Ed25519KeyPair(byte[] privateKey, byte[] publicKey, long routingId) {
    public Ed25519KeyPair {
        if (privateKey.length != 32 || publicKey.length != 32)
            throw new MeshValidationException("Keys must be 32 bytes");
    }

    /**
     * Generates new Ed25519 key pair.
     * 
     * @return generated key pair
     */
    public static Ed25519KeyPair generate() {
        Ed25519KeyPairGenerator gen = new Ed25519KeyPairGenerator();
        gen.init(new Ed25519KeyGenerationParameters(new SecureRandom()));
        AsymmetricCipherKeyPair kp = gen.generateKeyPair();

        byte[] privateKey = ((Ed25519PrivateKeyParameters) kp.getPrivate()).getEncoded();
        byte[] publicKey = ((Ed25519PublicKeyParameters) kp.getPublic()).getEncoded();
        long routingId = generateRoutingId(publicKey);
        return new Ed25519KeyPair(privateKey, publicKey, routingId);
    }

    /**
     * Generates routing id from public key.
     * 
     * @param publicKey public key to generate routing id from
     * @return generated routing id
     */
    public static long generateRoutingId(byte[] publicKey) {
        return ByteBuffer.wrap(publicKey, 0, 8).getLong();
    }

    /**
     * Generates base64 encoded public key from key pair
     * 
     * @param keyPair key pair to generate encoded public key from
     * @return base64 encoded public key
     */
    public static String toBase64Public(Ed25519KeyPair keyPair) {
        return Base64.getEncoder().encodeToString(keyPair.publicKey);
    }

    /**
     * Generates base64 encoded private key from key pair
     * 
     * @param keyPair key pair to generate encoded private key from
     * @return base64 encoded private key
     */
    public static String toBase64Private(Ed25519KeyPair keyPair) {
        return Base64.getEncoder().encodeToString(keyPair.privateKey);
    }

    /**
     * Generates key pair from base64 encoded public and private keys
     * 
     * @param publicBase64  base64 encoded public key
     * @param privateBase64 base64 encoded private key
     * @return generated key pair
     * @throws MeshValidationException if keys are not 32 bytes after decoding.
     */
    public static Ed25519KeyPair fromBase64(String publicBase64, String privateBase64) throws MeshValidationException {
        byte[] publicKey = Base64.getDecoder().decode(publicBase64);
        byte[] privateKey = Base64.getDecoder().decode(privateBase64);
        long routingId = generateRoutingId(publicKey);
        return new Ed25519KeyPair(privateKey, publicKey, routingId);
    }
}
