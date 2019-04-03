package fr.innodev.trd.gpsbasedemo;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.widget.Toast;

public class StepCounter implements SensorEventListener {
    public static final float M_PAR_PAS = 0.6f;
    SensorManager sensorManager ;
    Sensor sSensor;
    MapsActivity parentActivity;
    private long steps = 0;
    private Context aplicationContext;

    public StepCounter(Context aplicationContext ,MapsActivity parentActivity,SensorManager sensorManager) {
        this.sensorManager = sensorManager;
        this.parentActivity = parentActivity;
        this.aplicationContext = aplicationContext;
        this.sSensor= sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        this.sensorManager.registerListener(this,sSensor,SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor sensor = event.sensor;
        if (sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {
            steps++;
        }
        Toast.makeText(aplicationContext,"pasos"+steps,Toast.LENGTH_SHORT).show();
        parentActivity.updateCircle(steps * this.M_PAR_PAS);
        parentActivity.update1step();
    }

    public void resetCounter(){
        this.steps = 0;
        Toast.makeText(aplicationContext,"pasos to 0",Toast.LENGTH_SHORT).show();
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

}
