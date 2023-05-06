package com.nuist.tool.img;

import android.graphics.Bitmap;
import android.graphics.Matrix;

import java.util.Random;

/**
 * ->  tah9  2023/3/24 16:21
 */
public class BitmapUtil {
    public static Bitmap blur(Bitmap bitmap, int radius) {
        return  Fast.doBlur(bitmap, radius, false);
    }

    public static Bitmap frostedGlass(Bitmap bitmap, double radius) {
        Bitmap copy = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_4444);
        Random rand = new Random();
        int[] srcPixels = new int[copy.getWidth() * copy.getHeight()];
        int w, h, woff, hoff;
        for (w = 0; w < bitmap.getWidth(); w++) {
            for (h = 0; h < bitmap.getHeight(); h++) {
                do {
                    woff = rand.nextInt((int) (2 * radius + 1)) - (int) radius;
                    hoff = rand.nextInt((int) (2 * radius + 1)) - (int) radius;
                } while (w + woff < 0 || h + hoff < 0 || w + woff >= copy.getWidth()
                        || h + hoff >= copy.getHeight());
                try {
                    srcPixels[w + h * copy.getWidth()] = bitmap.getPixel(w + woff, h + hoff);
                } catch (Exception e) {
                }
            }
        }
        copy.setPixels(srcPixels, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        return copy;
    }

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
