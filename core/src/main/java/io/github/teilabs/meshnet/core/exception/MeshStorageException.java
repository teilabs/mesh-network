package io.github.teilabs.meshnet.core.exception;

/**
 * Thrown when an error occurs while accessing or modifying stored data.
 */
public class MeshStorageException extends MeshException {
    public MeshStorageException(String message) {
        super(message);
    }

    public MeshStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
