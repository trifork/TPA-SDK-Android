package io.tpa.tpalib;

import android.support.annotation.NonNull;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

final class FileUtils {

    @SuppressWarnings("TryFinallyCanBeTryWithResources")
    @NonNull
    static byte[] readFile(@NonNull File file) throws IOException {
        // Open file
        RandomAccessFile f = new RandomAccessFile(file, "r");
        try {
            // Get and check length
            long longlength = f.length();
            int length = (int) longlength;
            if (length != longlength) {
                throw new IOException("File size >= 2 GB");
            }
            // Read file and return data
            byte[] data = new byte[length];
            f.readFully(data);
            return data;
        } finally {
            f.close();
        }
    }
}
