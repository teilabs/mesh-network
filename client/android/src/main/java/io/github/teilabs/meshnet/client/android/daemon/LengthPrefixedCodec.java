package io.github.teilabs.meshnet.client.android.daemon;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public final class LengthPrefixedCodec {
    private LengthPrefixedCodec() {
    }

    public static void send(Socket socket, byte[] payload) throws IOException {
        if (payload == null) {
            throw new IllegalArgumentException("Payload cannot be null");
        }

        DataOutputStream out = new DataOutputStream(socket.getOutputStream());
        out.writeInt(payload.length);
        out.write(payload);
        out.flush();
    }

    public static byte[] receive(Socket socket) throws IOException {
        DataInputStream in = new DataInputStream(socket.getInputStream());

        int length;
        try {
            length = in.readInt();
        } catch (IOException e) {
            return null;
        }

        if (length < 0) {
            throw new IOException("Invalid payload length: " + length);
        }

        byte[] buffer = new byte[length];
        int totalRead = 0;
        while (totalRead < length) {
            int read = in.read(buffer, totalRead, length - totalRead);
            if (read == -1) {
                throw new IOException("Unexpected end of stream");
            }
            totalRead += read;
        }

        return buffer;
    }
}