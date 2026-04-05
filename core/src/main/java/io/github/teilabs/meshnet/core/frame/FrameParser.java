package io.github.teilabs.meshnet.core.frame;

public interface FrameParser<RawFrameType> {
    Frame parse(RawFrameType rawFrame);
    RawFrameType serialize(Frame frame);
}
