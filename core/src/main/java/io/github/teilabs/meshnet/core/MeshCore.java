package io.github.teilabs.meshnet.core;

import io.github.teilabs.meshnet.core.api.DefaultMeshMessageCodec;
import io.github.teilabs.meshnet.core.api.MeshIncomingMessage;
import io.github.teilabs.meshnet.core.api.MeshMessageCodec;
import io.github.teilabs.meshnet.core.api.MeshOutgoingMessage;
import io.github.teilabs.meshnet.core.buffer.FrameBuffer;
import io.github.teilabs.meshnet.core.buffer.FrameBufferEvents;
import io.github.teilabs.meshnet.core.buffer.PersistentFrameBuffer;
import io.github.teilabs.meshnet.core.crypto.BouncyCastleCryptoProvider;
import io.github.teilabs.meshnet.core.crypto.CryptoProvider;
import io.github.teilabs.meshnet.core.crypto.Ed25519KeyPair;
import io.github.teilabs.meshnet.core.frame.BinaryFrameCodec;
import io.github.teilabs.meshnet.core.frame.Frame;
import io.github.teilabs.meshnet.core.frame.FrameCodec;
import io.github.teilabs.meshnet.core.routing.DefaultFrameRouter;
import io.github.teilabs.meshnet.core.routing.FrameRouter;
import io.github.teilabs.meshnet.core.routing.FrameRouterEvents;
import io.github.teilabs.meshnet.core.routing.HashMapTunnelManager;
import io.github.teilabs.meshnet.core.routing.TunnelManager;

public class MeshCore implements CoreInput {
    private final CoreEvents coreEvents;

    private final FrameCodec frameCodec;

    private final CryptoProvider cryptoProvider;

    private Ed25519KeyPair keyPair;

    private final TunnelManager tunnelManager;

    private final MeshMessageCodec meshMessageCodec;

    private final FrameRouter frameRouter;

    private final FrameBuffer frameBuffer;

    public MeshCore(CoreEvents coreEvents) {
        this.coreEvents = coreEvents;

        this.frameCodec = new BinaryFrameCodec();
        this.cryptoProvider = new BouncyCastleCryptoProvider();
        this.keyPair = (this.coreEvents.getKeyPair() != null) ? this.coreEvents.getKeyPair()
                : this.coreEvents.saveKeyPair(this.cryptoProvider.generateKeyPair());
        this.tunnelManager = new HashMapTunnelManager();
        this.meshMessageCodec = new DefaultMeshMessageCodec(this.cryptoProvider, this.frameCodec, this.keyPair,
                this.tunnelManager);
        this.frameBuffer = new PersistentFrameBuffer(new FrameBufferEvents() {

            @Override
            public void writeFile(String path, byte[] data) {
                coreEvents.writeFile(path, data);
            }

            @Override
            public byte[] readFile(String path) {
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

        }, this.frameCodec);
        this.frameRouter = new DefaultFrameRouter(this.keyPair, new FrameRouterEvents() {

            @Override
            public void sendFrame(Frame frame, long to) {
                coreEvents.sendRawFrame(frameCodec.serialize(frame), to);
            }

            @Override
            public void sendFrameToEveryone(Frame frame) {
                coreEvents.sendRawFrameToEveryone(frameCodec.serialize(frame));
            }

            @Override
            public void transferMessageToApp(MeshIncomingMessage message) {
                if (message.getDstAppId() == 0) {
                    // TODO: proccess it by core
                    // If type == 1 add this node to the end of the path
                    return;
                }
                coreEvents.transferMessageToApp(message);
            }

            @Override
            public boolean checkConnectionToNode(long nodeRoutingId) {
                return coreEvents.checkConnectionToNode(nodeRoutingId);
            }

        }, this.meshMessageCodec, this.cryptoProvider, this.frameCodec, this.tunnelManager, this.frameBuffer);
    }

    @Override
    public void onRawFrameRecieved(byte[] rawFrame) {
        Frame frame = frameCodec.parse(rawFrame);
        frameRouter.onFrameRecieved(frame);
    }

    @Override
    public void onAppSendMessage(MeshOutgoingMessage message) {
        Frame frame = meshMessageCodec.generateOutgoingFrame(message);
        frameRouter.sendFrame(frame);
    }
}