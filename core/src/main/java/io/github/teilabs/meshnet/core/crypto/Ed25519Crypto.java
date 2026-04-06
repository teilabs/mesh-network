package io.github.teilabs.meshnet.core.crypto;

import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters;
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters;
import org.bouncycastle.crypto.signers.Ed25519Signer;

public final class Ed25519Crypto {
    public static byte[] sign(byte[] data, byte[] privateKey) {
        Ed25519Signer signer = new Ed25519Signer();

        signer.init(true, new Ed25519PrivateKeyParameters(privateKey));
        signer.update(data, 0, data.length);

        return signer.generateSignature();
    }

    public static boolean verify(byte[] data, byte[] signature, byte[] publicKey) {
        Ed25519Signer signer = new Ed25519Signer();

        signer.init(false, new Ed25519PublicKeyParameters(publicKey));
        signer.update(data, 0, data.length);
        
        return signer.verifySignature(signature);
    }
}
