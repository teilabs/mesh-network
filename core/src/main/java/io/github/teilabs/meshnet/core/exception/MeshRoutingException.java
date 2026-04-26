package io.github.teilabs.meshnet.core.exception;

/**
 * Thrown when an error occurs during routing or tunnel management.
 */
public class MeshRoutingException extends MeshException {
    public MeshRoutingException(String message) {
        super(message);
    }

    public MeshRoutingException(String message, Throwable cause) {
        super(message, cause);
    }
}
