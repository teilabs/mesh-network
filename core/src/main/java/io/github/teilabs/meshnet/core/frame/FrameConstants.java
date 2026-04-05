package io.github.teilabs.meshnet.core.frame;

public final class FrameConstants {
    public static final int VERSION = 1;
    public static final int SIGNATURE_SIZE = 64;  // Ed25519
    public static final int PUBLICK_KEY_SIZE = 32;
    public static final int HEADER_SIZE = 20 + SIGNATURE_SIZE + PUBLICK_KEY_SIZE;

    private FrameConstants() {}
}