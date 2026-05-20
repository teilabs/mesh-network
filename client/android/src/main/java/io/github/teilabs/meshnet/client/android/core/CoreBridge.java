package io.github.teilabs.meshnet.client.android.core;

import android.content.Context;

import java.io.IOException;

import io.github.teilabs.meshnet.client.android.radio.RadioManager;
import io.github.teilabs.meshnet.client.android.radio.ble.BleManager;
import io.github.teilabs.meshnet.client.android.util.FileUtils;
import io.github.teilabs.meshnet.client.android.util.AndroidLogger;
import io.github.teilabs.meshnet.core.CoreEvents;
import io.github.teilabs.meshnet.core.MeshCore;
import io.github.teilabs.meshnet.core.api.MeshIncomingMessage;
import io.github.teilabs.meshnet.core.config.Config;
import io.github.teilabs.meshnet.core.crypto.Ed25519KeyPair;
import io.github.teilabs.meshnet.core.exception.MeshStorageException;
import io.github.teilabs.meshnet.core.exception.MeshValidationException;
import io.github.teilabs.meshnet.core.routing.Tunnel;
import io.github.teilabs.meshnet.core.util.Logger;

/**
 * Class that connects the core with the client app.
 */
public final class CoreBridge implements CoreEvents, Logger {
    private static final String TAG = "CoreBridge";

    private final Context context;

    private final Config config;

    private final MeshCore meshCore;

    private final AndroidKeyStorage androidKeyStorage;

    // private final SdkSocketServer sdkSocketServer;

    private final RadioManager bleManager, wifiManager;

    public CoreBridge(Context context, Config config) {
        if (context == null)
            throw new MeshValidationException("Context cannot be null");
        if (config == null)
            throw new MeshValidationException("Config cannot be null");

        this.context = context.getApplicationContext();
        this.config = config;
        this.meshCore = new MeshCore(this, config, this);
        this.androidKeyStorage = new AndroidKeyStorage(this.context);

        this.bleManager = new BleManager(meshCore::onBytesReceived);
        this.wifiManager = null;

        AndroidLogger.i(TAG, "Core bridge initialized");
    }

    /**
     * Starts the core bridge and services in it.
     */
    public void start() {
        AndroidLogger.i(TAG, "Starting core bridge");
        // sdkSocketServer.start();
    }

    /**
     * Stops the core bridge and services in it.
     */
    public void stop() {
        AndroidLogger.i(TAG, "Stopping core bridge");
        // sdkSocketServer.close();
        meshCore.shutdown();
    }

    @Override
    public void sendBytesToEveryone(byte[] bytes) {
        AndroidLogger.d(TAG, "Sending " + bytes.length + " bytes to daemon");
        try {
            if (bleManager != null) bleManager.sendRadioData(bytes);
            if (wifiManager != null) wifiManager.sendRadioData(bytes);
        } catch (IOException e) {
            AndroidLogger.e(TAG, "Failed to send data message to daemon", e);
        }
    }

    @Override
    public void startAdvertising(byte[] bytes, int intervalMs) {
        AndroidLogger.i(TAG, "Starting advertising via daemon with interval " + intervalMs + " ms");
        try {
            if (bleManager != null) bleManager.startAdvertising(bytes, intervalMs);
            if (wifiManager != null) wifiManager.startAdvertising(bytes, intervalMs);
        } catch (IOException e) {
            AndroidLogger.e(TAG, "Failed to send start advertising message to daemon", e);
        }
    }

    @Override
    public void stopAdvertising() {
        try {
            if (bleManager != null) bleManager.stopAdvertising();
            if (wifiManager != null) wifiManager.stopAdvertising();
        } catch (IOException e) {
            AndroidLogger.e(TAG, "Failed to send stop advertising message to daemon", e);
        }
    }

    @Override
    public Ed25519KeyPair getKeyPair() {
        Ed25519KeyPair keyPair = androidKeyStorage.loadKeyPair();
        AndroidLogger.d(TAG,
                keyPair != null ? "Loaded key pair from Android storage" : "No key pair in Android storage");
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
    public void writeFile(String path, byte[] data) throws MeshStorageException {
        AndroidLogger.d(TAG, "Writing file: " + path);
        try {
            FileUtils.write(context, path, data);
        } catch (MeshStorageException e) {
            AndroidLogger.e(TAG, "Failed to write file", e);
            throw e;
        }
    }

    @Override
    public byte[] readFile(String path) throws MeshStorageException {
        AndroidLogger.d(TAG, "Reading file: " + path);
        try {
            return FileUtils.read(context, path);
        } catch (MeshStorageException e) {
            AndroidLogger.e(TAG, "Failed to read file", e);
            throw e;
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
