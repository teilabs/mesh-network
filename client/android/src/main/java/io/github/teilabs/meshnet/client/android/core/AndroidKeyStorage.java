package io.github.teilabs.meshnet.client.android.core;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import io.github.teilabs.meshnet.client.android.util.Logger;
import io.github.teilabs.meshnet.core.crypto.Ed25519KeyPair;

public final class AndroidKeyStorage {
    private static final String TAG = "AndroidKeyStorage";
    private static final String PREF_NAME = "mesh_keys";
    private static final String KEY_PUBLIC = "public_key";
    private static final String KEY_PRIVATE = "private_key";

    private final SharedPreferences prefs;

    public AndroidKeyStorage(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("Context cannot be null");
        }
        try {
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();
            prefs = EncryptedSharedPreferences.create(
                    context,
                    PREF_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM);
        } catch (Exception e) {
            Logger.e(TAG, "Failed to initialize EncryptedSharedPreferences", e);
            throw new RuntimeException(e);
        }
    }

    public Ed25519KeyPair loadKeyPair() {
        String publicBase64 = prefs.getString(KEY_PUBLIC, null);
        String privateBase64 = prefs.getString(KEY_PRIVATE, null);
        if (publicBase64 == null || privateBase64 == null) {
            return null;
        }
        try {
            return Ed25519KeyPair.fromBase64(publicBase64, privateBase64);
        } catch (Exception e) {
            Logger.e(TAG, "Failed to decode key pair", e);
            return null;
        }
    }

    public Ed25519KeyPair saveKeyPair(Ed25519KeyPair keyPair) {
        if (keyPair == null) {
            throw new IllegalArgumentException("KeyPair cannot be null");
        }
        prefs.edit()
                .putString(KEY_PUBLIC, Ed25519KeyPair.toBase64Public(keyPair))
                .putString(KEY_PRIVATE, Ed25519KeyPair.toBase64Private(keyPair))
                .apply();
        return keyPair;
    }

    public void deleteKeyPair() {
        prefs.edit().remove(KEY_PRIVATE).apply();
        Logger.i(TAG, "Key pair deleted");
    }

    public boolean hasKeyPair() {
        String privB64 = prefs.getString(KEY_PRIVATE, null);
        return privB64 != null && !privB64.isEmpty();
    }
}
