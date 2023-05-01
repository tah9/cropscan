package com.nuist.cropscan.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * ->  tah9  2023/4/30 23:39
 */
public class NestedChildWebView extends WebView {
    private static final String TAG = "NestedChildWebView";

    public NestedChildWebView(@NonNull Context context) {
        super(context);
    }

    public NestedChildWebView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public NestedChildWebView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        Log.d(TAG, "dispatchTouchEvent: " + ev);
        return true;
    }
}
