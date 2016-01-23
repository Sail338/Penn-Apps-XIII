package com.pennapps.client;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.*;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import io.socket.emitter.Emitter;
import org.w3c.dom.Entity;


import java.net.URISyntaxException;


public class MainActivity extends Activity  {
    private SensorManager manager;
    private Sensor sensor;
    float[] coords = new float[3];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try {
            manager = (SensorManager) getApplicationContext().getSystemService(Context.SENSOR_SERVICE);
            sensor = manager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        } catch (Exception e){
            Log.e("ERROR",e.getMessage());
        }
        manager.registerListener(listener,sensor,manager.SENSOR_DELAY_FASTEST);
        socket.connect();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private Socket socket;

    {
        try {

            socket = IO.socket("http://10.103.226.100:420");
            socket.open();
        //   listen();
            //creates sensor manager

        } catch (URISyntaxException e) {
            Log.e("ERROR", e.getMessage());
        }

    }

    private static final float NS2S = 1.0f / 1000000000.0f;
    private final float[] deltaRotationVector = new float[4];
    private float timestamp;

    SensorEventListener listener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            Log.i("SHIT",event.sensor.getName());
            Log.i("DIp",Float.toString(event.values[0]));
            //coords[0] = event.values[0];
            // This timestep's delta rotation to be multiplied by the current rotation
            // after computing it from the gyro sample data.
           // Log.e("TIMESTAMPE", Float.toString(timestamp));
            if (timestamp != 0) {
               final float dT = (event.timestamp - timestamp) * NS2S;
                // Axis of the rotation sample, not normalized yet.
                float axisX = event.values[0];
                float axisY = event.values[1];
                float axisZ = event.values[2];
                coords[0] = event.values[0];
                coords[1] =event.values[1];
                coords[2]= event.values[2];

                Log.e("LOG",Float.toString(coords[0]));
                socket.emit("satan", coords[0]+","+coords[1]+","+coords[2]);


                // Calculate the angular speed of the sample
                double omegaMagnitude = Math.sqrt(axisX * axisX + axisY * axisY + axisZ * axisZ);

                // Normalize the rotation vector if it's big enough to get the axis
                // (that is, EPSILON should represent your maximum allowable margin of error)
                if (omegaMagnitude > 1) {
                    axisX /= omegaMagnitude;
                    axisY /= omegaMagnitude;
                    axisZ /= omegaMagnitude;
                }

                // Integrate around this axis with the angular speed by the timestep
                // in order to get a delta rotation from this sample over the timestep
                // We will convert this axis-angle representation of the delta rotation
                // into a quaternion before turning it into the rotation matrix.
                float thetaOverTwo = (float) omegaMagnitude * dT / 2.0f;
                float sinThetaOverTwo = (float) Math.sin(thetaOverTwo);
                float cosThetaOverTwo = (float) Math.cos(thetaOverTwo);
                deltaRotationVector[0] = sinThetaOverTwo * axisX;
                deltaRotationVector[1] = sinThetaOverTwo * axisY;
                deltaRotationVector[2] = sinThetaOverTwo * axisZ;
                deltaRotationVector[3] = cosThetaOverTwo;
            }
            timestamp = event.timestamp;
            float[] deltaRotationMatrix = new float[9];
            manager.getRotationMatrixFromVector(deltaRotationMatrix, deltaRotationVector);

            Log.i("X", Float.toString(coords[0]));


            // User code should concatenate the delta rotation we computed with the current rotation
            // in order to get the updated rotation.
            // rotationCurrent = rotationCurrent * deltaRotationMatrix;
   }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }


    };


    public void listen(){
        socket.on("420", new Emitter.Listener() {

            @Override
            public void call(final Object... args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("DATA","RECEVIED");
                        if(args[0] instanceof String){
                            Log.d("SATAN",args[0].toString());
                            byte[] getdata  = Base64.decode(args[0].toString().getBytes(),Base64.NO_WRAP);
                            Bitmap bmp = BitmapFactory.decodeByteArray(getdata, 0,getdata.length);
                            if(!args[0].toString().equals("camera")){
                                displayImage(bmp);
                                return;
                            }


                            return;
                        }
                        if(args[0] instanceof JSONArray){
                            Log.d("SATAN",args[0].toString());
                            return;
                        }

                    }
                });

            }
        }) ;


    }
    public void displayImage(Bitmap map){


        ImageView imageView = (ImageView) findViewById(R.id.imageView);
        Log.d("RECIEVED","DECODED IMAGE");
        imageView.setImageBitmap(map);


    }




}