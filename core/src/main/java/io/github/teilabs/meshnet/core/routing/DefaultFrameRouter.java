package io.github.teilabs.meshnet.core.routing;

import io.github.teilabs.meshnet.core.api.DefaultMeshMessageCodec;
import io.github.teilabs.meshnet.core.api.MeshIncomingMessage;
import io.github.teilabs.meshnet.core.api.MeshMessageCodec;
import io.github.teilabs.meshnet.core.buffer.FrameBuffer;
import io.github.teilabs.meshnet.core.crypto.CryptoProvider;
import io.github.teilabs.meshnet.core.crypto.Ed25519KeyPair;
import io.github.teilabs.meshnet.core.frame.Frame;
import io.github.teilabs.meshnet.core.frame.FrameCodec;
import java.nio.ByteBuffer;

public class DefaultFrameRouter implements FrameRouter {
    private Ed25519KeyPair keyPair;

    private long routingId;

    private final FrameRouterEvents frameRouterEvents;

    private final MeshMessageCodec meshMessageCodec;

    private final FrameBuffer frameBuffer;

    public DefaultFrameRouter(Ed25519KeyPair keyPair, FrameRouterEvents frameRouterEvents,
            MeshMessageCodec meshMessageCodec, CryptoProvider cryptoProvider, FrameCodec frameCodec,
            TunnelManager tunnelManager, FrameBuffer frameBuffer) {
        this.keyPair = keyPair;
        this.routingId = ByteBuffer.wrap(keyPair.publicKey(), 0, 8).getLong();
        this.frameRouterEvents = frameRouterEvents;
        this.frameBuffer = frameBuffer;
        this.meshMessageCodec = new DefaultMeshMessageCodec(cryptoProvider, frameCodec, keyPair, tunnelManager);
    }

    @Override
    public void onFrameRecieved(Frame frame) {
        if (frame.getDstRoutingId() == ByteBuffer.wrap(keyPair.publicKey(), 0, 8).getLong()) {
            MeshIncomingMessage message = meshMessageCodec.parseIncomingFrame(frame);
            frameRouterEvents.transferMessageToApp(message);
        } else {
            switch (frame.getType()) {
                case 0: {
                    if (frameBuffer.containsFrame(frame)) {
                        // TODO: if destination peer connected to us, send frame to destination peer immediatly without storing
                        frameBuffer.addFrame(frame);
                        frameRouterEvents.sendFrameToEveryone(frame);
                    }
                    break;
                }
                case 1: {
                    // Updating frame path by adding this node routing id
                    long[] path = new long[frame.getPath().length + 1];
                    for (int i = 0; i < frame.getPath().length; i++) {
                        path[i] = frame.getPath()[i];
                    }
                    path[frame.getPath().length] = routingId;

                    frameRouterEvents.sendFrameToEveryone(
                            new Frame(frame.getVersion(), frame.getType(), frame.getTimestamp(), frame.getSrcAppId(),
                                    frame.getDstAppId(), frame.getSrcPubKey(), frame.getDstRoutingId(),
                                    frame.getNonce(), frame.getSignature(), path, frame.getEncryptedData()));
                    break;
                }
                case 2, 3: {
                    Long nextRoutingId = null;
                    // Finding first occurency of this node routing id in the path to get the next
                    // receiver
                    for (int i = 0; i < frame.getPath().length; i++) {
                        if (frame.getPath()[i] == routingId) {
                            nextRoutingId = frame.getPath()[i + 1];
                            break;
                        }
                    }
                    if (nextRoutingId == null) {
                        throw new RuntimeException("Can't find routingId in path.");
                    }
                    frameRouterEvents.sendFrame(frame, nextRoutingId);
                    break;
                }
            }
        }
    }

    @Override
    public void sendFrame(Frame frame) {
        switch (frame.getType()) {
            case 0: {
                if (frameBuffer.containsFrame(frame)) {
                    // TODO: if destination peer connected to us, send frame to destination peer immediatly without storing
                    frameBuffer.addFrame(frame);
                    frameRouterEvents.sendFrameToEveryone(frame);
                }
                break;
            }
            case 1: {
                frameRouterEvents.sendFrameToEveryone(frame);
                break;
            }
            case 2, 3: {
                frameRouterEvents.sendFrame(frame, frame.getPath()[1]);
                break;
            }
        }
    }
}
