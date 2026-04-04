package io.github.teilabs.core;

public interface CoreEvents {
    void sendRawFrame(byte[] rawFrame, byte[] to);

    void sendRawFrameToEveryone(byte[] rawFrame);
}
