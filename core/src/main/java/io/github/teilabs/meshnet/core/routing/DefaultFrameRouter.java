package io.github.teilabs.meshnet.core.routing;

import io.github.teilabs.meshnet.core.api.DefaultMeshMessageCodec;
import io.github.teilabs.meshnet.core.api.MeshIncomingMessage;
import io.github.teilabs.meshnet.core.api.MeshMessageCodec;
import io.github.teilabs.meshnet.core.buffer.FrameBuffer;
import io.github.teilabs.meshnet.core.crypto.CryptoProvider;
import io.github.teilabs.meshnet.core.crypto.Ed25519KeyPair;
import io.github.teilabs.meshnet.core.frame.Frame;
import io.github.teilabs.meshnet.core.frame.FrameCodec;
import io.github.teilabs.meshnet.core.transport.NodesManager;
import io.github.teilabs.meshnet.core.transport.TransportProvider;

public class DefaultFrameRouter implements FrameRouter {
    private Ed25519KeyPair keyPair;

    private final FrameRouterEvents frameRouterEvents;

    private final MeshMessageCodec meshMessageCodec;

    private final FrameBuffer frameBuffer;

    private final TransportProvider  transportProvider;

    private final NodesManager nodesManager;

    public DefaultFrameRouter(Ed25519KeyPair keyPair, FrameRouterEvents frameRouterEvents,
            MeshMessageCodec meshMessageCodec, CryptoProvider cryptoProvider, FrameCodec frameCodec,
            TunnelManager tunnelManager, FrameBuffer frameBuffer, NodesManager nodesManager, TransportProvider transportProvider) {
        this.keyPair = keyPair;
        this.frameRouterEvents = frameRouterEvents;
        this.frameBuffer = frameBuffer;
        this.meshMessageCodec = new DefaultMeshMessageCodec(cryptoProvider, frameCodec, keyPair, tunnelManager);
        this.transportProvider = transportProvider;
        this.nodesManager = nodesManager;
    }

    @Override
    public void onFrameRecieved(Frame frame) {
        if (frame.getDstRoutingId() == keyPair.routingId()) {
            MeshIncomingMessage message = meshMessageCodec.parseIncomingFrame(frame);
            frameRouterEvents.transferMessageToApp(message);
        } else {
            switch (frame.getType()) {
                case 0: {
                    // Checking that we aren't already distributing this frame
                    if (!frameBuffer.containsFrame(frame)) {
                        // If we have connection to destination node we should immediatle send frame to
                        // it without storing and redistributing
                        if (nodesManager.checkConnectionToNode(frame.getDstRoutingId())) {
                            transportProvider.sendFrame(frame, frame.getDstRoutingId());
                            break;
                        }
                        frameBuffer.addFrame(frame);
                        transportProvider.sendFrameToEveryone(frame);
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

                    transportProvider.sendFrameToEveryone(
                            new Frame(frame.getVersion(), frame.getType(), frame.getTimestamp(), frame.getSrcAppId(),
                                    frame.getDstAppId(), frame.getSrcPubKey(), frame.getDstRoutingId(),
                                    frame.getNonce(), frame.getSignature(), path, frame.getPathPosition(),
                                    frame.getEncryptedData()));
                    break;
                }
                case 2, 3: {
                    // Chacking that pathPosition is correct
                    // If it isn't it means that we aren't correct redistributor for this frame and
                    // we can just skip it
                    if (frame.getPath()[frame.getPathPosition()] == keyPair.routingId()) {
                        transportProvider.sendFrame(new Frame(frame.getVersion(), frame.getType(), frame.getTimestamp(),
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
                // Checking that we aren't already distributing this frame
                if (!frameBuffer.containsFrame(frame)) {
                    // If we have connection to destination node we should immediatle send frame to
                    // it without storing and redistributing
                    if (nodesManager.checkConnectionToNode(frame.getDstRoutingId())) {
                        transportProvider.sendFrame(frame, frame.getDstRoutingId());
                        break;
                    }
                    frameBuffer.addFrame(frame);
                    transportProvider.sendFrameToEveryone(frame);
                }
                break;
            }
            case 1: {
                transportProvider.sendFrameToEveryone(frame);
                break;
            }
            case 2, 3: {
                transportProvider.sendFrame(frame, frame.getPath()[1]);
                break;
            }
        }
    }
}
