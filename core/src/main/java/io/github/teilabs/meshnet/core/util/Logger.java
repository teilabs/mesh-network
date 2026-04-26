package io.github.teilabs.meshnet.core.util;

/**
 * Platform-agnostic logging interface.
 * Implementations should be provided by the client (platform specified)
 * package.
 */
public interface Logger {
    void d(String tag, String message);

    void i(String tag, String message);

    void w(String tag, String message);

    void e(String tag, String message, Throwable t);

    default void e(String tag, String message) {
        e(tag, message, null);
    }
}
