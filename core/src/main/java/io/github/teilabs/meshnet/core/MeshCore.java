package io.github.teilabs.meshnet.core;

import io.github.teilabs.meshnet.core.crypto.CryptoProvider;
import io.github.teilabs.meshnet.core.crypto.Ed25519KeyPair;
import io.github.teilabs.meshnet.core.frame.Frame;
import io.github.teilabs.meshnet.core.frame.FrameCodec;
import java.nio.ByteBuffer;

public class MeshCore implements CoreInput {
    private final CoreEvents coreEvents;

    private final FrameCodec frameParser;

    private final CryptoProvider cryptoProvider;

    private Ed25519KeyPair keyPair;

    public MeshCore(CoreEvents coreEvents, FrameCodec frameParser, CryptoProvider cryptoProvider) {
        this.coreEvents = coreEvents;
        this.frameParser = frameParser;
        this.cryptoProvider = cryptoProvider;
        this.keyPair = (this.coreEvents.getKeyPair() != null) ? this.coreEvents.getKeyPair() : this.coreEvents.saveKeyPair(this.cryptoProvider.generateKeyPair());
    }

    @Override
    public void onRawFrameRecieved(byte[] rawFrame) {
        Frame frame = frameParser.parse(rawFrame);
        if (frame.getDstRoutingId() == ByteBuffer.wrap(keyPair.publicKey(), 0, 8).getLong()) {

        }
        else {

        }
    }
}