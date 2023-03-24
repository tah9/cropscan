package com.nuist.cropscan;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

import com.nuist.cropscan.aidl.AidlServiceTest;
import com.nuist.cropscan.scan.ActCropScan;

class MainLifecycle implements DefaultLifecycleObserver {
    private static final String TAG = "MainLifecycle";

    @Override
    public void onCreate(@NonNull LifecycleOwner owner) {
        DefaultLifecycleObserver.super.onCreate(owner);
        Log.d(TAG, "onCreate: ");
    }

    @Override
    public void onStop(@NonNull LifecycleOwner owner) {
        DefaultLifecycleObserver.super.onStop(owner);
        Log.d(TAG, "onStop: ");
    }
}

public class MainActivity extends AppCompatActivity {
    public Context context = this;
    private static final String TAG = "MainActivity";
    private boolean bindService;
    private Button camera;
    private Button btn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_main);
        initView();
        getLifecycle().addObserver(new MainLifecycle());




    }


    @Override
    protected void onStop() {
        super.onStop();

        Log.d(TAG, "onStop: ");

        try {
            if (bindService) {
                unbindService(serviceConnection);
            }
        } catch (Exception e) {
            Log.d(TAG, "unbindService: " + e);
        }
    }

    private void setupService() {
        Intent intent = new Intent(context, AidlServiceTest.class);
        bindService = bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        startService(intent);
    }


    ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.d(TAG, "onServiceConnected: ");
            IMyAidlInterface iMyAidlInterface = IMyAidlInterface.Stub.asInterface(iBinder);
            PcPathBean pcPathBean = new PcPathBean();
            pcPathBean.setPath("测试路径");
            pcPathBean.setTime(System.currentTimeMillis());
            try {
                Log.d(TAG, "onServiceConnected: " + System.currentTimeMillis());
                iMyAidlInterface.sendMessage(pcPathBean);
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.d(TAG, "onServiceDisconnected: ");
        }
    };

    private void initView() {
        findViewById(R.id.btn).setOnClickListener(v -> {
            setupService();
        });
        camera = (Button) findViewById(R.id.camera);
        btn = (Button) findViewById(R.id.btn);
        camera.setOnClickListener(v->{
            startActivity(new Intent(this, ActCropScan.class));
        });
    }
}