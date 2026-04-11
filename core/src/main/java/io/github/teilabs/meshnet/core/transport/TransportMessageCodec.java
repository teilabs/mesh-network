package io.github.teilabs.meshnet.core.transport;

public interface TransportMessageCodec {
    byte[] serialize(TransportMessage message);

    TransportMessage parse(byte[] bytes);
}
