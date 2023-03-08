package com.example.application;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

public class MainActivity extends AppCompatActivity implements ImageAnalysis.Analyzer, View.OnClickListener {
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private Button bCapture;
    public static final int REQUEST_CAMERA = 101;
    private static final int REQUEST_EXTERNAL_STORAGE = 102;
    private static final String[] permstorage = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
    private static final String[] permcamera = {Manifest.permission.CAMERA};
    PreviewView previewView;
    private ImageCapture imageCapture;
    private MyThread myThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        previewView = findViewById(R.id.previewView);
        bCapture = findViewById(R.id.bCapture);
        bCapture.setBackgroundColor(Color.GREEN);
        bCapture.setTextColor(Color.BLACK);
        bCapture.setOnClickListener(this);
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {

                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                startCameraX(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }

        }, getExecutor());
        askPermissions();

    }
    Executor getExecutor() {
        return ContextCompat.getMainExecutor(this);
    }

    @SuppressLint("RestrictedApi")
    private void startCameraX(ProcessCameraProvider cameraProvider) {
        cameraProvider.unbindAll();
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();
        Preview preview = new Preview.Builder()
                .build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        imageCapture = new ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build();

        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();

        imageAnalysis.setAnalyzer(getExecutor(), this);

        //bind to lifecycle:
        cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);
    }

    @Override
    public void analyze(@NonNull ImageProxy image) {
        // image processing here for the current frame
        Log.d("TAG", "analyze: got the frame at: " + image.getImageInfo().getTimestamp());
        image.close();
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.bCapture) {
            if(bCapture.getText().equals("Start")) {
                myThread = new MyThread(imageCapture, getContentResolver(), getExecutor(), this);
                myThread.start();
                bCapture.setText("Stop");
                bCapture.setBackgroundColor(Color.RED);
                bCapture.setTextColor(Color.WHITE);
            }
            else
            {
                myThread.stopThread();
                bCapture.setText("Start");
                bCapture.setBackgroundColor(Color.GREEN);
                bCapture.setTextColor(Color.BLACK);
            }
        }
    }
    private void askPermissions() {
        int storage = ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE);
        int camera = ActivityCompat.checkSelfPermission(this,Manifest.permission.CAMERA);
        int sms = ActivityCompat.checkSelfPermission(this,Manifest.permission.SEND_SMS);
        if(camera != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,permcamera, REQUEST_CAMERA);
        }
        if (storage != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, permstorage, REQUEST_EXTERNAL_STORAGE);
        }
        if (sms != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, permstorage, REQUEST_EXTERNAL_STORAGE);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case REQUEST_CAMERA:
                if(grantResults[0] != PackageManager.PERMISSION_GRANTED)
                    Toast.makeText(this, "Camera permission is required", Toast.LENGTH_SHORT).show();
            case REQUEST_EXTERNAL_STORAGE:
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED)
                    Toast.makeText(this, "Storage permission is required", Toast.LENGTH_SHORT).show();
        }
    }

}
