package fr.innodev.trd.gpsbasedemo;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.widget.Toast;

public class StepCounter implements SensorEventListener {
    SensorManager sensorManager ;
    Sensor sSensor;

    private long steps = 0;
    private Context aplicationContext;
    public StepCounter(Context aplicationContext ,SensorManager sensorManager) {
        this.sensorManager = sensorManager;
        this.aplicationContext = aplicationContext;
        this.sSensor= sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        this.sensorManager.registerListener(this,sSensor,SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor sensor = event.sensor;
        float[] values = event.values;
        if (sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {
            steps++;
        }
        Toast.makeText(aplicationContext,"asdasd "+steps,Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

}
