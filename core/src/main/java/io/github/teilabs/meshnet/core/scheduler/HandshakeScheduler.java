package io.github.teilabs.meshnet.core.scheduler;

import io.github.teilabs.meshnet.core.config.Config;
import io.github.teilabs.meshnet.core.transport.NodesManager;
import io.github.teilabs.meshnet.core.util.Logger;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class HandshakeScheduler {
    private static final String TAG = "HandshakeScheduler";

    private final Config config;

    private final Logger logger;

    private final NodesManager nodesManager;

    private final HandshakeSchedulerEvents handshakeSchedulerEvents;

    private ScheduledExecutorService scheduler;

    private volatile boolean running = false;

    public HandshakeScheduler(Config config, Logger logger, NodesManager nodesManager,
            HandshakeSchedulerEvents handshakeSchedulerEvents) {
        this.config = config;
        this.logger = logger;
        this.nodesManager = nodesManager;
        this.handshakeSchedulerEvents = handshakeSchedulerEvents;
    }

    public synchronized void start() {
        if (running) {
            logger.w(TAG, "Already running");
            return;
        }

        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "MeshCore-Handshake");
            t.setDaemon(true);
            t.setPriority(Thread.MIN_PRIORITY);
            return t;
        });

        running = true;

        scheduler.scheduleAtFixedRate(this::broadcastHandshakes, config.handshakeIntervalMs(),
                config.handshakeIntervalMs(), TimeUnit.MILLISECONDS);

        logger.i(TAG, "Started with interval=" + (config.handshakeIntervalMs() / 1000) + "s");
    }
    
    public synchronized void stop() {
        if (!running) {
            logger.w(TAG, "Already stopped");
            return;
        }

        if (scheduler != null) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    logger.w(TAG, "Scheduler did not terminate gracefully, forcing shutdown");
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                logger.e(TAG, "Interrupted while waiting for scheduler shutdown", e);
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
            scheduler = null;
        }

        running = false;

        logger.i(TAG, "Stopped");
    }

    private void broadcastHandshakes() {
        if (!running) return;

        Set<Long> nodesSnapshot;
        try {
            nodesSnapshot = new HashSet<>(nodesManager.getNodes());
        } catch (Exception e) {
            logger.e(TAG, "Failed to get nodes snapshot", e);
            return;
        }

        if (nodesSnapshot.isEmpty()) {
            logger.d(TAG, "No nodes to send handshakes to");
            return;
        }

        logger.d(TAG, "Scheduling handshakes for " + nodesSnapshot.size() + " nodes");

        int processedNodesCount = 0;
        for (long nodeId : nodesSnapshot) {
            long offset = (long) ((config.handshakeIntervalMs() * processedNodesCount) / nodesSnapshot.size());

            scheduler.schedule(() -> {
                if (!running) return;
                try {
                    handshakeSchedulerEvents.sendHandshake(nodeId);
                } catch (Exception e) {
                    logger.e(TAG, "Failed to send handshake to node " + nodeId, e);
                }
            }, offset, TimeUnit.MILLISECONDS);
        }
    }

    public synchronized boolean isRunning() {
        return running;
    }
}
