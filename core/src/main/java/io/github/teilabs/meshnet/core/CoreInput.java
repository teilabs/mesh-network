package io.github.teilabs.meshnet.core;

import io.github.teilabs.meshnet.core.api.MeshOutgoingMessage;
import io.github.teilabs.meshnet.core.crypto.Ed25519KeyPair;

/**
 * Events that {@link MeshCore} can handle.
 */
public interface CoreInput {
    /**
     * Called by daemon when it receives bytes from another node.
     * 
     * @param bytes received bytes
     */
    void onBytesReceived(byte[] bytes);

    /**
     * Called by daemon when app want to send message through mesh network.
     * 
     * @param message message that app want to send
     */
    void onAppSendMessage(MeshOutgoingMessage message);

    /**
     * Called by daemon to shutdown MeshCore.
     */
    void shutdown();

    /**
     * Called by daemon to change advertising state to started.
     * 
     * @param intervalMs interval in milliseconds
     */
    void startAdvertising(int intervalMs);

    /**
     * Called by daemon to change advertising state to stopped.
     */
    void stopAdvertising();

    /**
     * Called by daemon to get current node's key pair without private key.
     */
    Ed25519KeyPair getKeyPair();
}
