package io.github.teilabs.meshnet.core.buffer;

import io.github.teilabs.meshnet.core.frame.Frame;
import io.github.teilabs.meshnet.core.frame.FrameCodec;
import java.util.LinkedHashSet;
import java.util.Set;

public class PersistentFrameBuffer implements FrameBuffer {
    private static final int MAX_FRAMES = 1000;
    public static final String FOLDER_PATH = "frame_buffer/";

    private final FrameBufferEvents frameBufferEvents;

    private final Set<Frame> frames = new LinkedHashSet<>();

    private final FrameCodec frameCodec;

    public PersistentFrameBuffer(FrameBufferEvents frameBufferEvents, FrameCodec frameCodec) {
        this.frameBufferEvents = frameBufferEvents;
        this.frameCodec = frameCodec;

        // List all sved frames
        String[] files = this.frameBufferEvents.listFiles(FOLDER_PATH);
        // For each frame parse it from bytes and put in set
        for (int i = 0; i < files.length; i++) {
            byte[] bytes = this.frameBufferEvents.readFile(FOLDER_PATH + files[i]);
            Frame parsedFrame = this.frameCodec.parse(bytes);
            frames.add(parsedFrame);
        }
    }

    @Override
    public void addFrame(Frame frame) {
        if (containsFrame(frame)) {
            throw new IllegalArgumentException("Frame already stored");
        }

        while (frames.size() >= MAX_FRAMES) {
            Frame firstFrame = frames.iterator().next();
            frameBufferEvents.deleteFile(FOLDER_PATH + firstFrame.hashCode() + ".bin");
            frames.remove(firstFrame);
        }
        frameBufferEvents.writeFile(FOLDER_PATH + frame.hashCode() + ".bin", frameCodec.serialize(frame));
        frames.add(frame);
    }

    @Override
    public boolean containsFrame(Frame frame) {
        return frames.contains(frame);
    }

    @Override
    public Frame[] getAllFrames() {
        return frames.toArray(new Frame[0]);
    }
}
