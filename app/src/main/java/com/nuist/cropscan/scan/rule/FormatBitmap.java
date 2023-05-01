package com.nuist.cropscan.scan.rule;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;

import com.nuist.cropscan.tool.Tools;

/**
 * ->  tah9  2023/4/30 15:50
 */
public class FormatBitmap {
    public static Bitmap format(Context context, Bitmap bitmap) {
            /*
            若是bitmap和现有不符合，调整
             */
        int scrWid = Tools.getWidth(context);
        int scrHei = Tools.fullScreenHeight(context);


        //现拍的bitmap尺寸正好
        if (bitmap.getWidth() == scrWid && bitmap.getHeight() == scrHei) {

        } else {
            float scale = (float) scrWid / bitmap.getWidth();
            int newHei = (int) (bitmap.getHeight() * scale);

            //以宽为基准缩放
            Matrix matrix = new Matrix();
            matrix.postScale(scale, scale);
            bitmap = Bitmap.createBitmap(bitmap,
                    0, 0, bitmap.getWidth(), bitmap.getHeight(),
                    matrix, true);
            if (newHei < scrHei) {
                Bitmap bgBit = Bitmap.createBitmap(scrWid, scrHei, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bgBit);
                canvas.drawColor(Color.BLACK);
                canvas.drawBitmap(bitmap, 0, Math.abs(newHei - scrHei) / 2f, null);
                bitmap = bgBit;
            }
            //长图截取中间
            else {
                bitmap = Bitmap.createBitmap(bitmap, 0, Math.abs(newHei - scrHei) / 2, scrWid, scrHei);
            }
        }
        //去除部分顶部和底部，只保留中间的bitmap
        bitmap = Bitmap.createBitmap(bitmap, 0, ((int) (bitmap.getHeight() * CropConfig.CropViewClipTopScale)),
                bitmap.getWidth(), ((int) (bitmap.getHeight() * CropConfig.CropViewHeightScale)));
        return bitmap;
    }
}
