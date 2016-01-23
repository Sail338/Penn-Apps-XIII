package com.pennapps.client;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import io.socket.client.IO;
import io.socket.client.Socket;


import java.net.URISyntaxException;


public class MainActivity extends AppCompatActivity {
    private SensorManager manager;
    private Sensor sensor;
    private final float NS2S = 1.0f / 1000000000.0f;
    private final float[] deltaRotationVector = new float[4];
    private String[] coords;
   private float timestamp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
        try{

            socket = IO.socket("http://10.103.226.100:420");
            socket.open();
        //creates sensor manager
            for(int i=0;i<10;i++) {
                socket.emit("satan", "420");
            }
        } catch(URISyntaxException e){
            Log.e("ERROR",e.getMessage());
        }

    }

    public void onSensorChanged(SensorEvent event){

        manager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = manager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        String x = Float.toString(event.values[0]);
        String y = Float.toString(event.values[1]);
        String z = Float.toString(event.values[2]);
        coords[0] = x;
        coords[1] = y;
        coords[2] = z;

    }

}
