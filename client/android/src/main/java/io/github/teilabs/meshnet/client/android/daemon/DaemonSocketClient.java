package io.github.teilabs.meshnet.client.android.daemon;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import io.github.teilabs.meshnet.client.android.util.AndroidLogger;
import io.github.teilabs.meshnet.core.exception.MeshValidationException;

/**
 * Class for socket communication with the daemon.
 */
public final class DaemonSocketClient {
    private static final String TAG = "DaemonSocketClient";

    private static final String DAEMON_HOST = "127.0.0.1";
    private static final int DAEMON_PORT = 18881;
    private static final int SOCKET_TIMEOUT_MS = 60000;

    /**
     * Message that will be sent once to all types of connections that daemon
     * support.
     */
    private static final byte TYPE_DATA = 0x01;
    /**
     * Message that requests the daemon to start advertising with specified payload
     * and interval.
     */
    private static final byte TYPE_START_ADVERTISING = 0x02;
    /**
     * Message that requests the daemon to stop advertising.
     */
    private static final byte TYPE_STOP_ADVERTISING = 0x03;
    /**
     * Message that daemon receive from one of connection types.
     */
    private static final byte TYPE_INCOMING = 0x04;

    @FunctionalInterface
    public interface MessageHandler {
        /**
         * Called when a message is received from the daemon.
         *
         * @param data The message data.
         */
        void onMessage(byte[] data);
    }

    private final MessageHandler onMessage;

    private Socket socket;
    private volatile boolean running = false;

    public DaemonSocketClient(MessageHandler onMessage) {
        if (onMessage == null)
            throw new MeshValidationException("MessageHandler cannot be null");
        this.onMessage = onMessage;
    }

    /**
     * Starts the daemon socket client.
     */
    public void start() {
        if (running) {
            AndroidLogger.d(TAG, "Start requested while client is already running");
            return;
        }
        running = true;
        AndroidLogger.i(TAG, "Starting daemon socket client");

        new Thread(() -> {
            // Loop for reading data from socket while it is running
            while (running) {
                try {
                    // Connect to the socket if it is closed
                    if (socket == null || socket.isClosed()) {
                        AndroidLogger.i(TAG, "Connecting to daemon " + DAEMON_HOST + ":" + DAEMON_PORT);
                        socket = new Socket(DAEMON_HOST, DAEMON_PORT);
                        socket.setSoTimeout(SOCKET_TIMEOUT_MS);
                        AndroidLogger.i(TAG, "Connected to daemon");
                    }

                    // Read data from the socket if is is present, otherwise IOException will be
                    // thrown
                    DataInputStream in = new DataInputStream(socket.getInputStream());
                    int len = in.readInt();
                    byte type = in.readByte();
                    byte[] payload = new byte[len - 1];
                    in.readFully(payload);
                    AndroidLogger.d(TAG, "Received daemon message type " + type + " with " + payload.length + " bytes");

                    // Process payload according to the type
                    switch (type) {
                        case TYPE_INCOMING: {
                            onMessage.onMessage(payload);
                            break;
                        }
                        default: {
                            AndroidLogger.e(TAG, "Unsupported message type.");
                            break;
                        }
                    }

                } catch (IOException e) {
                    if (running) {
                        AndroidLogger.w(TAG, "Daemon connection issue, retrying");
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException ignored) {
                            AndroidLogger.w(TAG, "Retry sleep interrupted");
                        }
                    }
                }
            }
            AndroidLogger.i(TAG, "Daemon socket reader stopped");
        }, "DaemonSocketClient-Read").start();
    }

    /**
     * Sends a message to the daemon.
     *
     * @param type The message type.
     * @param data The message data.
     * @throws IOException If an I/O error occurs.
     */
    private void sendWithHeader(byte type, byte[] data) throws IOException {
        if (data == null) {
            AndroidLogger.w(TAG, "Ignoring send request with null payload for type " + type);
            return;
        }
        Socket s = socket;
        if (s != null && !s.isClosed()) {
            DataOutputStream out = new DataOutputStream(s.getOutputStream());
            out.writeInt(data.length + 1);
            out.write(type);
            out.write(data);
            out.flush();
            AndroidLogger.d(TAG, "Sent daemon message type " + type + " with " + data.length + " bytes");
        } else {
            AndroidLogger.w(TAG, "Cannot send daemon message because socket is not connected");
        }
    }

    /**
     * Sends a data-message to the daemon.
     *
     * @param data The message data.
     * @throws IOException If an I/O error occurs.
     */
    public void send(byte[] data) throws IOException {
        sendWithHeader(TYPE_DATA, data);
    }

    /**
     * Sends a start-advertising-message to the daemon.
     *
     * @param data       The message data.
     * @param intervalMs The interval between advertising in milliseconds.
     * @throws IOException If an I/O error occurs.
     */
    public void startAdvertising(byte[] data, int intervalMs) throws IOException {
        AndroidLogger.i(TAG, "Sending start advertising command with interval " + intervalMs + " ms");
        byte[] payload = new byte[4 + data.length];
        payload[0] = (byte) ((intervalMs >> 24) & 0xFF);
        payload[1] = (byte) ((intervalMs >> 16) & 0xFF);
        payload[2] = (byte) ((intervalMs >> 8) & 0xFF);
        payload[3] = (byte) (intervalMs & 0xFF);
        System.arraycopy(data, 0, payload, 4, data.length);
        sendWithHeader(TYPE_START_ADVERTISING, payload);
    }

    /**
     * Sends a stop-advertising-message to the daemon.
     *
     * @throws IOException If an I/O error occurs.
     */
    public void stopAdvertising() throws IOException {
        AndroidLogger.i(TAG, "Sending stop advertising command");
        sendWithHeader(TYPE_STOP_ADVERTISING, new byte[0]);
    }

    /**
     * Stops the daemon socket client.
     */
    public void stop() {
        running = false;
        AndroidLogger.i(TAG, "Stopping daemon socket client");
        try {
            if (socket != null)
                socket.close();
        } catch (IOException e) {
            AndroidLogger.e(TAG, "Failed to close daemon socket", e);
        }
    }
}
