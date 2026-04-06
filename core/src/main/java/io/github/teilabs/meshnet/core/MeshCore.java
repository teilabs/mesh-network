package io.github.teilabs.meshnet.core;

import io.github.teilabs.meshnet.core.crypto.CryptoProvider;
import io.github.teilabs.meshnet.core.crypto.Ed25519KeyPair;
import io.github.teilabs.meshnet.core.frame.Frame;
import io.github.teilabs.meshnet.core.frame.FrameParser;
import java.nio.ByteBuffer;

public class MeshCore implements CoreInput {
    private final CoreEvents coreEvents;

    private final FrameParser frameParser;

    private final CryptoProvider cryptoProvider;

    private final Ed25519KeyPair keyPair;

    public MeshCore(CoreEvents coreEvents, FrameParser frameParser, CryptoProvider cryptoProvider,
            Ed25519KeyPair keyPair) {
        this.coreEvents = coreEvents;
        this.frameParser = frameParser;
        this.cryptoProvider = cryptoProvider;
        this.keyPair = (keyPair != null) ? keyPair : this.coreEvents.saveKeyPair(this.cryptoProvider.generateKeyPair());
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