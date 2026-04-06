package io.github.teilabs.meshnet.core;

import io.github.teilabs.meshnet.core.crypto.Ed25519KeyPair;

public interface CoreEvents {
    void sendRawFrame(byte[] rawFrame, byte[] to);

    void sendRawFrameToEveryone(byte[] rawFrame);

    Ed25519KeyPair saveKeyPair(Ed25519KeyPair keyPair);
}
