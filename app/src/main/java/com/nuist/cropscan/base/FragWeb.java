package com.nuist.cropscan.base;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import com.nuist.cropscan.R;
import com.nuist.cropscan.scan.ActCropScan;
import com.nuist.cropscan.dialog.LoadingDialogUtils;

import org.json.JSONObject;

/**
 * ->  tah9  2023/4/18 8:10
 */
public class FragWeb extends BaseFrag {
    private static final String TAG = "WebFrag";

    public String url;
    public String color;


    public FragWeb(String url) {
        this.url = url;
        Log.d(TAG, "WebFrag: url > " + url);
    }

    public FragWeb(String url, String color) {
        this.url = url;
        this.color = color;
    }

    public static WebView webView;

    public static WebView getWebView() {
        return webView;
    }


    @Override
    public void onResume() {
        super.onResume();
        String resultPlant = optString(getResources().getString(R.string.record));
        if (!resultPlant.isEmpty()) {
            webView.loadUrl("javascript:toResultInfo()");
        }
    }

    @JavascriptInterface
    public void evalFinish(String jsonStr) throws Exception {
        Log.d(TAG, "evalFinish: " + jsonStr);
        JSONObject o = new JSONObject(jsonStr);
        LoadingDialogUtils.dismiss();
//        if (o.optInt("acc") == 1) {
        setString(getResources().getString(R.string.record), o.optString("record"));
        Intent intent = new Intent(getActivity(), ActWeb.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
//            finish();
//        } else {
//            runOnUiThread(() -> {
//                Toast.makeText(context, "检测失败", Toast.LENGTH_SHORT).show();
//                picMask.setImageBitmap(null);
//            });
//        }
    }

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
    public void loadUrl(String url) {
        Log.d(TAG, "loadUrl: " + url);
        webView.post(() -> {
            webView.loadUrl(url);
            Log.d(TAG, "after loadUrl: " + url);
        });
    }


    @JavascriptInterface
    public void toCapture() {
        Log.d(TAG, "toCapture: ");
        startActivity(new Intent(context, ActCropScan.class));
    }

    @JavascriptInterface
    public void toggleUser() {
        Log.d(TAG, "toggleUser: ");
        getBaseAct().clearSp();
    }

    @JavascriptInterface
    public String getVersion() {
        Log.d(TAG, "getVersion: ");
        try {
            String versionName = context.getPackageManager().
                    getPackageInfo(context.getPackageName(), 0).versionName;
            String result = versionName + "." + getBaseAct().optInt(getResources().getString(R.string.web_version));
            Log.d(TAG, "getVersion: " + result);
            return result;
        } catch (PackageManager.NameNotFoundException e) {
            return "4.4.4";
        }
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
//        settings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setAllowFileAccess(true);
        webView.addJavascriptInterface(this, "androidMethods");
    }
}
