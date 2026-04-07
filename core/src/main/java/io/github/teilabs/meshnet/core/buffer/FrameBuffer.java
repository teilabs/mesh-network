package io.github.teilabs.meshnet.core.buffer;

import io.github.teilabs.meshnet.core.frame.Frame;

public interface FrameBuffer {
    void addFrame(Frame frame);

    boolean containsFrame(Frame frame);

    Frame[] getAllFrames();
}
