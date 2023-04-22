package com.nuist.cropscan.tool;

import android.graphics.Bitmap;
import android.graphics.Matrix;

/**
 * ->  tah9  2023/3/24 16:21
 */
public class BitmapTool {
    public static Bitmap zoomBit(Bitmap.Config config, Bitmap bitmap, int size) {
        float scale;
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        // 设置想要的大小
        if (width >= height) {
            scale = (float) size / (float) width;
        } else {
            scale = (float) size / (float) height;
        }
        System.out.println("scale" + scale);

        // 取得想要缩放的matrix参数
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);
        try {
            Bitmap bit = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
            if (bit != bitmap)
                return bit;
            else
                return bitmap.copy(config, true);
        } catch (OutOfMemoryError e) {
            return bitmap.copy(config, true);
        }
    }

    //图片缩放
    public static Bitmap zoomBit(Bitmap bitmap, int size) {
        float scale;
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        // 设置想要的大小
        if (width >= height) {
            scale = (float) size / (float) width;
        } else {
            scale = (float) size / (float) height;
        }
        System.out.println("scale" + scale);

        // 取得想要缩放的matrix参数
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);
        try {
            Bitmap bit = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
            if (bit != bitmap) {
                bitmap.recycle();
                return bit;
            } else {
                return bitmap.copy(Bitmap.Config.ARGB_8888, true);
            }
        } catch (OutOfMemoryError e) {
            return bitmap.copy(Bitmap.Config.ARGB_8888, true);
        }
    }
}
