package io.github.teilabs.meshnet.client.android.core;

import android.content.Context;

import java.io.IOException;

import io.github.teilabs.meshnet.client.android.daemon.DaemonSocketClient;
import io.github.teilabs.meshnet.client.android.util.FileUtils;
import io.github.teilabs.meshnet.client.android.util.AndroidLogger;
import io.github.teilabs.meshnet.core.CoreEvents;
import io.github.teilabs.meshnet.core.MeshCore;
import io.github.teilabs.meshnet.core.api.MeshIncomingMessage;
import io.github.teilabs.meshnet.core.config.Config;
import io.github.teilabs.meshnet.core.crypto.Ed25519KeyPair;
import io.github.teilabs.meshnet.core.routing.Tunnel;
import io.github.teilabs.meshnet.core.util.Logger;

public final class CoreBridge implements CoreEvents, Logger {
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
        this.meshCore = new MeshCore(this, config, this);
        this.daemonSocketClient = new DaemonSocketClient(meshCore::onBytesReceived);
        this.androidKeyStorage = new AndroidKeyStorage(this.context);

        AndroidLogger.i(TAG, "Core bridge initialized");
    }

    public void start() {
        AndroidLogger.i(TAG, "Starting core bridge");
        // sdkSocketServer.start();
        daemonSocketClient.start();
    }

    public void stop() {
        AndroidLogger.i(TAG, "Stopping core bridge");
        // sdkSocketServer.close();
        daemonSocketClient.stop();
    }

    @Override
    public void sendBytesToEveryone(byte[] bytes) {
        AndroidLogger.d(TAG, "Sending " + bytes.length + " bytes to daemon");
        try {
            daemonSocketClient.send(bytes);
        } catch (IOException e) {
            AndroidLogger.e(TAG, "Failed to send data message to daemon", e);
        }
    }

    @Override
    public void startAdvertising(byte[] bytes, int intervalMs) {
        AndroidLogger.i(TAG, "Starting advertising via daemon with interval " + intervalMs + " ms");
        try {
            daemonSocketClient.startAdvertising(bytes, intervalMs);
        } catch (IOException e) {
            AndroidLogger.e(TAG, "Failed to send start advertising message to daemon", e);
        }
    }

    @Override
    public void stopAdvertising() {
        try {
            daemonSocketClient.stopAdvertising();
        } catch (IOException e) {
            AndroidLogger.e(TAG, "Failed to send stop advertising message to daemon", e);
        }
    }

    @Override
    public Ed25519KeyPair getKeyPair() {
        Ed25519KeyPair keyPair = androidKeyStorage.loadKeyPair();
        AndroidLogger.d(TAG, keyPair != null ? "Loaded key pair from Android storage" : "No key pair in Android storage");
        return keyPair;
    }

    @Override
    public Ed25519KeyPair saveKeyPair(Ed25519KeyPair keyPair) {
        AndroidLogger.i(TAG, "Saving key pair to Android storage");
        return androidKeyStorage.saveKeyPair(keyPair);
    }

    @Override
    public void transferMessageToApp(MeshIncomingMessage message) {
        AndroidLogger.d(TAG,
                "Received message for app " + message.getDstAppId() + " from app " + message.getSrcAppId());
        // TODO: transfer message
    }

    @Override
    public void writeFile(String path, byte[] data) {
        AndroidLogger.d(TAG, "Writing file: " + path);
        try {
            FileUtils.write(context, path, data);
        } catch (IOException e) {
            AndroidLogger.e(TAG, "Failed to write file", e);
        }
    }

    @Override
    public byte[] readFile(String path) throws IOException {
        AndroidLogger.d(TAG, "Reading file: " + path);
        try {
            return FileUtils.read(context, path);
        } catch (IOException e) {
            AndroidLogger.e(TAG, "Failed to read file", e);
            throw new IOException("Failed to read file");
        }
    }

    @Override
    public String[] listFiles(String folderPath) {
        String[] files = FileUtils.list(context, folderPath);
        AndroidLogger.d(TAG, "Listed " + files.length + " files in " + folderPath);
        return files;
    }

    @Override
    public void deleteFile(String path) {
        AndroidLogger.d(TAG, "Deleting file: " + path);
        FileUtils.delete(context, path);
    }

    @Override
    public boolean checkTunnelOpenAccess(Tunnel tunnel) {
        // TODO: check access
        AndroidLogger.w(TAG, "Tunnel open request denied by default for tunnel " + tunnel.hashCode());
        return false;
    }

    @Override
    public void d(String tag, String message) {
        AndroidLogger.d(tag, message);
    }

    @Override
    public void i(String tag, String message) {
        AndroidLogger.i(tag, message);
    }

    @Override
    public void w(String tag, String message) {
        AndroidLogger.w(tag, message);
    }

    @Override
    public void e(String tag, String message, Throwable t) {
        AndroidLogger.e(tag, message, t);
    }
}
