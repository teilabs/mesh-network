package io.github.teilabs.meshnet.core;

import io.github.teilabs.meshnet.core.api.MeshIncomingMessage;
import io.github.teilabs.meshnet.core.crypto.Ed25519KeyPair;
import io.github.teilabs.meshnet.core.routing.Tunnel;

/**
 * Functions that {@link MeshCore} can call.
 */
public interface CoreEvents {
    /**
     * Sends a frame bytes to the specific node.
     *
     * @param rawFrame      bytes of frame to send
     * @param nodeRoutingId routingId of node to send
     */
    void sendRawFrame(byte[] rawFrame, long nodeRoutingId);

    /**
     * Sends a frame bytes to all connected nodes.
     *
     * @param rawFrame bytes of frame to send
     */
    void sendRawFrameToEveryone(byte[] rawFrame);

    /**
     * Gets publick and priavte key from storage.
     * 
     * @return null - if storage doesn't have any key pairs, key pair from storage -
     *         otherwise
     */
    Ed25519KeyPair getKeyPair();

    /**
     * Puts key pair to storage and saves it.
     * 
     * @param keyPair key pair to save
     * @return saved key pair
     */
    Ed25519KeyPair saveKeyPair(Ed25519KeyPair keyPair);

    /**
     * Sends information to the destination app that message for it come and gives
     * the message.
     * 
     * @param message message to transfer
     */
    void transferMessageToApp(MeshIncomingMessage message);

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
     */
    byte[] readFile(String path);

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

    /**
     * Checks if node is connected
     * 
     * @param nodeRoutingId routing id of node to check
     * @return true - if node is connecte, false - otherwise
     */
    boolean checkConnectionToNode(long nodeRoutingId);

    /**
     * Asks user to open the tunnel.
     * 
     * @param tunnel tunnel that we want to open
     * @return true - if user accepted to open this tunnel, false - otherwise
     */
    boolean checkTunnelOpenAccess(Tunnel tunnel);
}
