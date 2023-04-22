package com.nuist.cropscan.request;

import android.content.Context;

import java.io.File;

/**
 * ->  tah9  2023/4/22 10:44
 */
public class FileConfig {
    public static String webFilePath(Context context) {
        return context.getFilesDir().getAbsolutePath() + "/dist";
    }

    public static String webFileHome(Context context) {
        return webFilePath(context) + "/index.html";
    }
}
