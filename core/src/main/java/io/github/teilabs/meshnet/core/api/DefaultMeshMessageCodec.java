package io.github.teilabs.meshnet.core.api;

import io.github.teilabs.meshnet.core.crypto.CryptoProvider;
import io.github.teilabs.meshnet.core.crypto.Ed25519KeyPair;
import io.github.teilabs.meshnet.core.frame.Frame;
import io.github.teilabs.meshnet.core.frame.FrameCodec;
import io.github.teilabs.meshnet.core.frame.FrameConstants;
import io.github.teilabs.meshnet.core.routing.Tunnel;

public final class DefaultMeshMessageCodec implements MeshMessageCodec {
    private final CryptoProvider cryptoProvider;

    private final FrameCodec frameCodec;

    private Ed25519KeyPair keyPair;

    public DefaultMeshMessageCodec(CryptoProvider cryptoProvider, FrameCodec frameCodec, Ed25519KeyPair keyPair) {
        this.cryptoProvider = cryptoProvider;
        this.frameCodec = frameCodec;
        this.keyPair = keyPair;
    }

    @Override
    public MeshIncomingMessage parseIncomingFrame(Frame frame) {
        // Validate frame signature to prove author
        if (!cryptoProvider.verify(frameCodec.serializeHeader(frame), frame.getSignature(), frame.getSrcPubKey())) {
            throw new IllegalArgumentException("Invalid signature. Author prove failed");
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
        long tunnelId = 0;

        if (type == 1 || type == 2 || type == 3) {
            tunnelId = Tunnel.generateTunnelId(Ed25519KeyPair.generateRoutingId(srcPubKey), dstRoutingId);
        }

        // Serialize header fields for future usage
        byte[] serializedHeader = frameCodec.serializeHeader(new Frame(version, type, timestamp, srcAppId,
                dstAppId, srcPubKey, dstRoutingId, nonce, tunnelId, new byte[0], new byte[0]));

        // Signing frame header for author proving on destination node
        byte[] signature = cryptoProvider.sign(serializedHeader,
                keyPair.privateKey());
        byte[] encryptedData = cryptoProvider.encrypt(message.getDstPubKey(), nonce, serializedHeader,
                message.getdata());

        return new Frame(
                version, type, timestamp, srcAppId, dstAppId, srcPubKey, dstRoutingId, nonce, tunnelId, signature,
                encryptedData);
    }
}
