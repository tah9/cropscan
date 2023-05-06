package com.nuist.cropscan;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

import com.nuist.gallery.GalleryConfig;

import java.io.File;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    private static final String TAG = "ExampleInstrumentedTest";
    @Test
    public void testFile(){
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("com.nuist.cropscan", appContext.getPackageName());
//        String rootPath = GalleryConfig.rootPath(appContext);
        File directory = Environment.getStorageDirectory();
        Log.d(TAG, "useAppContext: "+directory.getPath());
        String[] files = directory.list();
        for (String file : files) {
            Log.d(TAG, "useAppContext: "+file);
        }
    }
    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("com.nuist.cropscan", appContext.getPackageName());
        String rootPath = GalleryConfig.rootPath();
        File directory = Environment.getExternalStorageDirectory();
        Log.d(TAG, "useAppContext: "+directory.getPath());
        File[] files = directory.listFiles();
        for (File file : files) {
            Log.d(TAG, "useAppContext: "+file.getName());
        }
//        String[] list = new File(rootPath).list();
//        for (String s : list) {
//            Log.d(TAG, "useAppContext: "+s);
//        }
//        for (File file : new File(rootPath).listFiles()) {
//            Log.d(TAG, "useAppContext: "+file.getAbsolutePath());
//        }
        Log.d(TAG, "useAppContext: " + rootPath);
    }
}