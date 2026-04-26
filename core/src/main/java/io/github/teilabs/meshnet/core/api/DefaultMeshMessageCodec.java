package io.github.teilabs.meshnet.core.api;

import io.github.teilabs.meshnet.core.crypto.CryptoProvider;
import io.github.teilabs.meshnet.core.crypto.Ed25519KeyPair;
import io.github.teilabs.meshnet.core.exception.MeshSecurityException;
import io.github.teilabs.meshnet.core.frame.Frame;
import io.github.teilabs.meshnet.core.frame.FrameCodec;
import io.github.teilabs.meshnet.core.frame.FrameConstants;

/**
 * Default implementation of {@link MeshMessageCodec}.
 */
public final class DefaultMeshMessageCodec implements MeshMessageCodec {
    private final CryptoProvider cryptoProvider;

    private final FrameCodec frameCodec;

    private final Ed25519KeyPair keyPair;

    public DefaultMeshMessageCodec(CryptoProvider cryptoProvider, FrameCodec frameCodec, Ed25519KeyPair keyPair) {
        this.cryptoProvider = cryptoProvider;
        this.frameCodec = frameCodec;
        this.keyPair = keyPair;
    }

    @Override
    public MeshIncomingMessage parseIncomingFrame(Frame frame) throws MeshSecurityException {
        // Validate frame signature to prove author
        if (!cryptoProvider.verify(frameCodec.serializeHeader(frame), frame.getSignature(),
                frame.getSrcPubKey())) {
            throw new MeshSecurityException("Invalid signature. Author prove failed");
        }
        byte[] decryptedData = cryptoProvider.decrypt(keyPair.privateKey(), frame.getNonce(),
                frameCodec.serializeHeader(frame), frame.getEncryptedData());
        return new MeshIncomingMessage(frame.getTimestamp(), frame.getSrcAppId(), frame.getDstAppId(),
                frame.getSrcPubKey(), decryptedData);
    }

    @Override
    public Frame generateOutgoingFrame(MeshOutgoingMessage message) {
        byte version = FrameConstants.VERSION;
        byte type = message.getType();
        int timestamp = (int) (System.currentTimeMillis());
        short srcAppId = message.getSrcAppId();
        short dstAppId = message.getDstAppId();
        byte[] srcPubKey = keyPair.publicKey();
        long dstRoutingId = Ed25519KeyPair.generateRoutingId(message.getDstPubKey());
        byte[] nonce = cryptoProvider.generateNonce();
        boolean direction = message.getDirection();

        // Serialize header fields for future usage
        byte[] serializedHeader = frameCodec.serializeHeader(new Frame(version, type, timestamp, srcAppId,
                dstAppId, srcPubKey, dstRoutingId, nonce, direction, new byte[0], new byte[0]));

        // Sign frame header for author proving on destination node
        byte[] signature = cryptoProvider.sign(serializedHeader,
                keyPair.privateKey());
        byte[] encryptedData = cryptoProvider.encrypt(message.getDstPubKey(), nonce, serializedHeader,
                message.getData());

        return new Frame(
                version, type, timestamp, srcAppId, dstAppId, srcPubKey, dstRoutingId, nonce, direction, signature,
                encryptedData);
    }
}
