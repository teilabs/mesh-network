package io.github.teilabs.meshnet.client.android.core;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import io.github.teilabs.meshnet.client.android.util.AndroidLogger;
import io.github.teilabs.meshnet.core.crypto.Ed25519KeyPair;
import io.github.teilabs.meshnet.core.exception.MeshStorageException;
import io.github.teilabs.meshnet.core.exception.MeshValidationException;

public final class AndroidKeyStorage {
    private static final String TAG = "AndroidKeyStorage";
    private static final String PREF_NAME = "mesh_keys";
    private static final String KEY_PUBLIC = "public_key";
    private static final String KEY_PRIVATE = "private_key";

    private final SharedPreferences prefs;

    /**
     * Initializes encrypted storage for keys.
     * 
     * @param context Android context
     * @throws MeshValidationException if context is null.
     * @throws MeshStorageException    if EncryptedSharedPreferences initialization
     *                                 fails.
     */
    public AndroidKeyStorage(Context context) throws MeshValidationException, MeshStorageException {
        if (context == null) {
            throw new MeshValidationException("Context cannot be null");
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
            AndroidLogger.i(TAG, "Encrypted key storage initialized");
        } catch (Exception e) {
            AndroidLogger.e(TAG, "Failed to initialize EncryptedSharedPreferences", e);
            throw new MeshStorageException("Failed to initialize encrypted storage", e);
        }
    }

    public Ed25519KeyPair loadKeyPair() {
        String publicBase64 = prefs.getString(KEY_PUBLIC, null);
        String privateBase64 = prefs.getString(KEY_PRIVATE, null);
        if (publicBase64 == null || privateBase64 == null) {
            AndroidLogger.d(TAG, "No stored key pair found");
            return null;
        }
        try {
            AndroidLogger.d(TAG, "Stored key pair found, decoding");
            return Ed25519KeyPair.fromBase64(publicBase64, privateBase64);
        } catch (Exception e) {
            AndroidLogger.e(TAG, "Failed to decode key pair", e);
            return null;
        }
    }

    /**
     * Saves key pair to encrypted storage.
     * 
     * @param keyPair key pair to save
     * @return saved key pair
     * @throws MeshValidationException if keyPair is null.
     */
    public Ed25519KeyPair saveKeyPair(Ed25519KeyPair keyPair) throws MeshValidationException {
        if (keyPair == null) {
            throw new MeshValidationException("KeyPair cannot be null");
        }
        prefs.edit()
                .putString(KEY_PUBLIC, Ed25519KeyPair.toBase64Public(keyPair))
                .putString(KEY_PRIVATE, Ed25519KeyPair.toBase64Private(keyPair))
                .apply();
        AndroidLogger.i(TAG, "Key pair saved");
        return keyPair;
    }

    public void deleteKeyPair() {
        prefs.edit().remove(KEY_PRIVATE).apply();
        AndroidLogger.i(TAG, "Key pair deleted");
    }

    public boolean hasKeyPair() {
        String privB64 = prefs.getString(KEY_PRIVATE, null);
        boolean hasKeyPair = privB64 != null && !privB64.isEmpty();
        AndroidLogger.d(TAG, hasKeyPair ? "Key pair exists in storage" : "Key pair does not exist in storage");
        return hasKeyPair;
    }
}
