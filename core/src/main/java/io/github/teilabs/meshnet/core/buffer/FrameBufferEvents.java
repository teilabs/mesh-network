package io.github.teilabs.meshnet.core.buffer;

import java.io.IOException;

/**
 * Functions that {@link FrameBuffer} can call.
 */
public interface FrameBufferEvents {
    /**
     * Write a file to the file system.
     * 
     * @param path The path to the file.
     * @param data The data to write.
     */
    void writeFile(String path, byte[] data);

    /**
     * Reads a file from the file system.
     * 
     * @param path The path to the file.
     * @return The data read from the file.
     * @throws IOException 
     */
    byte[] readFile(String path) throws IOException;

    /**
     * Lists the files in a folder.
     * 
     * @param folderPath The path to the folder (must ends with /).
     * @return The array with names of the files in the folder.
     */
    String[] listFiles(String folderPath);

    /**
     * Deletes a file from the file system.
     * 
     * @param path The path to the file.
     */
    void deleteFile(String path);
}
