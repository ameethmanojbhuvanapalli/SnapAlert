package com.example.application;


import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.telephony.SmsManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;

import com.google.common.util.concurrent.SettableFuture;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;


public class Methods {
    private static final double K1 = 0.01;
    private static final double K2 = 0.03;
    private void sendSms() {
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage("+918688213068", null, "sms", null, null);
    }
    protected Bitmap perform(Bitmap image1, Bitmap image2, Context context){
        if(image2 != null)
        {
            int diff = compareImages(image1,image2);
            Toast.makeText(context,String.valueOf(diff), Toast.LENGTH_SHORT).show();
            if(diff > 5)
            {
                sendSms();
                saveBitmap(image1,context);
                //Upload to server
                return image2;
            }
        }
        return image1;
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

        return (int) (100.0 * (1.0 - ssimAvg));
    }

    public void saveBitmap(Bitmap bitmap,Context context) {
        // Get the timestamp in a format that can be used in a file name
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
        String timeStamp = dateFormat.format(new Date());

        // Get the storage directory for saving the image
        File storageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "Snap Alert");
        storageDir.mkdirs();
        File file = new File(storageDir, "IMG_" + timeStamp + ".jpg");

        try {
            // Compress the bitmap image and save it to the file
            OutputStream outputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.flush();
            outputStream.close();

            // Add the image to the gallery so it can be viewed in the Photos app
            MediaScannerConnection.scanFile(context,
                    new String[]{file.getAbsolutePath()},
                    new String[]{"image/jpeg"}, null);

            // Show a toast message to indicate that the image was saved
            Toast.makeText(context, "Image saved to " + file.getAbsolutePath(), Toast.LENGTH_SHORT).show();

        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    protected Future<Bitmap> capturePhoto(ImageCapture imageCapture, Executor executor,Activity mainActivity) {
        final SettableFuture<Bitmap> future = SettableFuture.create();

        imageCapture.takePicture(executor, new ImageCapture.OnImageCapturedCallback() {
            @Override
            public void onCaptureSuccess(@NonNull ImageProxy image) {
                ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                byte[] bytes = new byte[buffer.remaining()];
                buffer.get(bytes);
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                future.set(bitmap);
                image.close();
                Toast.makeText(mainActivity, "Captured", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                future.setException(exception);
            }
        });

        return future;
    }



}

