package com.pennapps.client;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.media.MediaRecorder;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.ragnarok.rxcamera.RxCamera;
import com.ragnarok.rxcamera.RxCameraData;
import com.ragnarok.rxcamera.config.RxCameraConfig;
import com.ragnarok.rxcamera.config.RxCameraConfigChooser;

import org.json.JSONArray;

import io.socket.client.IO;
import io.socket.client.Socket;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.zip.Deflater;


public class MainActivity extends AppCompatActivity {

    private static final int _IMAGES_PER_SECOND = 20;
    private Camera camera;
    private boolean readyForPicture = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        socket.connect();
        socket.emit("satan", "camera");

        camera = Camera.open();
        Camera.Parameters p = camera.getParameters();
        p.setPictureFormat(PixelFormat.JPEG);
        p.setJpegQuality(25);
        camera.setParameters(p);

        // Create our Preview view and set it as the content of our activity.
        CameraPreview mPreview = new CameraPreview(this, camera);
        FrameLayout preview = (FrameLayout) findViewById(R.id.generalLayout);
        preview.addView(mPreview);
        readyForPicture = true;

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                while(!readyForPicture){/*null*/}
                camera.takePicture(null, null, imgCallBack);
            }
        }, 5000);
    }

    private Camera.PictureCallback imgCallBack = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
            readyForPicture = false;
            Log.d("satan", data.length + " << size of data array.");

            Log.d("satan", "took picture");
            socket.emit("satan", Base64.encodeToString(data, Base64.NO_WRAP));
            camera.startPreview();
            readyForPicture = true;
        }
    };

   private Socket socket;
    {
        try{
            socket = IO.socket("http://10.103.226.100:420");
            socket.open();
        } catch(URISyntaxException e){
            Log.e("ERROR",e.getMessage());
        }

    }

}

