package io.github.teilabs.meshnet.core.transport;

import io.github.teilabs.meshnet.core.buffer.FrameBuffer;
import io.github.teilabs.meshnet.core.config.Config;
import io.github.teilabs.meshnet.core.crypto.CryptoProvider;
import io.github.teilabs.meshnet.core.crypto.Ed25519KeyPair;
import io.github.teilabs.meshnet.core.frame.Frame;
import io.github.teilabs.meshnet.core.frame.FrameCodec;
import io.github.teilabs.meshnet.core.transport.advertising.AdvertisingPayload;
import io.github.teilabs.meshnet.core.transport.advertising.AdvertisingPayloadCodec;
import io.github.teilabs.meshnet.core.transport.handshake.HandShakePayload;
import io.github.teilabs.meshnet.core.transport.handshake.HandShakePayloadCodec;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Default implementation of {@link TransportProvider}.
 */
public class DefaultTransportProvider implements TransportProvider {
    private final FrameCodec frameCodec;

    private final TransportMessageCodec transportMessageCodec;

    private final TransportProviderEvents transportProviderEvents;

    private final Map<Long, CompletableFuture<Boolean>> sendedHandshakes = new ConcurrentHashMap<Long, CompletableFuture<Boolean>>();

    private final Ed25519KeyPair keyPair;

    private final HandShakePayloadCodec handShakePayloadCodec;

    private final CryptoProvider cryptoProvider;

    private final AdvertisingPayloadCodec advertisingPayloadCodec;

    private final NodesManager nodesManager;

    private final FrameBuffer frameBuffer;

    private final Config config;

    public DefaultTransportProvider(FrameCodec frameCodec, TransportMessageCodec transportMessageCodec,
            TransportProviderEvents transportProviderEvents, Ed25519KeyPair keyPair,
            HandShakePayloadCodec handShakePayloadCodec, CryptoProvider cryptoProvider,
            AdvertisingPayloadCodec advertisingPayloadCodec, NodesManager nodesManager, FrameBuffer frameBuffer,
            Config config) {
        this.frameCodec = frameCodec;
        this.transportMessageCodec = transportMessageCodec;
        this.transportProviderEvents = transportProviderEvents;
        this.keyPair = keyPair;
        this.handShakePayloadCodec = handShakePayloadCodec;
        this.cryptoProvider = cryptoProvider;
        this.advertisingPayloadCodec = advertisingPayloadCodec;
        this.nodesManager = nodesManager;
        this.frameBuffer = frameBuffer;
        this.config = config;
    }

    @Override
    public void sendFrame(Frame frame, long nodeRoutingId) {
        // Pack frame into the transport message and then send its bytes presentation
        TransportMessage message = new TransportMessage(TransportMessageConstants.VERSION, TransportMessage.TYPE_FRAME,
                keyPair.routingId(),
                nodeRoutingId,
                frameCodec.serialize(frame));
        byte[] bytes = transportMessageCodec.serialize(message);
        transportProviderEvents.sendBytesToEveryone(bytes);
    }

    @Override
    public void sendFrameToEveryone(Frame frame) {
        // Pack frame into the transport message and then send its bytes presentation
        TransportMessage message = new TransportMessage(TransportMessageConstants.VERSION, TransportMessage.TYPE_FRAME,
                keyPair.routingId(),
                0,
                frameCodec.serialize(frame));
        byte[] bytes = transportMessageCodec.serialize(message);
        transportProviderEvents.sendBytesToEveryone(bytes);
    }

    @Override
    public CompletableFuture<Boolean> sendHandhsake(long nodeRoutingId) {
        CompletableFuture<Boolean> future = new CompletableFuture<Boolean>();
        // Set handshake timeout with value from TransportProvider interafce, to prevent
        // infinite waiting
        future.completeOnTimeout(false, config.handshakeTimeoutSec(), TimeUnit.SECONDS);
        future.whenComplete((result, throwable) -> {
            sendedHandshakes.remove(nodeRoutingId);
        });

        // Strore handshake to understand in future if received handshake is response or
        // request
        sendedHandshakes.put(nodeRoutingId, future);

        sendHandhsakePayload(nodeRoutingId);
        return future;
    }

