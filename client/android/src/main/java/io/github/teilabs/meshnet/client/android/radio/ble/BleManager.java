package io.github.teilabs.meshnet.client.android.radio.ble;

import io.github.teilabs.meshnet.client.android.radio.RadioCallback;
import io.github.teilabs.meshnet.client.android.radio.RadioManager;

public class BleManager implements RadioManager {
    private final RadioCallback callback;

    public BleManager(RadioCallback callback) {
        this.callback = callback;
    }

    @Override
    public void sendRadioData(byte[] data) {

    }

    @Override
    public void startAdvertising(byte[] data, int intervalMs) {

    }

    @Override
    public void stopAdvertising() {

    }
}
