package io.github.teilabs.meshnet.core.buffer;

public interface FrameBufferEvents {
    void writeFile(String path, byte[] data);

    byte[] readFile(String path);

    String[] listFiles(String folderPath);

    void deleteFile(String path);
}
