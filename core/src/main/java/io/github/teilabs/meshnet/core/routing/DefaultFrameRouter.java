package io.github.teilabs.meshnet.core.routing;

import io.github.teilabs.meshnet.core.api.MeshIncomingMessage;
import io.github.teilabs.meshnet.core.api.MeshMessageCodec;
import io.github.teilabs.meshnet.core.api.MeshOutgoingMessage;
import io.github.teilabs.meshnet.core.buffer.FrameBuffer;
import io.github.teilabs.meshnet.core.config.Config;
import io.github.teilabs.meshnet.core.config.Config.TransitMode;
import io.github.teilabs.meshnet.core.config.Config.TunnelMode;
import io.github.teilabs.meshnet.core.crypto.Ed25519KeyPair;
import io.github.teilabs.meshnet.core.exception.MeshRoutingException;
import io.github.teilabs.meshnet.core.exception.MeshSecurityException;
import io.github.teilabs.meshnet.core.exception.MeshValidationException;
import io.github.teilabs.meshnet.core.frame.Frame;
import io.github.teilabs.meshnet.core.transport.NodesManager;
import io.github.teilabs.meshnet.core.transport.TransportProvider;
import io.github.teilabs.meshnet.core.util.Logger;
import io.github.teilabs.meshnet.core.util.Pair;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Default implementation of {@link FrameRouter}.
 */
public class DefaultFrameRouter implements FrameRouter {
    private static final String TAG = "DefaultFrameRouter";

    private final Ed25519KeyPair keyPair;

    private final FrameRouterEvents frameRouterEvents;

    private final MeshMessageCodec meshMessageCodec;

    private final FrameBuffer frameBuffer;

    private final TransportProvider transportProvider;

    private final NodesManager nodesManager;

    private final TunnelManager tunnelManager;

    private final Config config;

    private final Logger logger;

    public DefaultFrameRouter(Ed25519KeyPair keyPair, FrameRouterEvents frameRouterEvents,
            MeshMessageCodec meshMessageCodec,
            FrameBuffer frameBuffer, NodesManager nodesManager,
            TransportProvider transportProvider, TunnelManager tunnelManager, Config config, Logger logger) {
        this.keyPair = keyPair;
        this.frameRouterEvents = frameRouterEvents;
        this.frameBuffer = frameBuffer;
        this.meshMessageCodec = meshMessageCodec;
        this.transportProvider = transportProvider;
        this.nodesManager = nodesManager;
        this.tunnelManager = tunnelManager;
        this.config = config;
        this.logger = logger;
    }

