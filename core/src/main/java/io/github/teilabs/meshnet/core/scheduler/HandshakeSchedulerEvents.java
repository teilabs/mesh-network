package io.github.teilabs.meshnet.core.scheduler;

import io.github.teilabs.meshnet.core.config.Config;
import java.util.concurrent.CompletableFuture;

/**
 * Functions that {@link HandshakeScheduler} can call.
 */
public interface HandshakeSchedulerEvents {
    /**
     * Sends handshake to a specific node.
     * 
     * @param nodeRoutingId routing id of the node to send to
     * @return a future that will be completed when the response handshake is
     *         received with true, or with false if response handshake isn't
     *         received in {@link Config#handshakeTimeoutSec} seconds.
     */
    CompletableFuture<Boolean> sendHandshake(long nodeRoutingId);
}
