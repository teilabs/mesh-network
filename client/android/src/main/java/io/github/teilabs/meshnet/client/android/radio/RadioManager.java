package io.github.teilabs.meshnet.client.android.radio;

import java.io.IOException;

public interface RadioManager {
    void sendRadioData(byte[] data) throws IOException;

    void startAdvertising(byte[] data, int intervalMs) throws IOException;

    void stopAdvertising() throws IOException;
}
