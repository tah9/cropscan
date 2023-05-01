package com.nuist.cropscan.base;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.nuist.cropscan.R;

import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class BaseAct extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {
    public Context context = this;
    public static final String TAG = "BaseAct";
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;


    public void setString(String key, String value) {
        editor.putString(key, value).commit();
    }

    public String optString(String key) {
        return sharedPreferences.getString(key, "");
    }

    public int optInt(String key) {
        return sharedPreferences.getInt(key, 0);
    }

    public void setInt(String key, int value) {
        editor.putInt(key, value).commit();
    }

    public void clearSp() {
        int webVersion = optInt(getResources().getString(R.string.web_version));
        editor.clear().commit();
        setInt(getResources().getString(R.string.web_version), webVersion);
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setLightStatusBar(getWindow(),false, Color.TRANSPARENT);
        methodRequiresThreePermission();
        sharedPreferences = getSharedPreferences(getPackageName(), MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public void setLightStatusBar(Window window, boolean lightStatusBar, int color) {
        // 设置浅色状态栏时的界面显示
        View decor = window.getDecorView();
        int ui = decor.getSystemUiVisibility();
        if (lightStatusBar) {
            ui |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        } else {
            ui &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        }
        decor.setSystemUiVisibility(ui);
        //取消设置透明状态栏,使 ContentView 内容不再覆盖状态栏
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        //需要设置这个 flag 才能调用 setStatusBarColor 来设置状态栏颜色
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        //设置状态栏颜色
        window.setStatusBarColor(color);

        // 去掉系统状态栏下的windowContentOverlay
        View v = window.findViewById(android.R.id.content);
        if (v != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                v.setForeground(null);
            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    public static final int RC_CAMERA_FILE_LOCATION = 1; // requestCode
    String[] perms = {Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};

    public void afterPermission() {

    }

    @AfterPermissionGranted(RC_CAMERA_FILE_LOCATION)
    public void methodRequiresThreePermission() {
        if (EasyPermissions.hasPermissions(this, perms)) {
            // Already have permission, do the thing
            // ...
            afterPermission();
        } else {
            // Do not have permissions, request them now
            EasyPermissions.requestPermissions(this, "不同意权限将无法使用程序",
                    RC_CAMERA_FILE_LOCATION, perms);
        }
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {

    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        Log.d(TAG, "onPermissionsDenied:" + requestCode + ":" + perms.size());
        if (perms.size() > 0) {
            methodRequiresThreePermission();
        }

        // (Optional) Check whether the user denied any permissions and checked "NEVER ASK AGAIN."
        // This will display a dialog directing them to enable the permission in app settings.
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this).build().show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == AppSettingsDialog.DEFAULT_SETTINGS_REQ_CODE) {
            // Do something after user returned from app settings screen, like showing a Toast.
            methodRequiresThreePermission();
//            Toast.makeText(this, "测试", Toast.LENGTH_SHORT)
//                    .show();
        }
    }
}