    @Override
    public void onFrameReceived(Frame frame, long prevNodeRoutingId) throws MeshValidationException, MeshRoutingException {
        logger.d(TAG, "Received frame type " + frame.getType() + " from node " + prevNodeRoutingId);
        // Validate frame encrypted data size to match configured max size
        if (frame.getEncryptedData().length > config.maxFrameEncryptedDataSize()) {
            logger.e(TAG, "Frame encrypted data too large: " + frame.getEncryptedData().length);
            throw new MeshValidationException("Frame encrypted data size is too big");
        }

        switch (frame.getType()) {
            case Frame.TYPE_DATA: {
                // If we are final destination of this frame we should deliver it to the app
                if (frame.getDstRoutingId() == keyPair.routingId()) {
                    logger.d(TAG, "Delivering data frame to local app " + frame.getDstAppId());
                    MeshIncomingMessage message = meshMessageCodec.parseIncomingFrame(frame);
                    frameRouterEvents.transferMessageToApp(message);
                } else {
                    // Check if config allows us to transit someone's messages
                    if (config.transitMode() == TransitMode.NONE) {
                        logger.d(TAG, "Dropping transit data frame because transit mode is NONE");
                        break;
                    }

                    // Checking that we aren't already distributing this frame
                    if (!frameBuffer.containsFrame(frame)) {
                        // If we have connection to destination node we should immediately send frame to
                        // it without storing and redistributing
                        if (nodesManager.checkConnectionToNode(frame.getDstRoutingId())) {
                            logger.d(TAG, "Forwarding data frame directly to node " + frame.getDstRoutingId());
                            transportProvider.sendFrame(frame, frame.getDstRoutingId());
                            break;
                        }

                        // Check if config allows us to store someone's messages
                        if (config.transitMode() == TransitMode.STORE) {
                            logger.d(TAG, "Buffering transit data frame for node " + frame.getDstRoutingId());
                            frameBuffer.addFrame(frame);
                        }

                        logger.d(TAG, "Broadcasting transit data frame");
                        transportProvider.sendFrameToEveryone(frame);
                    }
                }
                break;
            }
            case Frame.TYPE_OPEN_TUNNEL: {
                if (frame.getDstRoutingId() == keyPair.routingId()) {
                    logger.i(TAG, "Processing open tunnel request for local node");
                    // Get src and dst routing ids from frame
                    long srcRoutingId = Ed25519KeyPair.generateRoutingId(frame.getSrcPubKey());
                    long dstRoutingId = frame.getDstRoutingId();

                    // In frame srcAppId belongs to srcRoutingId and dstAppId belongs to
                    // dstRoutingId, so if we will swap srcRoutingId and dstRoutingId in tunnel we
                    // should swap appIds too
                    short endpoint1AppId = frame.getSrcAppId();
                    short endpoint2AppId = frame.getDstAppId();
                    if (srcRoutingId >= dstRoutingId) {
                        endpoint1AppId = frame.getDstAppId();
                        endpoint2AppId = frame.getSrcAppId();
                    }

                    // Fill appIds set with correct ordered pair
                    Set<Pair<Short, Short>> appIds = Collections.synchronizedSet(new HashSet<Pair<Short, Short>>());
                    appIds.add(new Pair<Short, Short>(endpoint1AppId, endpoint2AppId));

                    // Create tunnel entity between this two nodes with provided appIds
                    Tunnel tunnel = new Tunnel(Math.min(srcRoutingId, dstRoutingId),
                            Math.max(srcRoutingId, dstRoutingId), prevNodeRoutingId,
                            prevNodeRoutingId, appIds);
                    try {
                        // If tunnel manager throws an exception, it means that user reject tunnel
                        // opening
                        tunnelManager.addTunnel(tunnel);

                        // If there is a pending tunnel, it means that we are initiator of the tunnel
                        // and we don't need to send open tunnel message
                        if (tunnelManager.containsPendingTunnel(Tunnel.generateTunnelId(srcRoutingId, dstRoutingId),
                                endpoint1AppId, endpoint2AppId)) {
                            logger.d(TAG, "Completing pending tunnel " + Tunnel.generateTunnelId(srcRoutingId, dstRoutingId));
                            tunnelManager.removePendingTunnel(tunnel);
                        } else {
                            // If there isn't a pending tunnel, it means that we are not initiator of the
                            // tunnel and we should send open tunnel message to the initiator node
                            MeshOutgoingMessage message = new MeshOutgoingMessage(MeshOutgoingMessage.TYPE_OPEN_TUNNEL,
                                    frame.getDstAppId(), frame.getSrcAppId(), frame.getSrcPubKey(), new byte[0], true);
                            logger.d(TAG, "Sending open tunnel confirmation to node " + prevNodeRoutingId);
                            transportProvider.sendFrame(meshMessageCodec.generateOutgoingFrame(message),
                                    prevNodeRoutingId);
                        }
                    } catch (MeshRoutingException | MeshSecurityException e) {
                        logger.w(TAG, "Open tunnel failed for local node: " + e.getMessage());
                        // If there is a pending tunnel, it means that we are initiator of the tunnel
                        // and we should clean up pending tunnels list
                        if (tunnelManager.containsPendingTunnel(Tunnel.generateTunnelId(srcRoutingId, dstRoutingId),
                                endpoint1AppId, endpoint2AppId)) {
                            tunnelManager.removePendingTunnel(tunnel);
                        }

                        // Send close tunnel frame to another endpoint node to tell that opening failed
                        MeshOutgoingMessage message = new MeshOutgoingMessage(MeshOutgoingMessage.TYPE_CLOSE_TUNNEL,
                                frame.getDstAppId(), frame.getSrcAppId(), frame.getSrcPubKey(), new byte[0], false);
                        logger.d(TAG, "Sending close tunnel after failed open request");
                        transportProvider.sendFrame(meshMessageCodec.generateOutgoingFrame(message),
                                prevNodeRoutingId);
                    }
                } else {
                    // Check if config allows us to be part of someone's tunnel
                    if (config.tunnelMode() != TunnelMode.RELAY) {
                        logger.d(TAG, "Skipping open tunnel relay because tunnel mode is not RELAY");
                        break;
                    }

                    // Get src and dst routing ids from frame
                    long srcRoutingId = Ed25519KeyPair.generateRoutingId(frame.getSrcPubKey());
                    long dstRoutingId = frame.getDstRoutingId();

                    // In frame srcAppId belongs to srcRoutingId and dstAppId belongs to
                    // dstRoutingId, so if we will swap srcRoutingId and dstRoutingId in tunnel we
                    // should swap appIds too
                    short endpoint1AppId = frame.getSrcAppId();
                    short endpoint2AppId = frame.getDstAppId();
                    if (srcRoutingId >= dstRoutingId) {
                        endpoint1AppId = frame.getDstAppId();
                        endpoint2AppId = frame.getSrcAppId();
                    }

                    long tunnelId = Tunnel.generateTunnelId(srcRoutingId, dstRoutingId);
                    // If there is a tunnel it means, that this message is going back to tunnel
                    // initiator and we should fill tunnel info with our second neighbor
                    if (tunnelManager.containsTunnel(tunnelId, endpoint1AppId, endpoint2AppId)) {
                        // If frame direction is false, it means that this message isn't going back to
                        // tunnel initiator, so we should skip it to prevent cycle
                        if (!frame.getDirection()) {
                            logger.d(TAG, "Ignoring open tunnel frame to prevent relay cycle");
                            break;
                        }
                        Tunnel tunnel = tunnelManager.getTunnel(tunnelId);
                        // Delete stored tunnel to replace it by updated entity
                        tunnelManager.removeTunnel(tunnel);

                        Set<Pair<Short, Short>> appIds = tunnel.getAppIds();
                        appIds.add(new Pair<Short, Short>(endpoint1AppId, endpoint2AppId));

                        // Create tunnel entity with filled nextRoutingId (our second neighbor)
                        tunnelManager.addTunnel(new Tunnel(Math.min(srcRoutingId, dstRoutingId),
                                Math.max(srcRoutingId, dstRoutingId),
                                tunnel.getPrevRoutingId(), prevNodeRoutingId, appIds));

                        logger.d(TAG, "Relaying tunnel response back to previous node " + tunnel.getPrevRoutingId());
                        transportProvider.sendFrame(frame, tunnel.getPrevRoutingId());
                    } else {
                        // If there isn't a tunnel it means, that this message is going through this
                        // node first time and we should store tunnel with prevNodeRoutingId as one of
                        // neighbors
                        Set<Pair<Short, Short>> appIds = Collections.synchronizedSet(new HashSet<Pair<Short, Short>>());
                        appIds.add(new Pair<Short, Short>(endpoint1AppId, endpoint2AppId));

                        // Create tunnel entity with filled prevRoutingId (our first neighbor)
                        tunnelManager.addTunnel(new Tunnel(Math.min(srcRoutingId, dstRoutingId),
                                Math.max(srcRoutingId, dstRoutingId),
                                prevNodeRoutingId, 0, appIds));

                        logger.d(TAG, "Broadcasting open tunnel request for relay path discovery");
                        transportProvider.sendFrameToEveryone(frame);
                    }
                }
                break;
            }
            case Frame.TYPE_DATA_TUNNEL: {
                if (frame.getDstRoutingId() == keyPair.routingId()) {
                    validateTunnelFrame(frame, prevNodeRoutingId);

                    // If we are final destination, notify destination app that frame received
                    logger.d(TAG, "Delivering tunnel data to local app " + frame.getDstAppId());
                    frameRouterEvents.transferMessageToApp(meshMessageCodec.parseIncomingFrame(frame));
                    // TODO: maybe send ack
                } else {
                    // Get tunnel from memory to find next node to send frame
                    long tunnelId = Tunnel.generateTunnelId(Ed25519KeyPair.generateRoutingId(frame.getSrcPubKey()),
                            frame.getDstRoutingId());
                    Tunnel tunnel = tunnelManager.getTunnel(tunnelId);

                    validateTunnelFrame(frame, tunnelId, prevNodeRoutingId);

                    // Determine which node should be next by excluding the previous node from our
                    // tunnel neighbors and send frame to it
                    long nextRoutingId = tunnel.getNextRoutingId() != prevNodeRoutingId ? tunnel.getNextRoutingId()
                            : tunnel.getPrevRoutingId();
                    logger.d(TAG, "Forwarding tunnel data frame to node " + nextRoutingId);
                    transportProvider.sendFrame(frame, nextRoutingId);
                }
                break;
            }
            case Frame.TYPE_CLOSE_TUNNEL: {
                if (frame.getDstRoutingId() == keyPair.routingId()) {
                    logger.i(TAG, "Processing close tunnel for local node");
                    // Get tunnel and pending tunnel from memory to delete it
                    long tunnelId = Tunnel.generateTunnelId(Ed25519KeyPair.generateRoutingId(frame.getSrcPubKey()),
                            frame.getDstRoutingId());
                    Tunnel tunnel = tunnelManager.getTunnel(tunnelId);
                    Tunnel pendingTunnel = tunnelManager.getPendingTunnel(tunnelId);

                    validateTunnelFrame(frame, tunnelId, prevNodeRoutingId);

                    // Get src and dst routing ids from frame
                    long srcRoutingId = Ed25519KeyPair.generateRoutingId(frame.getSrcPubKey());
                    long dstRoutingId = frame.getDstRoutingId();

                    // In frame srcAppId belongs to srcRoutingId and dstAppId belongs to
                    // dstRoutingId, so if we will swap srcRoutingId and dstRoutingId in tunnel we
                    // should swap appIds too
                    short endpoint1AppId = frame.getSrcAppId();
                    short endpoint2AppId = frame.getDstAppId();
                    if (srcRoutingId >= dstRoutingId) {
                        endpoint1AppId = frame.getDstAppId();
                        endpoint2AppId = frame.getSrcAppId();
                    }

                    // Fill appIds set with correct ordered pair
                    Set<Pair<Short, Short>> appIds = Collections.synchronizedSet(new HashSet<Pair<Short, Short>>());
                    appIds.add(new Pair<Short, Short>(endpoint1AppId, endpoint2AppId));

                    // Remove tunnel and pending tunnel from local memory
                    tunnelManager
                            .removeTunnel(new Tunnel(tunnel.getEndpoint1RoutingId(), tunnel.getEndpoint2RoutingId(),
                                    tunnel.getPrevRoutingId(), tunnel.getNextRoutingId(), appIds));
                    tunnelManager
                            .removePendingTunnel(new Tunnel(pendingTunnel.getEndpoint1RoutingId(),
                                    pendingTunnel.getEndpoint2RoutingId(),
                                    pendingTunnel.getPrevRoutingId(), pendingTunnel.getNextRoutingId(), appIds));
                    logger.d(TAG, "Removed local tunnel state for tunnel " + tunnelId);

                    // TODO: maybe notify app
                } else {
                    // Get tunnel from memory to delete it and find next node to send frame
                    long tunnelId = Tunnel.generateTunnelId(Ed25519KeyPair.generateRoutingId(frame.getSrcPubKey()),
                            frame.getDstRoutingId());
                    Tunnel tunnel = tunnelManager.getTunnel(tunnelId);

                    validateTunnelFrame(frame, tunnelId, prevNodeRoutingId);

                    // Get src and dst routing ids from frame
                    long srcRoutingId = Ed25519KeyPair.generateRoutingId(frame.getSrcPubKey());
                    long dstRoutingId = frame.getDstRoutingId();

                    // In frame srcAppId belongs to srcRoutingId and dstAppId belongs to
                    // dstRoutingId, so if we will swap srcRoutingId and dstRoutingId in tunnel we
                    // should swap appIds too
                    short endpoint1AppId = frame.getSrcAppId();
                    short endpoint2AppId = frame.getDstAppId();
                    if (srcRoutingId >= dstRoutingId) {
                        endpoint1AppId = frame.getDstAppId();
                        endpoint2AppId = frame.getSrcAppId();
                    }

                    // Fill appIds set with correct ordered pair
                    Set<Pair<Short, Short>> appIds = Collections.synchronizedSet(new HashSet<Pair<Short, Short>>());
                    appIds.add(new Pair<Short, Short>(endpoint1AppId, endpoint2AppId));

                    // Remove tunnel from local memory
                    tunnelManager
                            .removeTunnel(new Tunnel(tunnel.getEndpoint1RoutingId(), tunnel.getEndpoint2RoutingId(),
                                    tunnel.getPrevRoutingId(), tunnel.getNextRoutingId(), appIds));

                    // Determine which node should be next by excluding the previous node from our
                    // tunnel neighbors and send frame to it
                    long nextRoutingId = tunnel.getNextRoutingId() != prevNodeRoutingId ? tunnel.getNextRoutingId()
                            : tunnel.getPrevRoutingId();
                    logger.d(TAG, "Forwarding close tunnel frame to node " + nextRoutingId);
                    transportProvider.sendFrame(frame, nextRoutingId);
                }
                break;
            }
        }
    }

