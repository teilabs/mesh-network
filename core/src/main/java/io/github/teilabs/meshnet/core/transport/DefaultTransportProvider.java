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
import io.github.teilabs.meshnet.core.util.Logger;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Default implementation of {@link TransportProvider}.
 */
public class DefaultTransportProvider implements TransportProvider {
    private static final String TAG = "DefaultTransportProvider";

    private final FrameCodec frameCodec;

    private final TransportMessageCodec transportMessageCodec;

    private final TransportProviderEvents transportProviderEvents;

    private final Map<Long, CompletableFuture<Boolean>> sentHandshakes = new ConcurrentHashMap<Long, CompletableFuture<Boolean>>();

    private final Ed25519KeyPair keyPair;

    private final HandShakePayloadCodec handShakePayloadCodec;

    private final CryptoProvider cryptoProvider;

    private final AdvertisingPayloadCodec advertisingPayloadCodec;

    private final NodesManager nodesManager;

    private final FrameBuffer frameBuffer;

    private final Config config;

    private final Logger logger;

    public DefaultTransportProvider(FrameCodec frameCodec, TransportMessageCodec transportMessageCodec,
            TransportProviderEvents transportProviderEvents, Ed25519KeyPair keyPair,
            HandShakePayloadCodec handShakePayloadCodec, CryptoProvider cryptoProvider,
            AdvertisingPayloadCodec advertisingPayloadCodec, NodesManager nodesManager, FrameBuffer frameBuffer,
            Config config, Logger logger) {
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
        this.logger = logger;
    }

    @Override
    public void sendFrame(Frame frame, long nodeRoutingId) {
        logger.d(TAG, "Sending frame type " + frame.getType() + " to node " + nodeRoutingId);
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
        logger.d(TAG, "Broadcasting frame type " + frame.getType());
        // Pack frame into the transport message and then send its bytes presentation
        TransportMessage message = new TransportMessage(TransportMessageConstants.VERSION, TransportMessage.TYPE_FRAME,
                keyPair.routingId(),
                0,
                frameCodec.serialize(frame));
        byte[] bytes = transportMessageCodec.serialize(message);
        transportProviderEvents.sendBytesToEveryone(bytes);
    }

    @Override
    public CompletableFuture<Boolean> sendHandshake(long nodeRoutingId) {
        logger.i(TAG, "Sending handshake to node " + nodeRoutingId);
        CompletableFuture<Boolean> future = new CompletableFuture<Boolean>();
        // Set handshake timeout with value from TransportProvider interface, to prevent
        // infinite waiting
        future.completeOnTimeout(false, config.handshakeTimeoutSec(), TimeUnit.SECONDS);
        future.whenComplete((result, throwable) -> {
            sentHandshakes.remove(nodeRoutingId);
            if (throwable != null) {
                logger.e(TAG, "Handshake failed for node " + nodeRoutingId, throwable);
            } else if (Boolean.TRUE.equals(result)) {
                logger.i(TAG, "Handshake completed with node " + nodeRoutingId);
            } else {
                logger.w(TAG, "Handshake timed out for node " + nodeRoutingId);
            }
        });

        // Store handshake to understand in future if received handshake is response or
        // request
        sentHandshakes.put(nodeRoutingId, future);

        sendHandshakePayload(nodeRoutingId);
        return future;
    }

    @Override
    public void startAdvertising(int intervalMs) {
        logger.i(TAG, "Starting advertising with interval " + intervalMs + " ms");
        // Create advertising payload with signed node info and pack it into the
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
        logger.i(TAG, "Stopping advertising");
        transportProviderEvents.stopAdvertising();
    }

