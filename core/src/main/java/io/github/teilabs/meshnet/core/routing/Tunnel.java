package io.github.teilabs.meshnet.core.routing;

import io.github.teilabs.meshnet.core.crypto.Ed25519KeyPair;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Set;
import kotlin.Pair;

/**
 * Class for storing info about tunnel between two nodes.
 */
public final class Tunnel {
    /**
     * Routing Id (see {@link Ed25519KeyPair#generateRoutingId}) of one of tunnel
     * endpoints.
     * <br>
     * Must be strictly lower than {@link #endpoint2RoutingId}.
     */
    private final long endpoint1RoutingId; // endpoint1RoutingId < endpoint2RoutingId
    /**
     * Routing Id (see {@link Ed25519KeyPair#generateRoutingId}) of another tunnel
     * endpoint.
     * <br>
     * Must be strictly greater than {@link #endpoint1RoutingId}.
     */
    private final long endpoint2RoutingId;

    /**
     * Routing Id (see {@link Ed25519KeyPair#generateRoutingId}) of the previous
     * node in the path.
     */
    private final long prevRoutingId;
    /**
     * Routing Id (see {@link Ed25519KeyPair#generateRoutingId}) of the next node in
     * the path.
     */
    private final long nextRoutingId;

    /**
     * Set of appIds that can use this tunnel.
     * <br>
     * For each pair:
     * <ul>
     * <li>First element: application ID on {@link #endpoint1RoutingId}.</li>
     * <li>Second element: application ID on {@link #endpoint2RoutingId}.</li>
     * </ul>
     * <br>
     * And each app can communicate only with app in the same pair.
     */
    private final Set<Pair<Short, Short>> appIds;

    public Tunnel(long endpoint1RoutingId, long endpoint2RoutingId, long prevRoutingId, long nextRoutingId,
            Set<Pair<Short, Short>> appIds) {
        this.endpoint1RoutingId = endpoint1RoutingId;
        this.endpoint2RoutingId = endpoint2RoutingId;
        this.prevRoutingId = prevRoutingId;
        this.nextRoutingId = nextRoutingId;
        this.appIds = appIds;

        validateFields();
    }

    /** Validates fields values. */
    void validateFields() {
        if (endpoint1RoutingId >= endpoint2RoutingId) {
            throw new IllegalArgumentException(
                    "The source routing ID must be strictly less than the destination routing ID");
        }
    }

    public long getEndpoint1RoutingId() {
        return endpoint1RoutingId;
    }

    public long getEndpoint2RoutingId() {
        return endpoint2RoutingId;
    }

    public long getPrevRoutingId() {
        return prevRoutingId;
    }

    public long getNextRoutingId() {
        return nextRoutingId;
    }

    public Set<Pair<Short, Short>> getAppIds() {
        return new HashSet<Pair<Short, Short>>(appIds);
    }

    public static long generateTunnelId(long endpoint1RoutingId, long endpoint2RoutingId) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES * 2);
        buffer.putLong(Math.min(endpoint1RoutingId, endpoint2RoutingId));
        buffer.putLong(Math.max(endpoint1RoutingId, endpoint2RoutingId));
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(buffer.array());
            return ByteBuffer.wrap(hash, 0, 8).getLong();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (endpoint1RoutingId ^ (endpoint1RoutingId >>> 32));
        result = prime * result + (int) (endpoint2RoutingId ^ (endpoint2RoutingId >>> 32));
        result = prime * result + (int) (prevRoutingId ^ (prevRoutingId >>> 32));
        result = prime * result + (int) (nextRoutingId ^ (nextRoutingId >>> 32));
        result = prime * result + ((appIds == null) ? 0 : appIds.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Tunnel other = (Tunnel) obj;
        if (endpoint1RoutingId != other.endpoint1RoutingId)
            return false;
        if (endpoint2RoutingId != other.endpoint2RoutingId)
            return false;
        if (prevRoutingId != other.prevRoutingId)
            return false;
        if (nextRoutingId != other.nextRoutingId)
            return false;
        if (appIds == null) {
            if (other.appIds != null)
                return false;
        } else if (!appIds.equals(other.appIds))
            return false;
        return true;
    }

}
