package io.github.teilabs.meshnet.client.android.util;

import android.content.Context;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import io.github.teilabs.meshnet.core.exception.MeshStorageException;

/**
 * Utility class for file operations.
 */
public final class FileUtils {

    private FileUtils() {
    }

    /**
     * Writes data to a file in the app's files directory.
     * 
     * @param context Android context
     * @param path    path of the file
     * @param data    data to write
     * @throws MeshStorageException if file writing fails.
     */
    public static void write(Context context, String path, byte[] data) throws MeshStorageException {
        File file = new File(context.getFilesDir(), path);
        File parent = file.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            throw new MeshStorageException("Failed to create directory: " + parent);
        }
        try (FileOutputStream out = new FileOutputStream(file)) {
            out.write(data);
            out.flush();
        } catch (IOException e) {
            throw new MeshStorageException("Failed to write file: " + path, e);
        }
    }

    /**
     * Reads data from a file in the app's files directory.
     * 
     * @param context Android context
     * @param path    path of the file
     * @return data read from the file, or null if file doesn't exist
     * @throws MeshStorageException if file reading fails.
     */
    public static byte[] read(Context context, String path) throws MeshStorageException {
        File file = new File(context.getFilesDir(), path);
        if (!file.exists())
            return null;
        byte[] buffer = new byte[(int) file.length()];
        try (FileInputStream in = new FileInputStream(file)) {
            int totalRead = 0;
            while (totalRead < buffer.length) {
                int read = in.read(buffer, totalRead, buffer.length - totalRead);
                if (read == -1)
                    throw new MeshStorageException("Unexpected end of file: " + path);
                totalRead += read;
            }
        } catch (IOException e) {
            throw new MeshStorageException("Failed to read file: " + path, e);
        }
        return buffer;
    }

    /**
     * Deletes a file in the app's files directory.
     * 
     * @param context Android context
     * @param path    path of the file
     */
    public static void delete(Context context, String path) {
        File file = new File(context.getFilesDir(), path);
        if (file.exists()) {
            file.delete();
        }
    }

    /**
     * Lists all files in the folder int the app's files directory.
     * 
     * @param context Android context
     * @param path    path of the folder
     * @return array of file names
     */
    public static String[] list(Context context, String path) {
        File folder = new File(context.getFilesDir(), path);
        if (!folder.exists() || !folder.isDirectory())
            return new String[0];
        File[] files = folder.listFiles();
        if (files == null)
            return new String[0];
        String[] names = new String[files.length];
        for (int i = 0; i < files.length; i++) {
            names[i] = files[i].getName();
        }
        return names;
    }
}
