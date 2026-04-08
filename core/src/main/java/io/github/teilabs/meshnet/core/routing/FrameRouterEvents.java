package io.github.teilabs.meshnet.core.routing;

import io.github.teilabs.meshnet.core.api.MeshIncomingMessage;
import io.github.teilabs.meshnet.core.frame.Frame;

public interface FrameRouterEvents {
    void sendFrame(Frame frame, long to);

    void sendFrameToEveryone(Frame frame);

    void transferMessageToApp(MeshIncomingMessage message);

    boolean checkConnectionToNode(long nodeRoutingId);
}
