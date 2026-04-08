package io.github.teilabs.meshnet.core;

import io.github.teilabs.meshnet.core.api.MeshIncomingMessage;
import io.github.teilabs.meshnet.core.crypto.Ed25519KeyPair;

public interface CoreEvents {
    void sendRawFrame(byte[] rawFrame, long to);

    void sendRawFrameToEveryone(byte[] rawFrame);

    Ed25519KeyPair getKeyPair();

    Ed25519KeyPair saveKeyPair(Ed25519KeyPair keyPair);

    void transferMessageToApp(MeshIncomingMessage message);

    void writeFile(String path, byte[] data);

    byte[] readFile(String path);

    String[] listFiles(String folderPath);

    void deleteFile(String path);

    boolean checkConnectionToNode(long nodeRoutingId);
}