    @Override
    public void onBytesReceived(byte[] bytes) {
        logger.d(TAG, "Received transport bytes: " + bytes.length);
        // Parse transport message from bytes to use structured data
        TransportMessage message = transportMessageCodec.parse(bytes);
        logger.d(TAG, "Parsed transport message type " + message.getType() + " from node "
                + message.getSenderRoutingId() + " to " + message.getTargetRoutingId());

        // Check if this node is correct receiver of this message
        if (message.getTargetRoutingId() != keyPair.routingId() && message.getTargetRoutingId() != 0) {
            logger.d(TAG, "Ignoring message for routingId " + message.getTargetRoutingId());
            return;
        }

        switch (message.getType()) {
            case TransportMessage.TYPE_HANDSHAKE: {
                logger.d(TAG, "Processing handshake from node " + message.getSenderRoutingId());
                HandShakePayload handShakePayload = handShakePayloadCodec.parse(message.getPayload());
                // Verify node info signature to check if this is true node info
                if (!cryptoProvider.verify(handShakePayload.getSrcPubKey(), handShakePayload.getSignature(),
                        handShakePayload.getSrcPubKey())) {
                    logger.e(TAG, "Handshake signature verification failed for node " + message.getSenderRoutingId());
                    throw new IllegalArgumentException("Invalid signature. Author prove failed");
                }

                // Check if this is response to our handshake or request for a new handshake
                if (sentHandshakes.containsKey(Ed25519KeyPair.generateRoutingId(handShakePayload.getSrcPubKey()))) {
                    // Add node to nodes manager, because it can be not stored
                    addNode(message.getSenderRoutingId());

                    // Complete stored future with true, because handshake was successful
                    sentHandshakes.get(Ed25519KeyPair.generateRoutingId(handShakePayload.getSrcPubKey()))
                            .complete(true);
                } else {
                    // Add node to nodes manager, because it can be not stored
                    addNode(message.getSenderRoutingId());
                    // Send handshake response to node that requested it
                    logger.d(TAG, "Replying with handshake to node " + message.getSenderRoutingId());
                    sendHandshakePayload(Ed25519KeyPair.generateRoutingId(handShakePayload.getSrcPubKey()));
                }
                break;
            }
            case TransportMessage.TYPE_FRAME: {
                logger.d(TAG, "Processing frame from node " + message.getSenderRoutingId());
                Frame frame = frameCodec.parse(message.getPayload());
                transportProviderEvents.onFrameReceived(frame, message.getSenderRoutingId());
                break;
            }
            case TransportMessage.TYPE_ADVERTISING: {
                logger.d(TAG, "Processing advertising from node " + message.getSenderRoutingId());
                AdvertisingPayload advertisingPayload = advertisingPayloadCodec.parse(message.getPayload());
                // Verify node info signature to check if this is true node info
                if (!cryptoProvider.verify(advertisingPayload.getSrcPubKey(), advertisingPayload.getSignature(),
                        advertisingPayload.getSrcPubKey())) {
                    logger.e(TAG, "Advertising signature verification failed for node "
                            + message.getSenderRoutingId());
                    throw new IllegalArgumentException("Invalid signature. Author prove failed");
                }
                // If verification passed, add node to nodes manager
                addNode(message.getSenderRoutingId());
                // Send handshake to advertising node to give info about our node
                logger.d(TAG, "Sending handshake after advertising from node " + message.getSenderRoutingId());
                sendHandshakePayload(message.getSenderRoutingId());
                break;
            }
            default:
                logger.w(TAG, "Unsupported transport message type: " + message.getType());
                break;
        }
    }

    private void sendHandshakePayload(long nodeRoutingId) {
        logger.d(TAG, "Sending handshake payload to node " + nodeRoutingId);
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
            logger.i(TAG, "Connected node " + nodeRoutingId);

            // Send stored messages to node if it is new
            Frame[] frames = frameBuffer.getAllFrames();
            logger.d(TAG, "Replaying " + frames.length + " buffered frames to node " + nodeRoutingId);
            for (Frame frame : frames) {
                sendFrame(frame, nodeRoutingId);
            }
        } catch (IllegalArgumentException e) {
            logger.d(TAG, "Node already connected: " + nodeRoutingId);
        }
    }
}
