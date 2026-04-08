package io.github.teilabs.meshnet.core.routing;

/**
 * Class for storing info about tunel betewen this node and destination node.
 */
public final class Tunnel {
    /**
     * Path to destination node.
     * <br>
     * Must start with this node routing id and ends with destination node routing
     * id.
     */
    private final long[] path;

    public Tunnel(long[] path) {
        this.path = path;
    }

    public long[] getPath() {
        return path.clone();
    }
}
