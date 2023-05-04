package com.nuist.guide;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.nuist.cropscan.HomeAct;
import com.nuist.cropscan.R;
import com.nuist.cropscan.base.BaseAct;
import com.nuist.cropscan.dialog.SnackUtil;
import com.nuist.cropscan.request.HttpOk;
import com.nuist.cropscan.tool.AniUtils;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class Act_Login extends BaseAct implements EasyPermissions.PermissionCallbacks {
    private static final String TAG = "Act_Login";
    private MaterialButton btnLogin;
    private MaterialButton btnRegister;
    private TextInputEditText editEmail;
    private TextInputEditText p1;
    private TextInputEditText p2;

    private boolean beLogin = true;
    private int initViewAniDuration = 300;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_layout);
        requiresOwnerPermission();

        initView();

        btnLogin.setOnClickListener(v -> {
            if (editEmail.getEditableText().toString().equals("")
                    || p1.getEditableText().toString().equals("")
            ) {
                SnackUtil.showAutoDis(editEmail, "输入不能为空");
                return;
            }

            Map<String, Object> map = new HashMap<>();
            map.put("email", editEmail.getEditableText().toString());
            map.put("userPassword", p1.getEditableText().toString());
            if (beLogin) {
                HttpOk.getInstance().postToOwnerUrl(map, "/user/login", json -> {
                    if (json.optInt("code") == 200) {
                        SnackUtil.showAutoDis(editEmail, "登录成功");
                        JSONObject data = json.optJSONObject("data");
                        setString("user", data.toString());
                        setString("uid", data.optString("id"));
                        startActivity(new Intent(this, HomeAct.class));
                        finish();
                    } else if (json.optInt("code") == 40000) {
                        SnackUtil.showAutoDis(editEmail, json.optString("message"));
                    }
                });
            } else {
                String manufacturer = Build.MANUFACTURER;
                String model = Build.MODEL;
                int apiLevel = Build.VERSION.SDK_INT;
                String osVersion = Build.VERSION.RELEASE;
                Map<String, Object> deviceMap = new HashMap<>();
                deviceMap.put("Manufacturer: ", manufacturer);
                deviceMap.put("Model: ", model);
                deviceMap.put("API Level: ", apiLevel);
                deviceMap.put("OS Version: ", osVersion);
                //根据设备信息创建用户名
                map.put("userName", new JSONObject(deviceMap).toString());
                map.put("checkPassword", p2.getEditableText().toString());
                Log.d(TAG, ": " + new JSONObject(map).toString());
                HttpOk.getInstance().postToOwnerUrl(map, "/user/register", json -> {
                    Log.d(TAG, ": " + json);
                    if (json.optInt("code") == 200) {
                        SnackUtil.showAutoDis(editEmail, "注册成功");
                        showLogin();
                    } else if (json.optInt("code") == 40000) {
                        SnackUtil.showAutoDis(editEmail, json.optString("message"));
                    }
                });
            }

        });

        btnRegister.setOnClickListener(v -> {
            if (beLogin) {
                showRegister();
            } else {
                showLogin();
            }
        });
    }

    private void showRegister() {
        beLogin = false;
        int hei = p1.getHeight();

        p2.animate()
                .alpha(1f)
                .setDuration(initViewAniDuration);
        AniUtils.moveYAni(btnRegister, -hei, 0, initViewAniDuration);
        AniUtils.moveYAni(btnLogin, -hei, 0, initViewAniDuration).addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                btnRegister.setText("已有账号？立即登录");
                btnLogin.setText("注册");
            }
        });
    }

    private void showLogin() {
        beLogin = true;

        int hei = p1.getHeight();
        p2.animate()
                .alpha(0f)
                .setDuration(initViewAniDuration);

        AniUtils.moveYAni(btnRegister, 0, -hei, initViewAniDuration);
        AniUtils.moveYAni(btnLogin, 0, -hei, initViewAniDuration).addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                btnRegister.setText("没有账号？立即注册");
                btnLogin.setText("登录");
            }
        });
    }

    private void initView() {
        btnLogin = (MaterialButton) findViewById(R.id.btn_login);
        btnRegister = (MaterialButton) findViewById(R.id.btn_register);
        editEmail = (TextInputEditText) findViewById(R.id.edit_email);
        p1 = (TextInputEditText) findViewById(R.id.p1);
        p2 = (TextInputEditText) findViewById(R.id.p2);
        p1.post(() -> {
            int hei = p1.getHeight();
            btnLogin.setTranslationY(-hei);
            btnRegister.setTranslationY(-hei);
        });
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
    public void requiresOwnerPermission() {
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
            requiresOwnerPermission();
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
            requiresOwnerPermission();
//            Toast.makeText(this, "测试", Toast.LENGTH_SHORT)
//                    .show();
        }
    }
}
