package com.nuist.cropscan.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONObject;

/**
 * ->  tah9  2023/4/26 22:06
 */
public class BoxImageView extends androidx.appcompat.widget.AppCompatImageView {
    public BoxImageView(@NonNull Context context) {
        super(context);
    }

    public BoxImageView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public BoxImageView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    Rect rect;
    Paint paint;

    public void drawRect(JSONObject o) {
        int left = o.optInt("left");
        int top = o.optInt("top");
        int width = o.optInt("width");
        int height = o.optInt("height");
        rect = new Rect(left,
                top,
                width + left, height + top);
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.parseColor("#ffffff"));
//        paint.setColor(Color.GREEN);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(6f);
//        paint.setAlpha(180);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (rect != null) {
            Log.d(TAG, "onDraw: ");
            canvas.drawRect(rect, paint);
        }
    }

    private static final String TAG = "BoxImageView";
}
