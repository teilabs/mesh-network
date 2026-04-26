package io.github.teilabs.meshnet.client.android.util;

import android.util.Log;

/**
 * Utility class for logging messages to Android's logcat.
 */
public final class AndroidLogger {
    private static final String TAG_PREFIX = "MeshNet.";

    private AndroidLogger() {
    }

    public static void d(String tag, String message) {
        Log.d(TAG_PREFIX + tag, message);
    }

    public static void i(String tag, String message) {
        Log.i(TAG_PREFIX + tag, message);
    }

    public static void w(String tag, String message) {
        Log.w(TAG_PREFIX + tag, message);
    }

    public static void e(String tag, String message, Throwable th) {
        Log.e(TAG_PREFIX + tag, message, th);
    }

    public static void e(String tag, String message) {
        e(tag, message, null);
    }
}
