package io.github.teilabs.meshnet.core.exception;

/**
 * Thrown when a security-related error occurs, such as invalid signatures or decryption failures.
 */
public class MeshSecurityException extends MeshException {
    public MeshSecurityException(String message) {
        super(message);
    }

    public MeshSecurityException(String message, Throwable cause) {
        super(message, cause);
    }
}
