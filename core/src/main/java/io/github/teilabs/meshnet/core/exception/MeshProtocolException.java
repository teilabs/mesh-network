package io.github.teilabs.meshnet.core.exception;

/**
 * Thrown when there is an error in the mesh protocol, such as invalid frame format or unsupported versions.
 */
public class MeshProtocolException extends MeshException {
    public MeshProtocolException(String message) {
        super(message);
    }
}
