package com.example.application;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.camera.core.ImageCapture;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;

public class MyThread extends Thread {
    private Bitmap image2 = null;
    private final Methods methods;
    private volatile boolean stopThread = false;
    private final ImageCapture imageCapture;
    private final Executor executor;
    private final Context context;
    private final long frequency;
    protected MyThread(ImageCapture imageCapture, Executor executor, Context context) {
        this.imageCapture = imageCapture;
        this.executor = executor;
        this.context = context;
        this.methods = new Methods(context);
        this.frequency = Long.parseLong(Objects.requireNonNull(Config.getConfigValue("frequency", context)));
    }

    public void stopThread()
    {
        stopThread = true;
    }

    @Override
    public void run() {
        Looper.prepare();
        while (!stopThread) {
            try {
                Thread.sleep(frequency);
                if(stopThread)
                    break;
                process();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                Log.d("Mythread","Thread Interrupted");
                return;
            } catch (ExecutionException e) {
                Log.e("Mythread", "ExecutionException occurred: " + e.getMessage());
                // Handle the exception appropriately, e.g. retry the operation
            } catch (IOException e) {
                Log.e("Mythread", "IOException occurred: " + e.getMessage());
                // Handle the exception appropriately, e.g. notify the user of the error
            }
        }
    }
    private void process() throws ExecutionException, InterruptedException, IOException {
        if (context == null) {
            Log.e("Mythread", "Context is null");
            return;
        }
        Toast.makeText(context.getApplicationContext(), "Start", Toast.LENGTH_SHORT).show();
        Future<Bitmap> future = methods.capturePhoto(imageCapture, executor);
        Bitmap image1 = future.get();
        image2 = methods.perform(image1, image2);
        Toast.makeText(context.getApplicationContext(), "done", Toast.LENGTH_SHORT).show();
    }

}
