package com.nuist.tool.file;

import android.util.Log;

import java.io.File;

/**
 * ->  tah9  2023/4/22 11:00
 */
public class FileUtils {
    private static final String TAG = "FileUtils";
    public static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir
                        (new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        if (dir.delete()) {
            Log.d(TAG, "deleteDir: success"+dir.getName());
            return true;
        } else {
            Log.d(TAG, "deleteDir: fail"+dir.getName());
            return false;
        }
    }
}