    @Override
    public void sendFrame(Frame frame) throws MeshValidationException, MeshRoutingException {
        logger.d(TAG, "Sending frame type " + frame.getType() + " from local node");
        // Validate frame encrypted data size to match configured max size
        if (frame.getEncryptedData().length > config.maxFrameEncryptedDataSize()) {
            logger.e(TAG, "Frame encrypted data too large: " + frame.getEncryptedData().length);
            throw new MeshValidationException("Frame encrypted data size is too big");
        }

        switch (frame.getType()) {
            case Frame.TYPE_DATA: {
                // Checking that we aren't already distributing this frame
                if (!frameBuffer.containsFrame(frame)) {
                    // If we have connection to destination node we should immediately send frame to
                    // it without storing and redistributing
                    if (nodesManager.checkConnectionToNode(frame.getDstRoutingId())) {
                        logger.d(TAG, "Sending data frame directly to node " + frame.getDstRoutingId());
                        transportProvider.sendFrame(frame, frame.getDstRoutingId());
                        break;
                    }
                    logger.d(TAG, "Buffering and broadcasting data frame");
                    frameBuffer.addFrame(frame);
                    transportProvider.sendFrameToEveryone(frame);
                }
                break;
            }
            case Frame.TYPE_OPEN_TUNNEL: {
                // Get src and dst routing ids from frame
                long srcRoutingId = Ed25519KeyPair.generateRoutingId(frame.getSrcPubKey());
                long dstRoutingId = frame.getDstRoutingId();

                // In frame srcAppId belongs to srcRoutingId and dstAppId belongs to
                // dstRoutingId, so if we will swap srcRoutingId and dstRoutingId in tunnel we
                // should swap appIds too
                short endpoint1AppId = frame.getSrcAppId();
                short endpoint2AppId = frame.getDstAppId();
                if (srcRoutingId >= dstRoutingId) {
                    endpoint1AppId = frame.getDstAppId();
                    endpoint2AppId = frame.getSrcAppId();
                }

                // Fill appIds set with correct ordered pair
                Set<Pair<Short, Short>> appIds = Collections.synchronizedSet(new HashSet<Pair<Short, Short>>());
                appIds.add(new Pair<Short, Short>(endpoint1AppId, endpoint2AppId));

                // Create tunnel entity between this two nodes with provided appIds
                Tunnel tunnel = new Tunnel(Math.min(srcRoutingId, dstRoutingId),
                        Math.max(srcRoutingId, dstRoutingId), 0, 0, appIds);
                // Store tunnel as pending to know that we are initiator
                tunnelManager.addPendingTunnel(tunnel);

                logger.i(TAG, "Broadcasting open tunnel request");
                transportProvider.sendFrameToEveryone(frame);
                break;
            }
            case Frame.TYPE_DATA_TUNNEL: {
                // Get tunnel from memory to find next node to send frame
                long tunnelId = Tunnel.generateTunnelId(Ed25519KeyPair.generateRoutingId(frame.getSrcPubKey()),
                        frame.getDstRoutingId());
                Tunnel tunnel = tunnelManager.getTunnel(tunnelId);

                validateTunnelFrame(frame, tunnelId, tunnel.getPrevRoutingId());

                // Send frame to next node from tunnel entity
                logger.d(TAG, "Sending tunnel data frame to node " + tunnel.getNextRoutingId());
                transportProvider.sendFrame(frame, tunnel.getNextRoutingId());
                break;
            }
            case Frame.TYPE_CLOSE_TUNNEL: {
                // Get tunnel from memory to find next node to send frame
                long tunnelId = Tunnel.generateTunnelId(Ed25519KeyPair.generateRoutingId(frame.getSrcPubKey()),
                        frame.getDstRoutingId());
                Tunnel tunnel = tunnelManager.getTunnel(tunnelId);

                validateTunnelFrame(frame, tunnelId, tunnel.getPrevRoutingId());

                // Get src and dst routing ids from frame
                long srcRoutingId = keyPair.routingId();
                long dstRoutingId = frame.getDstRoutingId();

                // In frame srcAppId belongs to srcRoutingId and dstAppId belongs to
                // dstRoutingId, so if we will swap srcRoutingId and dstRoutingId in tunnel we
                // should swap appIds too
                short endpoint1AppId = frame.getSrcAppId();
                short endpoint2AppId = frame.getDstAppId();
                if (srcRoutingId >= dstRoutingId) {
                    endpoint1AppId = frame.getDstAppId();
                    endpoint2AppId = frame.getSrcAppId();
                }

                // Fill appIds set with correct ordered pair
                Set<Pair<Short, Short>> appIds = Collections.synchronizedSet(new HashSet<Pair<Short, Short>>());
                appIds.add(new Pair<Short, Short>(endpoint1AppId, endpoint2AppId));

                // Remove tunnel from local memory to know that we can't use it anymore
                tunnelManager.removeTunnel(new Tunnel(tunnel.getEndpoint1RoutingId(), tunnel.getEndpoint2RoutingId(),
                        tunnel.getPrevRoutingId(), tunnel.getNextRoutingId(), appIds));

                // Send close tunnel frame to next node from tunnel entity to notify it that
                // tunnel is closed
                logger.i(TAG, "Sending close tunnel frame to node " + tunnel.getNextRoutingId());
                transportProvider.sendFrame(frame, tunnel.getNextRoutingId());
                break;
            }
        }
    }

