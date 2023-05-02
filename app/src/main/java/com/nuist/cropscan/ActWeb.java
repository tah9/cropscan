package com.nuist.cropscan;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.webkit.WebView;
import android.widget.Toast;

import androidx.fragment.app.FragmentTransaction;

import com.nuist.cropscan.base.BaseAct;
import com.nuist.cropscan.base.FragWeb;
import com.nuist.cropscan.dialog.DownLoadDialog;
import com.nuist.cropscan.request.BASEURL;
import com.nuist.cropscan.request.FileConfig;
import com.nuist.cropscan.request.HttpOk;
import com.nuist.cropscan.tool.FileUtils;
import com.nuist.cropscan.dialog.LoadingDialogUtils;
import com.nuist.cropscan.tool.LocalGps;
import com.nuist.cropscan.tool.ZipUtils;

import org.json.JSONObject;

import java.io.File;

public class ActWeb extends BaseAct {
    FragWeb fragWeb;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.framelayout);
        new LocalGps(this);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragWeb = new FragWeb(BASEURL.entireWebHost + "/#/main/home/" + optString("plant"));
        fragmentTransaction.replace(R.id.frame, fragWeb).commit();
    }


    // 第一次按下返回键的事件
    private long firstPressedTime;

    @Override
    public void onBackPressed() {
        WebView webView = fragWeb.getWebView();
        String url = webView.getUrl();
        Log.d(TAG, "onBackPressed: " + url);
        if (url.equals("file://" + FileConfig.webFileHome(context) + "#/home")
                || url.equals("file://" + FileConfig.webFileHome(context) + "#/login")
                || url.equals(BASEURL.entireWebHost + "/#/home")
                || url.equals(BASEURL.entireWebHost + "/#/login")

        ) {
            Log.d(TAG, "onBackPressed: 符合");
            if (System.currentTimeMillis() - firstPressedTime < 2000) {
                super.onBackPressed();
            } else {
                Toast.makeText(ActWeb.this, "再按一次退出", Toast.LENGTH_SHORT).show();
                firstPressedTime = System.currentTimeMillis();
            }
        } else {
            finish();
//            webView.loadUrl("javascript:backPressed()");
        }
    }
}