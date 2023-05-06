package com.nuist.cropscan.base;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.nuist.cropscan.R;
import com.nuist.tool.screen.ScreenUtil;

public class BaseAct extends AppCompatActivity {
    public Context context = this;
    public static final String TAG = "BaseAct";
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;


    public void setString(String key, String value) {
        initSp();
        editor.putString(key, value).commit();
    }

    public String optString(String key) {
        initSp();
        String value = sharedPreferences.getString(key, "");
        Log.d(TAG, "setValue: " + key + "-" + value);
        return value;
    }

    public int optInt(String key) {
        initSp();
        return sharedPreferences.getInt(key, 0);
    }

    public void setInt(String key, int value) {
        initSp();
        Log.d(TAG, "setInt: " + key + " " + value);
        editor.putInt(key, value).commit();
    }

    public void clearSp() {
        initSp();
        int webVersion = optInt(getResources().getString(R.string.web_version));
        editor.clear().commit();
        setInt(getResources().getString(R.string.web_version), webVersion);
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ScreenUtil.setTranslateStatusBar(getWindow());
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//        //需要设置这个 flag 才能调用 setStatusBarColor 来设置状态栏颜色
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//        //设置状态栏颜色
//        getWindow().setStatusBarColor(Color.TRANSPARENT);

    }

    private void initSp() {
        if (sharedPreferences == null) {
            sharedPreferences = getSharedPreferences(getPackageName(), MODE_PRIVATE);
            editor = sharedPreferences.edit();
        }
    }

}
