package io.github.teilabs.meshnet.core.transport.handshake;

public interface HandShakePayloadCodec {
    byte[] serialize(HandShakePayload handShakePayload);

    HandShakePayload parse(byte[] bytes);
}
