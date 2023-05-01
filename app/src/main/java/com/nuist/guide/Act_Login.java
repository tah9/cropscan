package com.nuist.guide;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.nuist.cropscan.R;
import com.nuist.cropscan.base.BaseAct;
import com.nuist.cropscan.dialog.SnackUtil;
import com.nuist.cropscan.request.HttpOk;
import com.nuist.cropscan.tool.AniUtils;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Act_Login extends BaseAct {
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
        initView();

        btnLogin.setOnClickListener(v -> {
            if (editEmail.getEditableText().toString().equals("")
                    || p1.getEditableText().toString().equals("")
            ) {
                SnackUtil.show(this, "输入不能为空");
                return;
            }

            Map<String, Object> map = new HashMap<>();
            map.put("email", editEmail.getEditableText().toString());
            map.put("userPassword", p1.getEditableText().toString());
            if (beLogin) {
                HttpOk.getInstance().postToOwnerUrl(map, "/user/login", json -> {
                    if (json.optInt("code") == 200) {
                        SnackUtil.show(this, "登录成功");
                    } else if (json.optInt("code") == 40000) {
                        SnackUtil.show(this, json.optString("message"));
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
                        SnackUtil.show(this, "注册成功");
                        showLogin();
                    } else if (json.optInt("code") == 40000) {
                        SnackUtil.show(this, json.optString("message"));
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
}