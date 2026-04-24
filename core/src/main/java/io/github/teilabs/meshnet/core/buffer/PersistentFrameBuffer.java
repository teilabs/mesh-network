package io.github.teilabs.meshnet.core.buffer;

import io.github.teilabs.meshnet.core.config.Config;
import io.github.teilabs.meshnet.core.frame.Frame;
import io.github.teilabs.meshnet.core.frame.FrameCodec;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Implementation of {@link FrameBuffer} that is using file storage to store
 * frames.
 */
public class PersistentFrameBuffer implements FrameBuffer {
    private final FrameBufferEvents frameBufferEvents;

    private final FrameCodec frameCodec;

    private final Config config;

    private final Set<Frame> frames = Collections.synchronizedSet(new LinkedHashSet<Frame>());

    public PersistentFrameBuffer(FrameBufferEvents frameBufferEvents, FrameCodec frameCodec, Config config) {
        this.frameBufferEvents = frameBufferEvents;
        this.frameCodec = frameCodec;
        this.config = config;

        // List all saved frames
        String[] files = this.frameBufferEvents.listFiles(this.config.storedFramesFolderPath());
        // For each frame parse it from bytes and put in set
        for (int i = 0; i < files.length; i++) {
            try {
                byte[] bytes = this.frameBufferEvents.readFile(this.config.storedFramesFolderPath() + files[i]);
                Frame parsedFrame = this.frameCodec.parse(bytes);
                frames.add(parsedFrame);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void addFrame(Frame frame) {
        // Checks if frame is already exists to prevent collisions
        if (containsFrame(frame)) {
            throw new IllegalArgumentException("Frame already stored");
        }

        // Removes oldest stored frame until count of frames is less than MAX_FRAMES
        while (frames.size() >= config.maxStoredFrames()) {
            Frame firstFrame = frames.iterator().next();
            frameBufferEvents.deleteFile(config.storedFramesFolderPath() + firstFrame.hashCode() + ".bin");
            frames.remove(firstFrame);
        }
        frameBufferEvents.writeFile(config.storedFramesFolderPath() + frame.hashCode() + ".bin",
                frameCodec.serialize(frame));
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
