package io.github.teilabs.meshnet.core;

public interface CoreEvents {
    void sendRawFrame(byte[] rawFrame, byte[] to);

    void sendRawFrameToEveryone(byte[] rawFrame);
}
