package com.nuist.cropscan.ActPicture;

import android.content.Context;
import android.os.Environment;

/**
 * ->  tah9  2023/5/4 14:22
 */
public class GalleryConfig {
    public static String rootPath(Context context) {
        return Environment.getExternalStorageDirectory().getParentFile().getAbsolutePath();
    }
}
