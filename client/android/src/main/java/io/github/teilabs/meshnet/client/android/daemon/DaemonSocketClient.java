package io.github.teilabs.meshnet.client.android.daemon;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public final class DaemonSocketClient {

    private static final String DAEMON_HOST = "127.0.0.1";
    private static final int DAEMON_PORT = 18881;
    private static final int SOCKET_TIMEOUT_MS = 60000;

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
                    byte[] data = new byte[len];
                    in.readFully(data);

                    onMessage.onMessage(data);

                } catch (IOException e) {
                    if (running) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException ex) {
                        }
                    }
                }
            }
        }, "DaemonSocketClient-Read").start();
    }

    public void send(byte[] data) throws IOException {
        if (data == null)
            return;
        Socket s = socket;
        if (s != null && !s.isClosed()) {
            DataOutputStream out = new DataOutputStream(s.getOutputStream());
            out.writeInt(data.length);
            out.write(data);
            out.flush();
        }
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