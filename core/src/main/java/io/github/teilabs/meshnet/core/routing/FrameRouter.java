package io.github.teilabs.meshnet.core.routing;

import io.github.teilabs.meshnet.core.frame.Frame;

public interface FrameRouter {
    void onFrameRecieved(Frame frame);

    void sendFrame(Frame frame);
}
