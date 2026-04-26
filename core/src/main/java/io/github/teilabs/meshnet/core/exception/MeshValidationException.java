package io.github.teilabs.meshnet.core.exception;

/**
 * Thrown when validation of mesh-specific data fails.
 */
public class MeshValidationException extends MeshException {
    public MeshValidationException(String message) {
        super(message);
    }
}
