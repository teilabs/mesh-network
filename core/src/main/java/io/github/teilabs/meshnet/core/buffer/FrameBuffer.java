package io.github.teilabs.meshnet.core.buffer;

import io.github.teilabs.meshnet.core.frame.Frame;

/**
 * Interface for storing {@link Frame Frames} for future redistributing.
 */
public interface FrameBuffer {
    /**
     * Adds {@link Frame} to the storage.
     * 
     * @param frame The frame to store.
     */
    void addFrame(Frame frame);

    /**
     * Checks if storage contains given frame.
     * 
     * @param frame The frame to check.
     * @return true - if frame is in the storage, false - otherwise.
     */
    boolean containsFrame(Frame frame);

    /**
     * Returns all stored frames.
     * 
     * @return The array contains all stored frames.
     */
    Frame[] getAllFrames();
}
