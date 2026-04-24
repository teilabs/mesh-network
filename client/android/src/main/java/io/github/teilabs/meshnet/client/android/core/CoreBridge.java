package io.github.teilabs.meshnet.client.android.core;

import android.content.Context;

import java.io.IOException;

import io.github.teilabs.meshnet.client.android.daemon.DaemonSocketClient;
import io.github.teilabs.meshnet.client.android.util.FileUtils;
import io.github.teilabs.meshnet.client.android.util.Logger;
import io.github.teilabs.meshnet.core.CoreEvents;
import io.github.teilabs.meshnet.core.MeshCore;
import io.github.teilabs.meshnet.core.api.MeshIncomingMessage;
import io.github.teilabs.meshnet.core.config.Config;
import io.github.teilabs.meshnet.core.crypto.Ed25519KeyPair;
import io.github.teilabs.meshnet.core.routing.Tunnel;

public final class CoreBridge implements CoreEvents {
    private static final String TAG = "CoreBridge";

    private final Context context;

    private final Config config;

    private final MeshCore meshCore;

    private final DaemonSocketClient daemonSocketClient;

    private final AndroidKeyStorage androidKeyStorage;

    // private final SdkSocketServer sdkSocketServer;

    public CoreBridge(Context context, Config config) {
        if (context == null)
            throw new IllegalArgumentException("Context cannot be null");
        if (config == null)
            throw new IllegalArgumentException("Config cannot be null");

        this.context = context.getApplicationContext();
        this.config = config;
        this.meshCore = new MeshCore(this, config);
        this.daemonSocketClient = new DaemonSocketClient(meshCore::onBytesReceived);
        this.androidKeyStorage = new AndroidKeyStorage(this.context);
    }

    public void start() {
        Logger.i(TAG, "Starting core bridge");
        // sdkSocketServer.start();
        daemonSocketClient.start();
    }

    public void stop() {
        Logger.i(TAG, "Stopping core bridge");
        // sdkSocketServer.close();
        daemonSocketClient.stop();
    }

    @Override
    public void sendBytesToEveryone(byte[] bytes) {
        try {
            daemonSocketClient.send(bytes);
        } catch (IOException e) {
            Logger.e(TAG, "Failed to send data message to daemon", e);
        }
    }

    @Override
    public void startAdvertising(byte[] bytes, int intervalMs) {
        try {
            daemonSocketClient.startAdvertising(bytes, intervalMs);
        } catch (IOException e) {
            Logger.e(TAG, "Failed to send start advertising message to daemon", e);
        }
    }

    @Override
    public void stopAdvertising() {
        try {
            daemonSocketClient.stopAdvertising();
        } catch (IOException e) {
            Logger.e(TAG, "Failed to send stop advertising message to daemon", e);
        }
    }

    @Override
    public Ed25519KeyPair getKeyPair() {
        return androidKeyStorage.loadKeyPair();
    }

    @Override
    public Ed25519KeyPair saveKeyPair(Ed25519KeyPair keyPair) {
        return androidKeyStorage.saveKeyPair(keyPair);
    }

    @Override
    public void transferMessageToApp(MeshIncomingMessage message) {

    }

    @Override
    public void writeFile(String path, byte[] data) {
        try {
            FileUtils.write(context, path, data);
        } catch (IOException e) {
            Logger.e(TAG, "Failed to write file", e);
        }
    }

    @Override
    public byte[] readFile(String path) throws IOException {
        try {
            return FileUtils.read(context, path);
        } catch (IOException e) {
            Logger.e(TAG, "Failed to read file", e);
            throw new IOException("Failed to read file");
        }
    }

    @Override
    public String[] listFiles(String folderPath) {
        return FileUtils.list(context, folderPath);
    }

    @Override
    public void deleteFile(String path) {
        FileUtils.delete(context, path);
    }

    @Override
    public boolean checkTunnelOpenAccess(Tunnel tunnel) {
        return false;
    }
}
