package io.github.teilabs.meshnet.core.crypto;

import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters;
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters;
import org.bouncycastle.crypto.signers.Ed25519Signer;

/**
 * Class for Ed25519 signing and verification.
 */
public final class Ed25519Crypto {
    private Ed25519Crypto() {
    }

    /**
     * Signs bytes with private key.
     * 
     * @param data       bytes to sign
     * @param privateKey private key to sign with
     * @return signature bytes
     */
    public static byte[] sign(byte[] data, byte[] privateKey) {
        Ed25519Signer signer = new Ed25519Signer();

        signer.init(true, new Ed25519PrivateKeyParameters(privateKey));
        signer.update(data, 0, data.length);

        return signer.generateSignature();
    }

    /**
     * Verifies signature.
     * 
     * @param data      signed bytes
     * @param signature signature to verify
     * @param publicKey public key to verify with
     * @return true - if signature is valid, false - otherwise
     */
    public static boolean verify(byte[] data, byte[] signature, byte[] publicKey) {
        Ed25519Signer signer = new Ed25519Signer();

        signer.init(false, new Ed25519PublicKeyParameters(publicKey));
        signer.update(data, 0, data.length);

        return signer.verifySignature(signature);
    }
}