    @Override
    public void startAdvertising(int intervalMs) {
        // Create advertizing payload with signed node info and pack it into the
        // transport message
        AdvertisingPayload advertisingPayload = new AdvertisingPayload(keyPair.publicKey(),
                cryptoProvider.sign(keyPair.publicKey(), keyPair.privateKey()));
        transportProviderEvents
                .startAdvertising(
                        transportMessageCodec.serialize(new TransportMessage(TransportMessageConstants.VERSION,
                                TransportMessage.TYPE_ADVERTISING,
                                keyPair.routingId(),
                                0, advertisingPayloadCodec.serialize(advertisingPayload))),
                        intervalMs);
    }

    @Override
    public void stopAdvertising() {
        transportProviderEvents.stopAdvertising();
    }

    @Override
    public void onBytesReceived(byte[] bytes) {
        // Parse transport message from bytes to use structured data
        TransportMessage message = transportMessageCodec.parse(bytes);

        // Check if this node is correct receiver of this message
        if (message.getTargetRoutingId() != keyPair.routingId() && message.getTargetRoutingId() != 0)
            return;

        switch (message.getType()) {
            case TransportMessage.TYPE_HANDSHAKE: {
                HandShakePayload handShakePayload = handShakePayloadCodec.parse(message.getPayload());
                // Verify node info signature to check if this is true node info
                if (!cryptoProvider.verify(handShakePayload.getSrcPubKey(), handShakePayload.getSignature(),
                        handShakePayload.getSrcPubKey())) {
                    throw new IllegalArgumentException("Invalid signature. Author prove failed");
                }

                // Check if this is respone to our handshake or request for a new handshak
                if (sendedHandshakes.containsKey(Ed25519KeyPair.generateRoutingId(handShakePayload.getSrcPubKey()))) {
                    // Add node to nodes manager, because it can be not stored
                    addNode(message.getSenderRoutingId());

                    // Complete stored future with true, because handshake was successful
                    sendedHandshakes.get(Ed25519KeyPair.generateRoutingId(handShakePayload.getSrcPubKey()))
                            .complete(true);
                } else {
                    // Add node to nodes manager, because it can be not stored
                    addNode(message.getSenderRoutingId());
                    // Send handshake response to node that requested it
                    sendHandhsakePayload(Ed25519KeyPair.generateRoutingId(handShakePayload.getSrcPubKey()));
                }
                break;
            }
            case TransportMessage.TYPE_FRAME: {
                Frame frame = frameCodec.parse(message.getPayload());
                transportProviderEvents.onFrameReceived(frame, message.getSenderRoutingId());
                break;
            }
            case TransportMessage.TYPE_ADVERTISING: {
                AdvertisingPayload advertisingPayload = advertisingPayloadCodec.parse(message.getPayload());
                // Verify node info signature to check if this is true node info
                if (!cryptoProvider.verify(advertisingPayload.getSrcPubKey(), advertisingPayload.getSignature(),
                        advertisingPayload.getSrcPubKey())) {
                    throw new IllegalArgumentException("Invalid signature. Author prove failed");
                }
                // If verification passed, add node to nodes manager
                addNode(message.getSenderRoutingId());
                // Send handshake to advertising node to give info about our node
                sendHandhsakePayload(message.getSenderRoutingId());
                break;
            }
        }
    }

    private void sendHandhsakePayload(long nodeRoutingId) {
        // Create handshake payload with signed node info and pack it into the transport
        // message
        HandShakePayload handShakePayload = new HandShakePayload(keyPair.publicKey(),
                cryptoProvider.sign(keyPair.publicKey(), keyPair.privateKey()));
        transportProviderEvents
                .sendBytesToEveryone(
                        transportMessageCodec.serialize(
                                new TransportMessage(TransportMessageConstants.VERSION, TransportMessage.TYPE_HANDSHAKE,
                                        keyPair.routingId(),
                                        nodeRoutingId, handShakePayloadCodec.serialize(handShakePayload))));
    }

    private void addNode(long nodeRoutingId) {
        try {
            // Store node in nodes manager
            nodesManager.addNode(nodeRoutingId);

            // Send stored messages to node if it is new
            Frame[] frames = frameBuffer.getAllFrames();
            for (Frame frame : frames) {
                sendFrame(frame, nodeRoutingId);
            }
        } catch (IllegalArgumentException e) {
            // Node already exists
        }
    }
}
