package io.github.teilabs.meshnet.core.api;

import io.github.teilabs.meshnet.core.frame.Frame;

public interface MeshMessageCodec {
    MeshIncomingMessage parseIncomingFrame(Frame frame);

    Frame generateOutgoingFrame(MeshOutgoingMessage message);
}
