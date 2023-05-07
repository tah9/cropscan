package com.nuist.webview;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.nuist.cropscan.R;
import com.nuist.cropscan.RippleView;
import com.nuist.cropscan.base.BaseFrag;
import com.nuist.cropscan.dialog.EvalDialog;
import com.nuist.request.BASEURL;
import com.nuist.cropscan.scan.ActCropScan;
import com.nuist.cropscan.scan.rule.FormatBitmap;
import com.nuist.tool.dialog.RippleDialog;
import com.nuist.tool.screen.Tools;
import com.nuist.guide.Act_Login;

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

    ActivityResultLauncher<Intent> intentActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        //此处是跳转的result回调方法
        if (result.getResultCode() == 101 && webView != null) {
            webView.loadUrl("javascript:window.location.reload(true)");
        }
    });


    @JavascriptInterface
    public String getValue(String key) {
        Log.d(TAG, "getValue: " + key);
        return optString(key);
    }


    /*
    js将图片原图链接传给安卓，安卓拿到链接后读取bitmap解析
     */
    @JavascriptInterface
    public void toEvalInfo(String url) {
        Log.d(TAG, "toEvalInfo: " + url);

        getBaseAct().runOnUiThread(() -> {
            EvalDialog evalDialog = new EvalDialog(context, getBaseAct());
            evalDialog.recordProcess(BASEURL.entireHost + url);
            evalDialog.setDismissListener(() -> {
//
            });
        });


    }

    @JavascriptInterface
    public void setValue(String key, String value) {
        Log.d(TAG, "setValue: " + key + "-" + value);
        setString(key, value);
    }


    @JavascriptInterface
    public void toHome() {
        Log.d(TAG, "toHome: ");
        getBaseAct().finish();
    }

    @JavascriptInterface
    public void toCapture() {
        Log.d(TAG, "toCapture: ");
        Intent intent = new Intent(context, ActCropScan.class);
        intentActivityResultLauncher.launch(intent);
    }

    @JavascriptInterface
    public void toggleUser() {
        Log.d(TAG, "toggleUser: ");
        getBaseAct().clearSp();
        startActivity(new Intent(getBaseAct(), Act_Login.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
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
        getBaseAct().setString("statusBarHeight", Tools.getCssStatusBarHeight(context)+"");
        View view = inflater.inflate(R.layout.frag_web, null);
        if (color != null) {
            view.setBackgroundColor(Color.parseColor(color));
        }
        webView = view.findViewById(R.id.webView);
        setWebView();
        if (url != null) {
            webView.loadUrl(url);
//            new Handler(Looper.getMainLooper()).postDelayed(()->{
//
//                webView.loadUrl(FileConfig.webFileHome(context)+"#/main/home/" + optString("plant"));
//            },2000);
        }
        return view;
    }

    @SuppressLint("JavascriptInterface")
    private void setWebView() {
        WebSettings settings = webView.getSettings();
        //禁止调用外部浏览器
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
            }
        });
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
