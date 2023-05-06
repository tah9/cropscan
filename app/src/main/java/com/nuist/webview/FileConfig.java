package com.nuist.webview;

import android.content.Context;

/**
 * ->  tah9  2023/4/22 10:44
 */
public class FileConfig {
    public static String webFileSavePath(Context context) {
        return context.getFilesDir().getAbsolutePath() + "/dist";
    }

    public static String webFileUrlHome(Context context) {
        return "file://" + webFileSavePath(context) + "/index.html";
    }
}
