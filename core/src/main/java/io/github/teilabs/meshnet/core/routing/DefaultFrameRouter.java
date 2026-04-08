package io.github.teilabs.meshnet.core.routing;

import io.github.teilabs.meshnet.core.api.DefaultMeshMessageCodec;
import io.github.teilabs.meshnet.core.api.MeshIncomingMessage;
import io.github.teilabs.meshnet.core.api.MeshMessageCodec;
import io.github.teilabs.meshnet.core.buffer.FrameBuffer;
import io.github.teilabs.meshnet.core.crypto.CryptoProvider;
import io.github.teilabs.meshnet.core.crypto.Ed25519KeyPair;
import io.github.teilabs.meshnet.core.frame.Frame;
import io.github.teilabs.meshnet.core.frame.FrameCodec;

public class DefaultFrameRouter implements FrameRouter {
    private Ed25519KeyPair keyPair;

    private final FrameRouterEvents frameRouterEvents;

    private final MeshMessageCodec meshMessageCodec;

    private final FrameBuffer frameBuffer;

    public DefaultFrameRouter(Ed25519KeyPair keyPair, FrameRouterEvents frameRouterEvents,
            MeshMessageCodec meshMessageCodec, CryptoProvider cryptoProvider, FrameCodec frameCodec,
            TunnelManager tunnelManager, FrameBuffer frameBuffer) {
        this.keyPair = keyPair;
        this.frameRouterEvents = frameRouterEvents;
        this.frameBuffer = frameBuffer;
        this.meshMessageCodec = new DefaultMeshMessageCodec(cryptoProvider, frameCodec, keyPair, tunnelManager);
    }

    @Override
    public void onFrameRecieved(Frame frame) {
        if (frame.getDstRoutingId() == keyPair.routingId()) {
            MeshIncomingMessage message = meshMessageCodec.parseIncomingFrame(frame);
            frameRouterEvents.transferMessageToApp(message);
        } else {
            switch (frame.getType()) {
                case 0: {
                    if (!frameBuffer.containsFrame(frame)) {
                        if (frameRouterEvents.checkConnectionToNode(frame.getDstRoutingId())) {
                            frameRouterEvents.sendFrame(frame, frame.getDstRoutingId());
                            break;
                        }
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
                    path[frame.getPath().length] = keyPair.routingId();

                    frameRouterEvents.sendFrameToEveryone(
                            new Frame(frame.getVersion(), frame.getType(), frame.getTimestamp(), frame.getSrcAppId(),
                                    frame.getDstAppId(), frame.getSrcPubKey(), frame.getDstRoutingId(),
                                    frame.getNonce(), frame.getSignature(), path, frame.getPathPosition(),
                                    frame.getEncryptedData()));
                    break;
                }
                case 2, 3: {
                    if (frame.getPath()[frame.getPathPosition()] == keyPair.routingId()) {
                        frameRouterEvents.sendFrame(new Frame(frame.getVersion(), frame.getType(), frame.getTimestamp(),
                                frame.getSrcAppId(),
                                frame.getDstAppId(), frame.getSrcPubKey(), frame.getDstRoutingId(),
                                frame.getNonce(), frame.getSignature(), frame.getPath(),
                                (short) (frame.getPathPosition() + 1),
                                frame.getEncryptedData()), frame.getPath()[frame.getPathPosition() + 1]);
                    } else {
                        throw new RuntimeException("Can't find routingId in path.");
                    }
                    break;
                }
            }
        }
    }

    @Override
    public void sendFrame(Frame frame) {
        switch (frame.getType()) {
            case 0: {
                if (!frameBuffer.containsFrame(frame)) {
                    if (frameRouterEvents.checkConnectionToNode(frame.getDstRoutingId())) {
                        frameRouterEvents.sendFrame(frame, frame.getDstRoutingId());
                        break;
                    }
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
