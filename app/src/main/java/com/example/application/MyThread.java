package com.example.application;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.camera.core.ImageCapture;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;

public class MyThread extends Thread {
    private Bitmap image1 = null;
    private Bitmap image2 = null;
    private Methods methods;
    private volatile boolean stopthread = false;

    private ImageCapture imageCapture;
    private ContentResolver contentResolver;
    private Executor executor;
    private MainActivity mainActivity;
    private String imgpath;

    protected MyThread(ImageCapture imageCapture, ContentResolver contentResolver, Executor executor, MainActivity mainActivity) {
        this.imageCapture = imageCapture;
        this.contentResolver = contentResolver;
        this.executor = executor;
        this.mainActivity = mainActivity;
        this.methods = new Methods();
    }

    public void stopThread()
    {
        stopthread = true;
    }

    @Override
    public void run() {
        Looper.prepare();
        while (!stopthread) {
            try {
                Thread.sleep(10000); // sleep for 10 seconds
                if(stopthread)
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
        Future<String> future = methods.capturePhoto(imageCapture, contentResolver, executor, mainActivity);
        imgpath = future.get(); // This will block until the future completes
        Toast.makeText(mainActivity, imgpath, Toast.LENGTH_SHORT).show();
        image1 = BitmapFactory.decodeFile(imgpath);
        Toast.makeText(mainActivity, "done", Toast.LENGTH_SHORT).show();
        image2 = methods.perform(image1, image2,mainActivity,imgpath);
    }

}
