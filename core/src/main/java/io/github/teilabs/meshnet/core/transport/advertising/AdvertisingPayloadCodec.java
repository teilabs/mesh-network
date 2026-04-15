package io.github.teilabs.meshnet.core.transport.advertising;

/**
 * Interface for parsing and serializing {@link AdvertisingPayloadCodec}.
 */
public interface AdvertisingPayloadCodec {
    /**
     * Serializes {@link AdvertisingPayload} to bytes.
     * 
     * @param message AdvertisingPayload to serialize.
     * @return Serialized AdvertisingPayload bytes.
     */
    byte[] serialize(AdvertisingPayload advertisingPayload);

    /**
     * Parses bytes to {@link AdvertisingPayload}.
     * 
     * @param bytes AdvertisingPayload bytes.
     * @return Parsed AdvertisingPayload.
     */
    AdvertisingPayload parse(byte[] bytes);
}
