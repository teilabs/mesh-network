package io.github.teilabs.meshnet.core.crypto;

import java.nio.ByteBuffer;
import java.security.SecureRandom;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.generators.Ed25519KeyPairGenerator;
import org.bouncycastle.crypto.params.Ed25519KeyGenerationParameters;
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters;
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters;

public record Ed25519KeyPair(byte[] privateKey, byte[] publicKey, long routingId) {
    public Ed25519KeyPair {
        if (privateKey.length != 32 || publicKey.length != 32) 
            throw new IllegalArgumentException("Keys must be 32 bytes");
    }

    public static Ed25519KeyPair generate() {
        Ed25519KeyPairGenerator gen = new Ed25519KeyPairGenerator();
        gen.init(new Ed25519KeyGenerationParameters(new SecureRandom()));
        AsymmetricCipherKeyPair kp = gen.generateKeyPair();

        byte[] privateKey = ((Ed25519PrivateKeyParameters) kp.getPrivate()).getEncoded();
        byte[] publicKey = ((Ed25519PublicKeyParameters) kp.getPublic()).getEncoded();
        long routingId = ByteBuffer.wrap(publicKey, 0, 8).getLong();;
        return new Ed25519KeyPair(privateKey, publicKey, routingId);
    }
}
