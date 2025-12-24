package com.example.sitepulse.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ImageUtils {

    private static final String TAG = "ImageUtils";

    /**
     * Compresses the image at the given URI and saves it to a temporary file.
     * Returns the URI of the compressed file.
     */
    public static Uri compressImage(Context context, Uri imageUri) {
        try {
            // 1. Decode the image to Bitmap
            InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
            Bitmap originalBitmap = BitmapFactory.decodeStream(inputStream);
            if (inputStream != null) inputStream.close();

            if (originalBitmap == null) {
                Log.e(TAG, "Failed to decode bitmap");
                return imageUri; // Return original if failure
            }

            // 2. Resize if too large (e.g., max 1024px width/height)
            Bitmap resizedBitmap = resizeBitmap(originalBitmap, 1024);

            // 3. Create a temporary file for the compressed image
            File cacheDir = context.getCacheDir();
            File compressedFile = new File(cacheDir, "compressed_" + System.currentTimeMillis() + ".jpg");
            FileOutputStream out = new FileOutputStream(compressedFile);

            // 4. Compress to JPEG with 70% quality
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 70, out);
            out.flush();
            out.close();

            Log.d(TAG, "Image compressed. Original size: " + originalBitmap.getByteCount() +
                    ", Compressed size: " + compressedFile.length());

            return Uri.fromFile(compressedFile);

        } catch (IOException e) {
            Log.e(TAG, "Compression failed", e);
            return imageUri; // Return original on error
        }
    }

    private static Bitmap resizeBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }
}