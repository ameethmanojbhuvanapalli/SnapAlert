package com.example.application;


import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class ImageUploader extends AsyncTask<Void, Void, String> {

    private static final String TAG = "ImageUploader";
    private static String UPLOAD_URL;
    private final Bitmap mBitmap;
    private final Context context;

    public ImageUploader(Bitmap bitmap,Context context) {
        mBitmap = bitmap;
        this.context = context;
        UPLOAD_URL = Config.getConfigValue("api_url",context);
    }

    @Override
    protected String doInBackground(Void... voids) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        mBitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);
        byte[] byteArray = stream.toByteArray();

        HttpURLConnection urlConnection = null;
        DataOutputStream dos = null;
        InputStream is = null;
        String response = null;

        try {
            URL url = new URL(UPLOAD_URL);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);
            urlConnection.setUseCaches(false);
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Connection", "Keep-Alive");
            urlConnection.setRequestProperty("Content-Type", "multipart/form-data; boundary=*****");
            dos = new DataOutputStream(urlConnection.getOutputStream());

            // Add the file data to the request
            dos.writeBytes("--*****\r\n");
            dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\"" + "image.jpg" + "\"" + "\r\n");
            dos.writeBytes("\r\n");
            dos.write(byteArray);
            dos.writeBytes("\r\n");
            dos.writeBytes("--*****--\r\n");

            // Flush the output stream
            dos.flush();

            // Get the server response
            int responseCode = urlConnection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                is = new BufferedInputStream(urlConnection.getInputStream());
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8), 8);
                StringBuilder sb = new StringBuilder();

                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                response = sb.toString();
            }
        } catch (IOException e) {
            Log.e(TAG, "Error uploading image: " + e.getMessage());
        } finally {
            try {
                if (dos != null) {
                    dos.close();
                }
                if (is != null) {
                    is.close();
                }
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            } catch (IOException e) {
                Log.e(TAG, "Error closing streams: " + e.getMessage());
            }
        }

        return response;
    }

    @Override
    protected void onPostExecute(String result) {
        // Handle the server response here
        if(result != null && result.equals("Image uploaded successfully!"))
            Toast.makeText(context, "Uploaded", Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(context, "Error while uploading: can't reach the servers", Toast.LENGTH_SHORT).show();
    }

}
