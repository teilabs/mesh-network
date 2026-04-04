package io.github.teilabs.core.frame;

public interface FrameParser<RawFrameType> {
    Frame parse(RawFrameType rawFrame);
    RawFrameType serialize(Frame frame);
}
