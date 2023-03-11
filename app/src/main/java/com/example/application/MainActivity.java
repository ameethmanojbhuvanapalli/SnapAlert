package com.example.application;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.SEND_SMS;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
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

import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

public class MainActivity extends AppCompatActivity implements ImageAnalysis.Analyzer, View.OnClickListener {
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private Button bCapture;
    private final String[] permissions = {CAMERA, SEND_SMS, WRITE_EXTERNAL_STORAGE};
    private static final int REQUEST_CODE = 1;

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
        verifyPermissions();

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

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.bCapture) {
            if(bCapture.getText().equals("Start")) {
                myThread = new MyThread(imageCapture, getExecutor(), this);
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
    private void verifyPermissions() {
        for (String permission : Arrays.asList(permissions)) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE);
                break;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE) {
            boolean allPermissionsGranted = true;
            boolean canSkipWriteExternalStorage = Build.VERSION.SDK_INT > Build.VERSION_CODES.R;

            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    if (!(permissions[i].equals(WRITE_EXTERNAL_STORAGE) && canSkipWriteExternalStorage)) {
                        allPermissionsGranted = false;
                        break;
                    }
                }
            }

            if (!allPermissionsGranted) {
                Toast.makeText(this, "Permissions are required", Toast.LENGTH_SHORT).show();
                showPermissionDeniedDialog();
            }
        }
    }

    private void showPermissionDeniedDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Permission denied")
                .setMessage("These permissions are required to use the app. Please grant the permission in the app settings.")
                .setPositiveButton("Go to settings", (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                    finish();// Close the app
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    finish(); // Close the app
                })
                .show();
    }

}
