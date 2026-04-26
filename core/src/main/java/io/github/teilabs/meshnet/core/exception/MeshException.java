package io.github.teilabs.meshnet.core.exception;

/**
 * Base exception for all mesh network related errors.
 */
public class MeshException extends RuntimeException {
    public MeshException(String message) {
        super(message);
    }

    public MeshException(String message, Throwable cause) {
        super(message, cause);
    }
}
