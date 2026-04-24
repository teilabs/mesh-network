package io.github.teilabs.meshnet.client.android.util;

import android.content.Context;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public final class FileUtils {

    private FileUtils() {
    }

    public static void write(Context context, String filename, byte[] data) throws IOException {
        File file = new File(context.getFilesDir(), filename);
        File parent = file.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            throw new IOException("Failed to create directory: " + parent);
        }
        try (FileOutputStream out = new FileOutputStream(file)) {
            out.write(data);
            out.flush();
        }
    }

    public static byte[] read(Context context, String filename) throws IOException {
        File file = new File(context.getFilesDir(), filename);
        if (!file.exists())
            return null;
        byte[] buffer = new byte[(int) file.length()];
        try (FileInputStream in = new FileInputStream(file)) {
            int totalRead = 0;
            while (totalRead < buffer.length) {
                int read = in.read(buffer, totalRead, buffer.length - totalRead);
                if (read == -1)
                    throw new IOException("Unexpected end of file");
                totalRead += read;
            }
        }
        return buffer;
    }

    public static void delete(Context context, String filename) {
        File file = new File(context.getFilesDir(), filename);
        if (!file.exists()) {
            file.delete();
        }
    }

    public static String[] list(Context context, String folderPath) {
        File folder = new File(context.getFilesDir(), folderPath);
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