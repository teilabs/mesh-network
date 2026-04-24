package io.github.teilabs.meshnet.client.android;

import android.content.Context;

import io.github.teilabs.meshnet.client.android.core.CoreBridge;
import io.github.teilabs.meshnet.core.config.Config;

public final class ClientEngine {
    private final CoreBridge coreBridge;

    public ClientEngine(Context context, Config config) {
        this.coreBridge = new CoreBridge(context.getApplicationContext(), config);
    }

    public void start() {
        coreBridge.start();
    }

    public void stop() {
        coreBridge.stop();
    }
}
