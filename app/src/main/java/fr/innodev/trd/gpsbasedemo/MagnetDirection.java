package fr.innodev.trd.gpsbasedemo;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;



public class MagnetDirection implements SensorEventListener {
    private static final float CM_PAR_PAS = 0.6f;
    SensorManager sensorManager ;
    Sensor mSensor;
    MapsActivity parentActivity;
    private long steps = 0;
    private Context aplicationContext;
    float[] rotationMatrix = new float[9];
    float[] orientationAngles = new float[3];
    private float[] accelerometerReading = new float[3];
    private  float[] magnetometerReading = new float[3];
    Handler handler;

    public MagnetDirection(Context aplicationContext , MapsActivity parentActivity, SensorManager sensorManager) {
        this.sensorManager = sensorManager;
        this.parentActivity = parentActivity;
        this.aplicationContext = aplicationContext;

        Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer,
                    SensorManager.SENSOR_DELAY_NORMAL);
        }
        Sensor magneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        if (magneticField != null) {
            sensorManager.registerListener(this, magneticField,
                    SensorManager.SENSOR_DELAY_NORMAL);
        }


        handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                getOrientation();
                handler.postDelayed(this, 1000);
            }
        };

//Start
        handler.postDelayed(runnable, 1000);


    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, accelerometerReading,
                    0, accelerometerReading.length);
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, magnetometerReading,
                    0, magnetometerReading.length);
        }
    }
    public  float[] getOrientation(){
        SensorManager.getRotationMatrix(rotationMatrix, null,
                accelerometerReading, magnetometerReading);
        SensorManager.getOrientation(rotationMatrix, orientationAngles);
        Log.e("values","0:"+orientationAngles[0]+" 1:"+orientationAngles[1]+"2: "+orientationAngles[2]);
        return orientationAngles;
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

}
