package io.github.teilabs.meshnet.client.android;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

import io.github.teilabs.meshnet.core.config.Config;

public final class MeshClientService extends Service {
    @Nullable
    private ClientEngine clientEngine;

    @Override
    public void onCreate() {
        super.onCreate();
        // TODO: parse config from files
        Config config = new Config() {
            @Override
            public int handshakeTimeoutSec() {
                return 10;
            }

            @Override
            public int maxTunnelsCount() {
                return 100;
            }

            @Override
            public TransitMode transitMode() {
                return TransitMode.STORE;
            }

            @Override
            public TunnelMode tunnelMode() {
                return TunnelMode.RELAY;
            }

            @Override
            public int maxStoredFrames() {
                return 1000;
            }

            @Override
            public String storedFramesFolderPath() {
                return "";
            }
        };
        clientEngine = new ClientEngine(getApplicationContext(), config);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (clientEngine != null) {
            clientEngine.start();
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (clientEngine != null) {
            clientEngine.stop();
            clientEngine = null;
        }
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
