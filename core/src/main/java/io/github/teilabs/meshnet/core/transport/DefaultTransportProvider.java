package io.github.teilabs.meshnet.core.transport;

import io.github.teilabs.meshnet.core.crypto.CryptoProvider;
import io.github.teilabs.meshnet.core.crypto.Ed25519KeyPair;
import io.github.teilabs.meshnet.core.frame.Frame;
import io.github.teilabs.meshnet.core.frame.FrameCodec;
import io.github.teilabs.meshnet.core.transport.advertising.AdvertisingPayload;
import io.github.teilabs.meshnet.core.transport.advertising.AdvertisingPayloadCodec;
import io.github.teilabs.meshnet.core.transport.handshake.HandShakePayload;
import io.github.teilabs.meshnet.core.transport.handshake.HandShakePayloadCodec;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class DefaultTransportProvider implements TransportProvider {
    private static final int HANDSHAKE_TIMEOUT_SEC = 10;

    private static final int ADVERTISING_PERIOD_MS = 1000;

    private final FrameCodec frameCodec;

    private final TransportMessageCodec transportMessageCodec;

    private final TransportProviderEvents transportProviderEvents;

    private final Map<Long, CompletableFuture<Boolean>> sendedHandshakes = new ConcurrentHashMap<>();

    private final Ed25519KeyPair keyPair;

    private final HandShakePayloadCodec handShakePayloadCodec;

    private final CryptoProvider cryptoProvider;

    private final AdvertisingPayloadCodec advertisingPayloadCodec;

    public DefaultTransportProvider(FrameCodec frameCodec, TransportMessageCodec transportMessageCodec,
            TransportProviderEvents transportProviderEvents, Ed25519KeyPair keyPair,
            HandShakePayloadCodec handShakePayloadCodec, CryptoProvider cryptoProvider,
            AdvertisingPayloadCodec advertisingPayloadCodec) {
        this.frameCodec = frameCodec;
        this.transportMessageCodec = transportMessageCodec;
        this.transportProviderEvents = transportProviderEvents;
        this.keyPair = keyPair;
        this.handShakePayloadCodec = handShakePayloadCodec;
        this.cryptoProvider = cryptoProvider;
        this.advertisingPayloadCodec = advertisingPayloadCodec;
    }

    @Override
    public void sendFrame(Frame frame, long nodeRoutingId) {
        TransportMessage message = new TransportMessage(TransportMessageConstants.VERSION, TransportMessage.TYPE_FRAME, nodeRoutingId,
                frameCodec.serialize(frame));
        byte[] bytes = transportMessageCodec.serialize(message);
        transportProviderEvents.sendBytes(bytes, nodeRoutingId);
    }

    @Override
    public void sendFrameToEveryone(Frame frame) {
        TransportMessage message = new TransportMessage(TransportMessageConstants.VERSION, TransportMessage.TYPE_FRAME, 0,
                frameCodec.serialize(frame));
        byte[] bytes = transportMessageCodec.serialize(message);
        transportProviderEvents.sendBytesToEveryone(bytes);
    }

    @Override
    public CompletableFuture<Boolean> sendHandhsake(long nodeRoutingId) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        future.completeOnTimeout(false, HANDSHAKE_TIMEOUT_SEC, TimeUnit.SECONDS);
        future.whenComplete((result, throwable) -> {
            sendedHandshakes.remove(nodeRoutingId);
        });

        sendedHandshakes.put(nodeRoutingId, future);
        sendHandhsakePayload(nodeRoutingId);

        return future;
    }

    @Override
    public void startAdvertising(int intervalMs) {
        AdvertisingPayload advertisingPayload = new AdvertisingPayload(keyPair.publicKey(),
                cryptoProvider.sign(keyPair.publicKey(), keyPair.privateKey()));
        transportProviderEvents
                .startAdvertising(
                        transportMessageCodec.serialize(new TransportMessage(TransportMessageConstants.VERSION, TransportMessage.TYPE_ADVERTISING,
                                0, advertisingPayloadCodec.serialize(advertisingPayload))),
                        ADVERTISING_PERIOD_MS);
    }

    @Override
    public void stopAdvertising() {
        transportProviderEvents.stopAdvertising();
    }

    @Override
    public void onBytesRecieved(byte[] bytes) {
        TransportMessage message = transportMessageCodec.parse(bytes);
        if (message.getTargetRoutingId() != keyPair.routingId() && message.getTargetRoutingId() != 0)
            return;

        switch (message.getType()) {
            case TransportMessage.TYPE_HANDSHAKE: {
                HandShakePayload handShakePayload = handShakePayloadCodec.parse(message.getPayload());
                if (!cryptoProvider.verify(handShakePayload.getSrcPubKey(), handShakePayload.getSignature(),
                        handShakePayload.getSrcPubKey())) {
                    throw new IllegalArgumentException("Invalid signature. Author prove failed");
                }

                if (sendedHandshakes.containsKey(ByteBuffer.wrap(handShakePayload.getSrcPubKey(), 0, 8).getLong())) {
                    sendedHandshakes.get(ByteBuffer.wrap(handShakePayload.getSrcPubKey(), 0, 8).getLong())
                            .complete(true);
                } else {
                    sendHandhsakePayload(ByteBuffer.wrap(handShakePayload.getSrcPubKey(), 0, 8).getLong());
                }
                break;
            }
            case TransportMessage.TYPE_FRAME: {
                Frame frame = frameCodec.parse(message.getPayload());
                transportProviderEvents.onFrameRecieved(frame);
                break;
            }
            case TransportMessage.TYPE_ADVERTISING: {
                // TODO: save node in NodesManager
                break;
            }
        }
    }

    private void sendHandhsakePayload(long nodeRoutingId) {
        HandShakePayload handShakePayload = new HandShakePayload(keyPair.publicKey(),
                cryptoProvider.sign(keyPair.publicKey(), keyPair.privateKey()));
        transportProviderEvents
                .sendBytes(transportMessageCodec.serialize(new TransportMessage(TransportMessageConstants.VERSION, TransportMessage.TYPE_HANDSHAKE,
                        nodeRoutingId, handShakePayloadCodec.serialize(handShakePayload))), nodeRoutingId);
    }
}
