package io.github.teilabs.meshnet.core.routing;

import io.github.teilabs.meshnet.core.api.MeshIncomingMessage;
import io.github.teilabs.meshnet.core.api.MeshMessageCodec;
import io.github.teilabs.meshnet.core.api.MeshOutgoingMessage;
import io.github.teilabs.meshnet.core.buffer.FrameBuffer;
import io.github.teilabs.meshnet.core.crypto.CryptoProvider;
import io.github.teilabs.meshnet.core.crypto.Ed25519KeyPair;
import io.github.teilabs.meshnet.core.frame.Frame;
import io.github.teilabs.meshnet.core.frame.FrameCodec;
import io.github.teilabs.meshnet.core.transport.NodesManager;
import io.github.teilabs.meshnet.core.transport.TransportProvider;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import kotlin.Pair;

public class DefaultFrameRouter implements FrameRouter {
    private Ed25519KeyPair keyPair;

    private final FrameRouterEvents frameRouterEvents;

    private final MeshMessageCodec meshMessageCodec;

    private final FrameBuffer frameBuffer;

    private final TransportProvider transportProvider;

    private final NodesManager nodesManager;

    private final TunnelManager tunnelManager;

    public DefaultFrameRouter(Ed25519KeyPair keyPair, FrameRouterEvents frameRouterEvents,
            MeshMessageCodec meshMessageCodec, CryptoProvider cryptoProvider, FrameCodec frameCodec,
            TunnelManager tunnelManager, FrameBuffer frameBuffer, NodesManager nodesManager,
            TransportProvider transportProvider, TunnelManager tunnelManager2) {
        this.keyPair = keyPair;
        this.frameRouterEvents = frameRouterEvents;
        this.frameBuffer = frameBuffer;
        this.meshMessageCodec = meshMessageCodec;
        this.transportProvider = transportProvider;
        this.nodesManager = nodesManager;
        this.tunnelManager = tunnelManager2;
    }

