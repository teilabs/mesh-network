package io.github.teilabs.meshnet.core;

import io.github.teilabs.meshnet.core.api.MeshOutgoingMessage;

public interface CoreInput {
    void onRawFrameRecieved(byte[] rawFrame);

    void onAppSendMessage(MeshOutgoingMessage message);
}
