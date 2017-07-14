package com.waracle.androidtest;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.util.LruCache;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.InvalidParameterException;

/**
 * Created by Riad on 20/05/2015.
 *
 */
public class ImageLoader {

    private static final String TAG = ImageLoader.class.getSimpleName();

    private final LruCache<String, Bitmap> memoryCache;


    public ImageLoader() {
        memoryCache = setupCache();
    }

    private LruCache<String, Bitmap> setupCache() {
        LruCache<String, Bitmap> memoryCache;
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int cacheSize = maxMemory / 8;

        memoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getByteCount() / 1024;
            }
        };

        return memoryCache;
    }

    public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            memoryCache.put(key, bitmap);
        }
    }

    public Bitmap getBitmapFromMemCache(String key) {
        return memoryCache.get(key);
    }

    /**
     * Simple function for loading a bitmap image from the web
     *
     * @param url       image url
     * @param imageView view to set image too.
     */
    public void load(String url, ImageView imageView) {
        if (TextUtils.isEmpty(url)) {
            throw new InvalidParameterException("URL is empty!");
        }

        // Can you think of a way to improve loading of bitmaps
        // that have already been loaded previously??

        // Answer : use LruCache

        final Bitmap bitmap = getBitmapFromMemCache(url);
        if (bitmap != null) {
            setImageView(imageView, bitmap);
        } else {
            new LoadTask(imageView).execute(url);
        }
    }

    private class LoadTask extends AsyncTask<String, Void, Bitmap> {

        private ImageView imageView;

        public LoadTask(ImageView imageView) {
            this.imageView = imageView;
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            Bitmap bitmap = null;
            String url = params[0];

            try {
                bitmap = convertToBitmap(loadImageData(url));

                if (bitmap != null) {
                    addBitmapToMemoryCache(url, bitmap);
                }
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            }

            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            setImageView(imageView, bitmap);
        }
    }

    private static byte[] loadImageData(String url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        InputStream inputStream = null;
        try {
            try {
                // Read data from workstation
                inputStream = connection.getInputStream();
            } catch (IOException e) {
                // Read the error from the workstation
                inputStream = connection.getErrorStream();
            }

            // Can you think of a way to make the entire
            // HTTP more efficient using HTTP headers??

            return StreamUtils.readUnknownFully(inputStream);
        } finally {
            // Close the input stream if it exists.
            StreamUtils.close(inputStream);

            // Disconnect the connection
            connection.disconnect();
        }
    }

    private static Bitmap convertToBitmap(byte[] data) {
        return BitmapFactory.decodeByteArray(data, 0, data.length);
    }

    private static void setImageView(ImageView imageView, Bitmap bitmap) {
        imageView.setImageBitmap(bitmap);
    }
}
