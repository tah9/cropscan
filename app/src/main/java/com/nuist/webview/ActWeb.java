package com.nuist.webview;

import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.widget.Toast;

import androidx.fragment.app.FragmentTransaction;

import com.nuist.cropscan.R;
import com.nuist.cropscan.base.BaseAct;
import com.nuist.request.BASEURL;
import com.nuist.tool.sensor.LocalGps;

public class ActWeb extends BaseAct {
    FragWeb fragWeb;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.framelayout);
        new LocalGps(this);

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        String url = FileConfig.webFileUrlHome(context)+"#/main/home/"+optString("plant");
        fragWeb = new FragWeb(url);
        fragmentTransaction.replace(R.id.frame, fragWeb).commit();
    }


    // 第一次按下返回键的事件
    private long firstPressedTime;

    @Override
    public void onBackPressed() {
        WebView webView = fragWeb.getWebView();
        String url = webView.getUrl();
        Log.d(TAG, "onBackPressed: " + url);
        if (url.equals(FileConfig.webFileUrlHome(context) + "#/home")
                || url.equals(FileConfig.webFileUrlHome(context) + "#/login")
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