    @Override
    public void onFrameReceived(Frame frame, long prevNodeRoutingId) {
        switch (frame.getType()) {
            case 0: {
                if (frame.getDstRoutingId() == keyPair.routingId()) {
                    MeshIncomingMessage message = meshMessageCodec.parseIncomingFrame(frame);
                    frameRouterEvents.transferMessageToApp(message);
                    break;
                }

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
                if (frame.getDstRoutingId() == keyPair.routingId()) {
                    long srcRoutingId = Ed25519KeyPair.generateRoutingId(frame.getSrcPubKey());
                    long dstRoutingId = frame.getDstRoutingId();

                    short endpoint1AppId = frame.getSrcAppId();
                    short endpoint2AppId = frame.getDstAppId();
                    if (srcRoutingId >= dstRoutingId) {
                        endpoint1AppId = frame.getDstAppId();
                        endpoint2AppId = frame.getSrcAppId();
                    }

                    Set<Pair<Short, Short>> appIds = Collections.synchronizedSet(new HashSet<>());
                    appIds.add(new Pair<Short, Short>(endpoint1AppId, endpoint2AppId));

                    Tunnel tunnel = new Tunnel(Math.min(srcRoutingId, dstRoutingId),
                            Math.max(srcRoutingId, dstRoutingId), prevNodeRoutingId,
                            prevNodeRoutingId, appIds);
                    try {
                        tunnelManager.addTunnel(tunnel);
                        if (tunnelManager.containsPendingTunnel(Tunnel.generateTunnelId(srcRoutingId, dstRoutingId),
                                endpoint1AppId, endpoint2AppId)) {
                            tunnelManager.removePendingTunnel(tunnel);
                        } else {
                            MeshOutgoingMessage message = new MeshOutgoingMessage(MeshOutgoingMessage.TYPE_OPEN_TUNNEL,
                                    frame.getDstAppId(), frame.getSrcAppId(), frame.getSrcPubKey(), new byte[0]);
                            transportProvider.sendFrame(meshMessageCodec.generateOutgoingFrame(message),
                                    prevNodeRoutingId);
                        }
                    } catch (RuntimeException e) {
                        if (tunnelManager.containsPendingTunnel(Tunnel.generateTunnelId(srcRoutingId, dstRoutingId),
                                endpoint1AppId, endpoint2AppId)) {
                            tunnelManager.removePendingTunnel(tunnel);
                        }

                        MeshOutgoingMessage message = new MeshOutgoingMessage(MeshOutgoingMessage.TYPE_CLOSE_TUNNEL,
                                frame.getDstAppId(), frame.getSrcAppId(), frame.getSrcPubKey(), new byte[0]);
                        transportProvider.sendFrame(meshMessageCodec.generateOutgoingFrame(message),
                                prevNodeRoutingId);
                    }
                } else {
                    long srcRoutingId = Ed25519KeyPair.generateRoutingId(frame.getSrcPubKey());
                    long dstRoutingId = frame.getDstRoutingId();

                    short endpoint1AppId = frame.getSrcAppId();
                    short endpoint2AppId = frame.getDstAppId();
                    if (srcRoutingId >= dstRoutingId) {
                        endpoint1AppId = frame.getDstAppId();
                        endpoint2AppId = frame.getSrcAppId();
                    }

                    long tunnelId = Tunnel.generateTunnelId(srcRoutingId, dstRoutingId);
                    if (tunnelManager.containsTunnel(tunnelId, endpoint1AppId, endpoint2AppId)) {
                        Tunnel tunnel = tunnelManager.getTunnel(tunnelId);
                        tunnelManager.removeTunnel(tunnel);

                        Set<Pair<Short, Short>> appIds = tunnel.getAppIds();
                        appIds.add(new Pair<Short, Short>(endpoint1AppId, endpoint2AppId));

                        tunnelManager.addTunnel(new Tunnel(Math.min(srcRoutingId, dstRoutingId),
                                Math.max(srcRoutingId, dstRoutingId),
                                tunnel.getPrevRoutingId(), prevNodeRoutingId, appIds));

                        transportProvider.sendFrame(frame, tunnel.getPrevRoutingId());
                    } else {
                        Set<Pair<Short, Short>> appIds = Collections.synchronizedSet(new HashSet<>());
                        appIds.add(new Pair<Short, Short>(endpoint1AppId, endpoint2AppId));

                        tunnelManager.addTunnel(new Tunnel(Math.min(srcRoutingId, dstRoutingId),
                                Math.max(srcRoutingId, dstRoutingId),
                                prevNodeRoutingId, 0, appIds));

                        transportProvider.sendFrameToEveryone(frame);
                    }
                }

                break;
            }
            case 2: {
                if (frame.getDstRoutingId() == keyPair.routingId()) {
                    // TODO: validate message that it is truly belongs to this tunnel and have
                    // access to use it
                    frameRouterEvents.transferMessageToApp(meshMessageCodec.parseIncomingFrame(frame));
                    // TODO: maybe send ack
                } else {
                    Tunnel tunnel = tunnelManager.getTunnel(frame.getTunnelId());
                    // TODO: validate message that it is truly belongs to this tunnel and have
                    // access to use it

                    long nextRoutingId = tunnel.getNextRoutingId() != prevNodeRoutingId ? tunnel.getNextRoutingId()
                            : tunnel.getPrevRoutingId();
                    transportProvider.sendFrame(frame, nextRoutingId);
                }
                break;
            }
            case 3: {
                if (frame.getDstRoutingId() == keyPair.routingId()) {
                    Tunnel tunnel = tunnelManager.getTunnel(frame.getTunnelId());
                    // TODO: validate message that it is truly belongs to this tunnel and have
                    // access to use it

                    long srcRoutingId = Ed25519KeyPair.generateRoutingId(frame.getSrcPubKey());
                    long dstRoutingId = frame.getDstRoutingId();

                    short endpoint1AppId = frame.getSrcAppId();
                    short endpoint2AppId = frame.getDstAppId();
                    if (srcRoutingId >= dstRoutingId) {
                        endpoint1AppId = frame.getDstAppId();
                        endpoint2AppId = frame.getSrcAppId();
                    }

                    Set<Pair<Short, Short>> appIds = Collections.synchronizedSet(new HashSet<>());
                    appIds.add(new Pair<Short, Short>(endpoint1AppId, endpoint2AppId));

                    tunnelManager
                            .removeTunnel(new Tunnel(tunnel.getEndpoint1RoutingId(), tunnel.getEndpoint2RoutingId(),
                                    tunnel.getPrevRoutingId(), tunnel.getNextRoutingId(), appIds));

                    // TODO: maybe notify app
                } else {
                    Tunnel tunnel = tunnelManager.getTunnel(frame.getTunnelId());
                    // TODO: validate message that it is truly belongs to this tunnel and have
                    // access to use it

                    long srcRoutingId = Ed25519KeyPair.generateRoutingId(frame.getSrcPubKey());
                    long dstRoutingId = frame.getDstRoutingId();

                    short endpoint1AppId = frame.getSrcAppId();
                    short endpoint2AppId = frame.getDstAppId();
                    if (srcRoutingId >= dstRoutingId) {
                        endpoint1AppId = frame.getDstAppId();
                        endpoint2AppId = frame.getSrcAppId();
                    }

                    Set<Pair<Short, Short>> appIds = Collections.synchronizedSet(new HashSet<>());
                    appIds.add(new Pair<Short, Short>(endpoint1AppId, endpoint2AppId));

                    tunnelManager
                            .removeTunnel(new Tunnel(tunnel.getEndpoint1RoutingId(), tunnel.getEndpoint2RoutingId(),
                                    tunnel.getPrevRoutingId(), tunnel.getNextRoutingId(), appIds));

                    long nextRoutingId = tunnel.getNextRoutingId() != prevNodeRoutingId ? tunnel.getNextRoutingId()
                            : tunnel.getPrevRoutingId();
                    transportProvider.sendFrame(frame, nextRoutingId);
                }
                break;
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
                long srcRoutingId = keyPair.routingId();
                long dstRoutingId = frame.getDstRoutingId();

                short endpoint1AppId = frame.getSrcAppId();
                short endpoint2AppId = frame.getDstAppId();
                if (srcRoutingId >= dstRoutingId) {
                    endpoint1AppId = frame.getDstAppId();
                    endpoint2AppId = frame.getSrcAppId();
                }

                Set<Pair<Short, Short>> appIds = Collections.synchronizedSet(new HashSet<>());
                appIds.add(new Pair<Short, Short>(endpoint1AppId, endpoint2AppId));

                Tunnel tunnel = new Tunnel(Math.min(srcRoutingId, dstRoutingId),
                        Math.max(srcRoutingId, dstRoutingId), 0, 0, appIds);
                tunnelManager.addPendingTunnel(tunnel);

                transportProvider.sendFrameToEveryone(frame);
                break;
            }
            case 2: {
                Tunnel tunnel = tunnelManager.getTunnel(frame.getTunnelId());
                // TODO: validate message that it is truly belongs to this tunnel and have
                // access to use it

                transportProvider.sendFrame(frame, tunnel.getNextRoutingId());
                break;
            }
            case 3: {
                Tunnel tunnel = tunnelManager.getTunnel(frame.getTunnelId());
                // TODO: validate message that it is truly belongs to this tunnel and have
                // access to use it

                long srcRoutingId = keyPair.routingId();
                long dstRoutingId = frame.getDstRoutingId();

                short endpoint1AppId = frame.getSrcAppId();
                short endpoint2AppId = frame.getDstAppId();
                if (srcRoutingId >= dstRoutingId) {
                    endpoint1AppId = frame.getDstAppId();
                    endpoint2AppId = frame.getSrcAppId();
                }

                Set<Pair<Short, Short>> appIds = Collections.synchronizedSet(new HashSet<>());
                appIds.add(new Pair<Short, Short>(endpoint1AppId, endpoint2AppId));

                tunnelManager.removeTunnel(new Tunnel(tunnel.getEndpoint1RoutingId(), tunnel.getEndpoint2RoutingId(),
                        tunnel.getPrevRoutingId(), tunnel.getNextRoutingId(), appIds));

                transportProvider.sendFrame(frame, tunnel.getNextRoutingId());
                break;
            }
        }
    }
}
