package io.github.teilabs.meshnet.client.android.radio;

@FunctionalInterface
public interface RadioCallback {
    void onRadioDataReceived(byte[] data);
}
