package io.github.teilabs.meshnet.core.buffer;

import io.github.teilabs.meshnet.core.config.Config;
import io.github.teilabs.meshnet.core.frame.Frame;
import io.github.teilabs.meshnet.core.frame.FrameCodec;
import io.github.teilabs.meshnet.core.util.Logger;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Implementation of {@link FrameBuffer} that is using file storage to store
 * frames.
 */
public class PersistentFrameBuffer implements FrameBuffer {
    private static final String TAG = "PersistentFrameBuffer";

    private final FrameBufferEvents frameBufferEvents;

    private final FrameCodec frameCodec;

    private final Config config;

    private final Logger logger;

    private final Set<Frame> frames = Collections.synchronizedSet(new LinkedHashSet<Frame>());

    public PersistentFrameBuffer(FrameBufferEvents frameBufferEvents, FrameCodec frameCodec, Config config,
            Logger logger) {
        this.frameBufferEvents = frameBufferEvents;
        this.frameCodec = frameCodec;
        this.config = config;
        this.logger = logger;

        // List all saved frames
        String[] files = this.frameBufferEvents.listFiles(this.config.storedFramesFolderPath());
        logger.i(TAG, "Loading " + files.length + " stored frames");
        // For each frame parse it from bytes and put in set
        for (int i = 0; i < files.length; i++) {
            try {
                byte[] bytes = this.frameBufferEvents.readFile(this.config.storedFramesFolderPath() + files[i]);
                Frame parsedFrame = this.frameCodec.parse(bytes);
                frames.add(parsedFrame);
            } catch (IOException e) {
                logger.e(TAG, "Failed to load frame from file: " + files[i], e);
            }
        }
    }

    @Override
    public void addFrame(Frame frame) {
        // Checks if frame is already exists to prevent collisions
        if (containsFrame(frame)) {
            logger.w(TAG, "Frame already stored: " + frame.hashCode());
            throw new IllegalArgumentException("Frame already stored");
        }

        // Removes oldest stored frame until count of frames is less than MAX_FRAMES
        while (frames.size() >= config.maxStoredFrames()) {
            Frame firstFrame = frames.iterator().next();
            logger.w(TAG, "Evicting oldest stored frame: " + firstFrame.hashCode());
            frameBufferEvents.deleteFile(config.storedFramesFolderPath() + firstFrame.hashCode() + ".bin");
            frames.remove(firstFrame);
        }
        frameBufferEvents.writeFile(config.storedFramesFolderPath() + frame.hashCode() + ".bin",
                frameCodec.serialize(frame));
        frames.add(frame);
        logger.i(TAG, "Stored frame: " + frame.hashCode());
    }

    @Override
    public boolean containsFrame(Frame frame) {
        return frames.contains(frame);
    }

    @Override
    public Frame[] getAllFrames() {
        logger.d(TAG, "Returning " + frames.size() + " buffered frames");
        return frames.toArray(new Frame[0]);
    }
}
