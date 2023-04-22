package com.nuist.cropscan.base;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.nuist.cropscan.ActWeb;
import com.nuist.cropscan.HomeAct;
import com.nuist.cropscan.R;
import com.nuist.cropscan.scan.ActCropScan;

/**
 * ->  tah9  2023/4/18 8:10
 */
public class FragWeb extends BaseFrag {
    private static final String TAG = "WebFrag";

    public String url;
    public String color;

    @Override
    public void onResume() {
        super.onResume();
    }

    public FragWeb(String url) {
        this.url = url;
        Log.d(TAG, "WebFrag: url > " + url);
    }

    public FragWeb(String url, String color) {
        this.url = url;
        this.color = color;
    }

    WebView webView;

    public WebView getWebView() {
        return webView;
    }

    //js调用安卓，必须加@JavascriptInterface注释的方法才可以被js调用
    @JavascriptInterface
    public void toHomeAct(String user) {
        Log.d(TAG, "toHomeAct: ");
        setString("user", user);
        startActivity(new Intent(getActivity(), HomeAct.class));
    } //js调用安卓，必须加@JavascriptInterface注释的方法才可以被js调用

    @JavascriptInterface
    public String getValue(String key) {
        Log.d(TAG, "getValue: " + key);
        return optString(key);
    }

    @JavascriptInterface
    public void setValue(String key, String value) {
        Log.d(TAG, "setValue: " + key + "-" + value);
        setString(key, value);
    }

    @JavascriptInterface
    public void toCapture(String uid) {
        startActivity(new Intent(getActivity(), ActCropScan.class).putExtra("uid",uid));
    }

    @JavascriptInterface
    public void loadUrl(String url) {
        Log.d(TAG, "loadUrl: " + url);
        webView.post(() -> {
            webView.loadUrl(url);
            Log.d(TAG, "after loadUrl: " + url);
        });
    }


    @JavascriptInterface
    public void toggleUser() {
        Log.d(TAG, "toggleUser: ");
        setString("user", "");
        startActivity(new Intent(getActivity(), ActWeb.class));
        getActivity().finish();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_web, null);
        if (color != null) {
            view.setBackgroundColor(Color.parseColor(color));
        }
        webView = view.findViewById(R.id.webView);
        setWebView();
        if (url != null) {
            webView.loadUrl(url);
        }
        return view;
    }

    @SuppressLint("JavascriptInterface")
    private void setWebView() {
        WebSettings settings = webView.getSettings();
        settings.setDomStorageEnabled(true);
        settings.setJavaScriptEnabled(true);
        //允许跨域
        settings.setAllowUniversalAccessFromFileURLs(true);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setAllowFileAccess(true);
        webView.addJavascriptInterface(this, "androidMethods");
    }
}
