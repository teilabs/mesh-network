package io.github.teilabs.meshnet.core.routing;

public final class Tunnel {
    private final long[] path;

    public Tunnel(long[] path) {
        this.path = path;
    }

    public long[] getPath() {
        return path.clone();
    }
}
