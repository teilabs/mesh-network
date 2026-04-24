package io.github.teilabs.meshnet.client.android.daemon;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import io.github.teilabs.meshnet.client.android.util.Logger;

public final class DaemonSocketClient {
    private static final String TAG = "DaemonSocketClient";

    private static final String DAEMON_HOST = "127.0.0.1";
    private static final int DAEMON_PORT = 18881;
    private static final int SOCKET_TIMEOUT_MS = 60000;

    private static final byte TYPE_DATA = 0x01;
    private static final byte TYPE_START_ADVERTISING = 0x02;
    private static final byte TYPE_STOP_ADVERTISING = 0x03;
    private static final byte TYPE_INCOMING = 0x04;

    @FunctionalInterface
    public interface MessageHandler {
        void onMessage(byte[] data);
    }

    private final MessageHandler onMessage;

    private Socket socket;
    private volatile boolean running = false;

    public DaemonSocketClient(MessageHandler onMessage) {
        if (onMessage == null)
            throw new IllegalArgumentException("MessageHandler cannot be null");
        this.onMessage = onMessage;
    }

    public void start() {
        if (running)
            return;
        running = true;

        new Thread(() -> {
            while (running) {
                try {
                    if (socket == null || socket.isClosed()) {
                        socket = new Socket(DAEMON_HOST, DAEMON_PORT);
                        socket.setSoTimeout(SOCKET_TIMEOUT_MS);
                    }

                    DataInputStream in = new DataInputStream(socket.getInputStream());
                    int len = in.readInt();
                    byte type = in.readByte();
                    byte[] payload = new byte[len - 1];
                    in.readFully(payload);

                    switch (type) {
                        case TYPE_INCOMING: {
                            onMessage.onMessage(payload);
                            break;
                        }
                        default: {
                            Logger.e(TAG, "Unsupported message type.");
                            break;
                        }
                    }

                } catch (IOException e) {
                    if (running) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException ignored) {
                        }
                    }
                }
            }
        }, "DaemonSocketClient-Read").start();
    }

    private void sendWithHeader(byte type, byte[] data) throws IOException {
        if (data == null)
            return;
        Socket s = socket;
        if (s != null && !s.isClosed()) {
            DataOutputStream out = new DataOutputStream(s.getOutputStream());
            out.writeInt(data.length + 1);
            out.write(type);
            out.write(data);
            out.flush();
        }
    }

    public void send(byte[] data) throws IOException {
        sendWithHeader(TYPE_DATA, data);
    }

    public void startAdvertising(byte[] data, int intervalMs) throws IOException {
        byte[] payload = new byte[4 + data.length];
        payload[0] = (byte) ((intervalMs >> 24) & 0xFF);
        payload[1] = (byte) ((intervalMs >> 16) & 0xFF);
        payload[2] = (byte) ((intervalMs >> 8) & 0xFF);
        payload[3] = (byte) (intervalMs & 0xFF);
        System.arraycopy(data, 0, payload, 4, data.length);
        sendWithHeader(TYPE_START_ADVERTISING, payload);
    }

    public void stopAdvertising() throws IOException {
        sendWithHeader(TYPE_STOP_ADVERTISING, new byte[0]);
    }

    public void stop() {
        running = false;
        try {
            if (socket != null)
                socket.close();
        } catch (IOException e) {
        }
    }
}