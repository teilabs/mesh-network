package io.github.teilabs.meshnet.core;

import io.github.teilabs.meshnet.core.frame.BinaryFrameParser;
import io.github.teilabs.meshnet.core.frame.Frame;

public class MeshCore implements CoreInput {
    private final CoreEvents coreEvents;

    private final BinaryFrameParser binaryFrameParser = new BinaryFrameParser();

    public MeshCore(CoreEvents coreEvents) {
        this.coreEvents = coreEvents;
    }

    @Override
    public void onRawFrameRecieved(byte[] rawFrame) {
        Frame frame = binaryFrameParser.parse(rawFrame);
    }
}