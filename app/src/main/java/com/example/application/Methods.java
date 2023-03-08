package com.example.application;


import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.telephony.SmsManager;
import android.util.Base64;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import com.google.common.util.concurrent.SettableFuture;


public class Methods {
    private static final double K1 = 0.01;
    private static final double K2 = 0.03;
    private void sendSms(Context context,Bitmap bitmap) {
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage("+918688213068", null, "sms", null, null);
    }
    protected Bitmap perform(Bitmap image1, Bitmap image2, Context context,String imgpath) throws IOException {
        if(image2 != null)
        {
            int diff = compareImages(image1,image2);
            Toast.makeText(context,String.valueOf(diff), Toast.LENGTH_SHORT).show();
            if(diff > 5)
            {
                sendSms(context,image1);
                return image2;
            }
        }
        Bitmap temp = image1;

        // Delete the file associated with image1
        File file = new File(imgpath);
        if (file.delete())
            Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT).show();
        return temp;
    }
    private int compareImages(Bitmap image1, Bitmap image2) {
        if (image1 == null || image2 == null) {
            throw new IllegalArgumentException("Bitmaps cannot be null");
        }

        int width = image1.getWidth();
        int height = image1.getHeight();

        if (width != image2.getWidth() || height != image2.getHeight()) {
            throw new IllegalArgumentException("Bitmaps must have the same dimensions");
        }

        double sum = 0.0;
        int count = 0;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel1 = image1.getPixel(x, y);
                int pixel2 = image2.getPixel(x, y);

                double red1 = Color.red(pixel1) / 255.0;
                double green1 = Color.green(pixel1) / 255.0;
                double blue1 = Color.blue(pixel1) / 255.0;

                double red2 = Color.red(pixel2) / 255.0;
                double green2 = Color.green(pixel2) / 255.0;
                double blue2 = Color.blue(pixel2) / 255.0;

                double luma1 = 0.299 * red1 + 0.587 * green1 + 0.114 * blue1;
                double luma2 = 0.299 * red2 + 0.587 * green2 + 0.114 * blue2;

                double variance1 = (0.299 * red1 - luma1) * (0.299 * red1 - luma1)
                        + (0.587 * green1 - luma1) * (0.587 * green1 - luma1)
                        + (0.114 * blue1 - luma1) * (0.114 * blue1 - luma1);

                double variance2 = (0.299 * red2 - luma2) * (0.299 * red2 - luma2)
                        + (0.587 * green2 - luma2) * (0.587 * green2 - luma2)
                        + (0.114 * blue2 - luma2) * (0.114 * blue2 - luma2);

                double covariance = (0.299 * red1 - luma1) * (0.299 * red2 - luma2)
                        + (0.587 * green1 - luma1) * (0.587 * green2 - luma2)
                        + (0.114 * blue1 - luma1) * (0.114 * blue2 - luma2);

                double ssim = ((2 * luma1 * luma2 + K1) * (2 * covariance + K2))
                        / ((luma1 * luma1 + luma2 * luma2 + K1) * (variance1 + variance2 + K2));

                sum += ssim;
                count++;
            }
        }

        double ssimAvg = sum / count;
        // Compute the percentage difference
        int diffPercent = (int) (100.0 * (1.0 - ssimAvg));

        return diffPercent;
    }

    protected String getLatestPath() {
        File directory = new File("/storage/emulated/0/Pictures");
        File[] files = directory.listFiles((dir, name) -> name.toLowerCase().endsWith(".jpg") || name.toLowerCase().endsWith(".jpeg") || name.toLowerCase().endsWith(".png"));

        if (files != null && files.length > 0) {
            File latestFile = files[0];

            for (int i = 1; i < files.length; i++) {
                if (files[i].lastModified() > latestFile.lastModified()) {
                    latestFile = files[i];
                }
            }

            return latestFile.getAbsolutePath();
        }
        return null;
    }
    protected Future<String> capturePhoto(ImageCapture imageCapture, ContentResolver contentResolver, Executor executor, MainActivity mainActivity) {
        long timestamp = System.currentTimeMillis();
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, timestamp);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");

        final SettableFuture<String> future = SettableFuture.create();

        imageCapture.takePicture(
                new ImageCapture.OutputFileOptions.Builder(
                        contentResolver,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        contentValues
                ).build(),
                executor,
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        String path = getLatestPath();
                        future.set(path);
                        Toast.makeText(mainActivity, "Photo has been saved successfully.", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        future.setException(exception);
                        Toast.makeText(mainActivity, "Error saving photo: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
        );
        return future;
    }

}

