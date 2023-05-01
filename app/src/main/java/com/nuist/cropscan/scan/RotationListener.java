package com.nuist.cropscan.scan;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

/**
 * ->  tah9  2023/4/30 5:01
 */
public class RotationListener {
    private static final String TAG = "RotationListener";
    private LifecycleOwner lifecycle;
    private Context context;
    private SensorManager sensorManager;
    private Sensor rotationSensor, orientationSensor;

    public RotationListener(LifecycleOwner lifecycle, Context context) {
        this.lifecycle = lifecycle;
        this.context = context;

        lifecycle.getLifecycle().addObserver(new DefaultLifecycleObserver() {
            @Override
            public void onCreate(@NonNull LifecycleOwner owner) {
                DefaultLifecycleObserver.super.onCreate(owner);
                sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
                rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
                orientationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
            }

            @Override
            public void onResume(@NonNull LifecycleOwner owner) {
                DefaultLifecycleObserver.super.onResume(owner);
                // 注册方向传感器监听器
                sensorManager.registerListener(sensorEventListener, rotationSensor, SensorManager.SENSOR_DELAY_NORMAL);
                sensorManager.registerListener(sensorEventListener, orientationSensor, SensorManager.SENSOR_DELAY_NORMAL);
            }

            @Override
            public void onPause(@NonNull LifecycleOwner owner) {
                DefaultLifecycleObserver.super.onPause(owner);
                // 取消注册方向传感器监听器
                sensorManager.unregisterListener(sensorEventListener);
            }
        });
    }

    private float preAngle = 0;
    private float[] rotationMatrix = new float[9];
    private float[] orientationAngles = new float[3];
    private int lastOrientation = -1;
    private SensorEventListener sensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {

            if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
                SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values);
                SensorManager.getOrientation(rotationMatrix, orientationAngles);
            }

            float azimuth = (float) Math.toDegrees(orientationAngles[0]);
            float pitch = (float) Math.toDegrees(orientationAngles[1]);
            float roll = (float) Math.toDegrees(orientationAngles[2]);

            //将角度值限制在 -180 到 180 的范围内
            if (azimuth > 180) {
                azimuth -= 360;
            } else if (azimuth < -180) {
                azimuth += 360;
            }

            if (pitch > 180) {
                pitch -= 360;
            } else if (pitch < -180) {
                pitch += 360;
            }

            if (roll > 180) {
                roll -= 360;
            } else if (roll < -180) {
                roll += 360;
            }

            //获取 90 的整数倍的角度值
            int rollInt = -Math.round(roll / 90) * 90;

                if (preAngle!=rollInt){
                    listener.turnAround(rollInt);
                    preAngle=rollInt;
                }


        }


        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // 方向传感器精度变化时回调该方法
//            Log.d(TAG, "onAccuracyChanged: ");
        }
    };

    public interface SensorRotationListener {
        void turnAround(float v);
    }

    private SensorRotationListener listener;

    public void setSensorEventListener(SensorRotationListener listener) {
        this.listener = listener;
    }

}
