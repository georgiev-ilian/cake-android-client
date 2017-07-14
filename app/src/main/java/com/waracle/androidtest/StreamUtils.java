package com.waracle.androidtest;

import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Riad on 20/05/2015.
 *
 */
class StreamUtils {
    private static final String TAG = StreamUtils.class.getSimpleName();

    private static final int CHUNK_SIZE = 1024;

    // Can you see what's wrong with this???

    // Answer : unnessecary conversion to ArrayList, lack of control over byte size that are read
    static byte[] readUnknownFully(InputStream stream) throws IOException {

        // Read in stream of bytes
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[CHUNK_SIZE];
        while (true) {
            int r = stream.read(buffer);
            if (r == -1) break;
            out.write(buffer, 0, r);
        }

        // Return the raw byte array.
        return out.toByteArray();
    }

    static void close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            }
        }
    }
}
