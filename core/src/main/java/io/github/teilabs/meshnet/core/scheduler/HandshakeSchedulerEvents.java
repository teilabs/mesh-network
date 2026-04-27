package io.github.teilabs.meshnet.core.scheduler;

import java.util.concurrent.CompletableFuture;

public interface HandshakeSchedulerEvents {
    CompletableFuture<Boolean> sendHandshake(long nodeRoutingId);
}
