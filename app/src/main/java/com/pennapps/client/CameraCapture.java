package com.pennapps.client;

import android.hardware.Camera;
import android.os.Environment;
import android.util.Log;

import org.json.JSONArray;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import io.socket.client.Socket;

public class CameraCapture {
    Camera mCamera;
    public Socket socket;
    private static final String TAG = "System";
    private static final int MEDIA_TYPE_PHOTO = 1;
    private static boolean isSafe = true;

    // constructor

    public CameraCapture () {
        mCamera = initializeCamera();
    }

    public void cameraActionCall () {

        // if camera is/was released, attempts to initialize it

        if (mCamera == null) {
            try {
                mCamera = initializeCamera();

            } catch (Exception e) {
                Log.d(TAG, "Failed to initialize camera, permission or in use");
                e.printStackTrace();
            }
        }

        snapshot();
    }

    // releases camera resource

    public void cameraRelease() {
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }

    // calls getCameraInstance

    private Camera initializeCamera () {
        mCamera = getCameraInstance();
        return mCamera;
    }

    // "starts" the camera, essentially

    private Camera getCameraInstance () {
        mCamera = null;
        try {
            mCamera = Camera.open();
            return mCamera;
        } catch (Exception e) {
            Log.d(TAG, "Camera is unavailable");
            e.printStackTrace();
        }
        return null;
    }

    // takes photo

    private void snapshot() {
        if (mCamera != null) {
            if (isSafe) {
                mCamera.takePicture(null, null, imgCallBack);
                isSafe = false;
            }
        }
    }

    private Camera.PictureCallback imgCallBack = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
            Log.d("satan", "took picture");
            JSONArray mJSONArray = new JSONArray(Arrays.asList(data));
            socket.emit("satan", mJSONArray);
            if (isSafe) {
                isSafe = false;
            }
            File imgFile = getMediaOutput(MEDIA_TYPE_PHOTO);
            if (imgFile == null) {
                Log.d(TAG, "Image creation failure");
                return;
            }

            // stores image to library for later processing

            try {

                // File I/O

                FileOutputStream fos = new FileOutputStream(imgFile);
                fos.write(data);
                fos.close();
                isSafe = true;

                // places a call to the imageProcessor, signalling new file

            } catch (Exception e) {
                Log.d(TAG, "Unable to save image file");
                e.printStackTrace();
            }
        }
    };

    // creates imgFile as well as returns the path to the imgFile to imgCallBack

    private File getMediaOutput(int type) {
        if (type == MEDIA_TYPE_PHOTO) {
            File mediaStorageDirectory = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES), "ProsoImgs");
            if (!mediaStorageDirectory.exists()) {
                if (!(mediaStorageDirectory.mkdir())) {
                    Log.d(TAG, "Failure to create directory, permissions?");
                    return null;
                }
            }

            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            File imgFile;
            imgFile = new File(mediaStorageDirectory.getPath() +
                    File.separator + timeStamp + "IMG.png");
            Log.d(TAG, imgFile.toString());

            return imgFile;
        }
        return null;
    }

    // returns Camera for those who requests it

    public Camera getCamera() {
        if (mCamera != null)
            return mCamera;
        return null;
    }

    public void endprogram() {
        cameraRelease();
        System.exit(0);
    }
}

