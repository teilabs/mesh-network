package io.github.teilabs.meshnet.core.buffer;

import io.github.teilabs.meshnet.core.exception.MeshStorageException;

/**
 * Functions that {@link FrameBuffer} can call.
 */
public interface FrameBufferEvents {
    /**
     * Write a file to the file system.
     * 
     * @param path The path to the file.
     * @param data The data to write.
     * @throws MeshStorageException if the file writing failed.
     */
    void writeFile(String path, byte[] data) throws MeshStorageException;

    /**
     * Reads a file from the file system.
     * 
     * @param path The path to the file.
     * @return The data read from the file.
     * @throws MeshStorageException if the file reading failed.
     */
    byte[] readFile(String path) throws MeshStorageException;

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
