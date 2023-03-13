package com.example.application;

import android.graphics.Bitmap;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.camera.core.ImageCapture;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;

public class MyThread extends Thread {
    private Bitmap image2 = null;
    private final Methods methods;
    private volatile boolean stopThread = false;
    private final ImageCapture imageCapture;
    private final Executor executor;
    private final MainActivity mainActivity;

    protected MyThread(ImageCapture imageCapture, Executor executor, MainActivity mainActivity) {
        this.imageCapture = imageCapture;
        this.executor = executor;
        this.mainActivity = mainActivity;
        this.methods = new Methods();
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
                Thread.sleep(10000); // sleep for 10 seconds
                if(stopThread)
                    break;
                process();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                Log.d("Mythread","Thread Interrupted");
                return;
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
    private void process() throws ExecutionException, InterruptedException, IOException {
        Toast.makeText(mainActivity, "Start", Toast.LENGTH_SHORT).show();
        Future<Bitmap> future = methods.capturePhoto(imageCapture, executor, mainActivity);
        Bitmap image1 = future.get();
        Toast.makeText(mainActivity, "done", Toast.LENGTH_SHORT).show();
        image2 = methods.perform(image1, image2,mainActivity);
    }

}
