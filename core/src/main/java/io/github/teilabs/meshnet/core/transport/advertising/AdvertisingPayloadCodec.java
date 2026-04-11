package io.github.teilabs.meshnet.core.transport.advertising;

public interface AdvertisingPayloadCodec {
    byte[] serialize(AdvertisingPayload advertisingPayload);

    AdvertisingPayload parse(byte[] bytes);
}
