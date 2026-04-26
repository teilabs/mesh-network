package io.github.teilabs.meshnet.core;

import io.github.teilabs.meshnet.core.api.DefaultMeshMessageCodec;
import io.github.teilabs.meshnet.core.api.MeshIncomingMessage;
import io.github.teilabs.meshnet.core.api.MeshMessageCodec;
import io.github.teilabs.meshnet.core.api.MeshOutgoingMessage;
import io.github.teilabs.meshnet.core.buffer.FrameBuffer;
import io.github.teilabs.meshnet.core.buffer.FrameBufferEvents;
import io.github.teilabs.meshnet.core.buffer.PersistentFrameBuffer;
import io.github.teilabs.meshnet.core.config.Config;
import io.github.teilabs.meshnet.core.crypto.BouncyCastleCryptoProvider;
import io.github.teilabs.meshnet.core.crypto.CryptoProvider;
import io.github.teilabs.meshnet.core.crypto.Ed25519KeyPair;
import io.github.teilabs.meshnet.core.exception.MeshStorageException;
import io.github.teilabs.meshnet.core.frame.BinaryFrameCodec;
import io.github.teilabs.meshnet.core.frame.Frame;
import io.github.teilabs.meshnet.core.frame.FrameCodec;
import io.github.teilabs.meshnet.core.routing.DefaultFrameRouter;
import io.github.teilabs.meshnet.core.routing.FrameRouter;
import io.github.teilabs.meshnet.core.routing.FrameRouterEvents;
import io.github.teilabs.meshnet.core.routing.HashMapTunnelManager;
import io.github.teilabs.meshnet.core.routing.Tunnel;
import io.github.teilabs.meshnet.core.routing.TunnelManager;
import io.github.teilabs.meshnet.core.routing.TunnelManagerEvents;
import io.github.teilabs.meshnet.core.transport.BinaryTransportMessageCodec;
import io.github.teilabs.meshnet.core.transport.DefaultTransportProvider;
import io.github.teilabs.meshnet.core.transport.HashSetNodesManager;
import io.github.teilabs.meshnet.core.transport.NodesManager;
import io.github.teilabs.meshnet.core.transport.TransportMessageCodec;
import io.github.teilabs.meshnet.core.transport.TransportProvider;
import io.github.teilabs.meshnet.core.transport.TransportProviderEvents;
import io.github.teilabs.meshnet.core.transport.advertising.AdvertisingPayloadCodec;
import io.github.teilabs.meshnet.core.transport.advertising.BinaryAdvertisingPayloadCodec;
import io.github.teilabs.meshnet.core.transport.handshake.BinaryHandShakePayloadCodec;
import io.github.teilabs.meshnet.core.transport.handshake.HandShakePayloadCodec;
import io.github.teilabs.meshnet.core.util.Logger;

/**
 * Main class that provides communication between the daemon and other classes.
 */
public class MeshCore implements CoreInput {
    private static final String TAG = "MeshCore";

    private final CoreEvents coreEvents;

    private final Config config;

    private final Logger logger;

    private final FrameCodec frameCodec;

    private final CryptoProvider cryptoProvider;

    private final Ed25519KeyPair keyPair;

    private final NodesManager nodesManager;

    private final TransportMessageCodec transportMessageCodec;

    private final HandShakePayloadCodec handShakePayloadCodec;

    private final AdvertisingPayloadCodec advertisingPayloadCodec;

    private final FrameBuffer frameBuffer;

    private final TransportProvider transportProvider;

    private final TunnelManager tunnelManager;

    private final MeshMessageCodec meshMessageCodec;

    private final FrameRouter frameRouter;

    public MeshCore(CoreEvents coreEvents, Config config, Logger logger) {
        this.coreEvents = coreEvents;
        this.config = config;
        this.logger = logger;

        this.frameCodec = new BinaryFrameCodec();
        this.cryptoProvider = new BouncyCastleCryptoProvider();
        // Get key pair from storage if it exists or create and store it otherwise
        this.keyPair = (this.coreEvents.getKeyPair() != null) ? this.coreEvents.getKeyPair()
                : this.coreEvents.saveKeyPair(this.cryptoProvider.generateKeyPair());
        this.nodesManager = new HashSetNodesManager(this.logger);
        this.transportMessageCodec = new BinaryTransportMessageCodec();
        this.handShakePayloadCodec = new BinaryHandShakePayloadCodec();
        this.advertisingPayloadCodec = new BinaryAdvertisingPayloadCodec();
        this.frameBuffer = new PersistentFrameBuffer(new FrameBufferEvents() {

            @Override
            public void writeFile(String path, byte[] data) throws MeshStorageException {
                coreEvents.writeFile(path, data);
            }

            @Override
            public byte[] readFile(String path) throws MeshStorageException {
                return coreEvents.readFile(path);
            }

            @Override
            public String[] listFiles(String folderPath) {
                return coreEvents.listFiles(folderPath);
            }

            @Override
            public void deleteFile(String path) {
                coreEvents.deleteFile(path);
            }
        }, this.frameCodec, this.config, this.logger);
        this.transportProvider = new DefaultTransportProvider(frameCodec, transportMessageCodec,
                new TransportProviderEvents() {
                    @Override
                    public void sendBytesToEveryone(byte[] bytes) {
                        coreEvents.sendBytesToEveryone(bytes);
                    }

                    @Override
                    public void onFrameReceived(Frame frame, long prevNodeRoutingId) {
                        frameRouter.onFrameReceived(frame, prevNodeRoutingId);
                    }

                    @Override
                    public void startAdvertising(byte[] bytes, int intervalMs) {
                        coreEvents.startAdvertising(bytes, intervalMs);
                    }

                    @Override
                    public void stopAdvertising() {
                        coreEvents.stopAdvertising();
                    }
                }, this.keyPair, this.handShakePayloadCodec, this.cryptoProvider, this.advertisingPayloadCodec,
                this.nodesManager, this.frameBuffer, this.config, this.logger);
        this.tunnelManager = new HashMapTunnelManager(new TunnelManagerEvents() {
            @Override
            public boolean checkTunnelOpenAccess(Tunnel tunnel) {
                return coreEvents.checkTunnelOpenAccess(tunnel);
            }
        }, this.keyPair, this.config, this.logger);
        this.meshMessageCodec = new DefaultMeshMessageCodec(this.cryptoProvider, this.frameCodec, this.keyPair);
        this.frameRouter = new DefaultFrameRouter(this.keyPair, new FrameRouterEvents() {
            @Override
            public void transferMessageToApp(MeshIncomingMessage message) {
                if (message.getDstAppId() == 0) {
                    // TODO: process it by core
                    return;
                }
                coreEvents.transferMessageToApp(message);
            }
        }, this.meshMessageCodec, this.frameBuffer, this.nodesManager, this.transportProvider, this.tunnelManager,
                this.config, this.logger);

        logger.i(TAG, "Initialized with routingId: " + keyPair.routingId());
    }

    @Override
    public void onBytesReceived(byte[] bytes) {
        logger.d(TAG, "Received " + bytes.length + " bytes");
        transportProvider.onBytesReceived(bytes);
    }

    @Override
    public void onAppSendMessage(MeshOutgoingMessage message) {
        logger.d(TAG, "Send message " + message.getType() + " from app " + message.getSrcAppId());
        Frame frame = meshMessageCodec.generateOutgoingFrame(message);
        frameRouter.sendFrame(frame);
    }
}
