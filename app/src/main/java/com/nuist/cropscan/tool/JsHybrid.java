package com.nuist.cropscan.tool;

import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * ->  tah9  2023/4/22 15:12
 */
public class JsHybrid {
    private static final String TAG = "JsHybrid";

    @JavascriptInterface
    public static void getLocalStorage(String user) {
        Log.d(TAG, "getLocalStorage: " + user);
    }
}