    /**
     * Validates that frame sent through the tunnel truly belongs to this tunnel
     * 
     * @param frame             frame from the tunnel
     * @param prevNodeRoutingId routing id of the node from which we received the
     *                          frame
     * @throws MeshValidationException if the frame is invalid.
     * @throws MeshRoutingException if there's a routing error.
     */
    private void validateTunnelFrame(Frame frame, long prevNodeRoutingId) throws MeshValidationException, MeshRoutingException {
        // Generate tunnelId and call validateTunnelFrame function with all arguments
        long tunnelId = Tunnel.generateTunnelId(Ed25519KeyPair.generateRoutingId(frame.getSrcPubKey()),
                frame.getDstRoutingId());
        validateTunnelFrame(frame, tunnelId, prevNodeRoutingId);
    }

    /**
     * Validates that frame sent through the tunnel truly belongs to this tunnel
     * 
     * @param frame             frame from the tunnel
     * @param tunnelId          id of the tunnel
     * @param prevNodeRoutingId routing id of the node from which we received the
     *                          frame
     * @throws MeshValidationException if the frame is invalid.
     * @throws MeshRoutingException if there's a routing error.
     */
    private void validateTunnelFrame(Frame frame, long tunnelId, long prevNodeRoutingId) throws MeshValidationException, MeshRoutingException {
        logger.d(TAG, "Validating tunnel frame for tunnel " + tunnelId + " from node " + prevNodeRoutingId);
        // Get src and dst routing ids from frame
        long srcRoutingId = Ed25519KeyPair.generateRoutingId(frame.getSrcPubKey());
        long dstRoutingId = frame.getDstRoutingId();

        // In frame srcAppId belongs to srcRoutingId and dstAppId belongs to
        // dstRoutingId, so if we will swap srcRoutingId and dstRoutingId in tunnel we
        // should swap appIds too
        short endpoint1AppId = frame.getSrcAppId();
        short endpoint2AppId = frame.getDstAppId();
        if (srcRoutingId >= dstRoutingId) {
            endpoint1AppId = frame.getDstAppId();
            endpoint2AppId = frame.getSrcAppId();
        }

        // Check that tunnel with this id exists and contains target apps pair
        if (!tunnelManager.containsTunnel(tunnelId, endpoint1AppId, endpoint2AppId)) {
            logger.e(TAG, "Tunnel validation failed: missing tunnel " + tunnelId + " for app pair "
                    + endpoint1AppId + ":" + endpoint2AppId);
            throw new MeshValidationException("Tunnel does not exist. Or appIds list does not contain pair "
                    + endpoint1AppId + ":" + endpoint2AppId);
        }

        // Get tunnel, because we know that it exists
        Tunnel tunnel = tunnelManager.getTunnel(tunnelId);

        // If prevNodeRoutingId not equal to tunnel's next node routing id or prev
        // routing id, then it's not our neighbor int the tunnel, so it hasn't got
        // permission to send us the frame in this tunnel
        if (tunnel.getNextRoutingId() != prevNodeRoutingId && tunnel.getPrevRoutingId() != prevNodeRoutingId) {
            logger.e(TAG, "Tunnel validation failed: node " + prevNodeRoutingId + " is not part of tunnel "
                    + tunnelId);
            throw new MeshRoutingException("Node " + prevNodeRoutingId + " is not part of tunnel " + tunnelId);
        }

        // TODO: verify signature
    }
}
