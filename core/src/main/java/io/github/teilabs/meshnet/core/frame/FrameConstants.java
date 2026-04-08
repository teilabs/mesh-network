package io.github.teilabs.meshnet.core.frame;

/** Class with all Frame related constants. */
public final class FrameConstants {
    public static final int VERSION = 1;

    // Version 1
    // Sizes
    public static final int SIGNATURE_SIZE_v1 = 64;
    public static final int PUBLICK_KEY_SIZE_v1 = 32;
    public static final int NONCE_SIZE_v1 = 12;
    public static final int HEADER_SIZE_v1 = 18 + PUBLICK_KEY_SIZE_v1 + NONCE_SIZE_v1;

    private FrameConstants() {}
}