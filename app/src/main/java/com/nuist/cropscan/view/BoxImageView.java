package com.nuist.cropscan.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.nuist.cropscan.view.entiry.TRect;

import java.util.ArrayList;
import java.util.List;

/**
 * ->  tah9  2023/4/26 22:06
 */
public class BoxImageView extends ViewGroup {

    private Bitmap tempBitmap;
    private Canvas tempCanvas;

    public BoxImageView(@NonNull Context context) {
        super(context);
        init();
    }

    public BoxImageView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BoxImageView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }


    private final float fontSize = 40f;
    private final String fontColor = "#000000";
    private final float fontWidth = 2f;

    private final String boxColor = "#ffffff";
    private final float boxLineWidth = 6f;

    //    Rect rect;
    Paint paint, textPaint, clipPaint;
    Canvas canvas;

    List<TRect> rectList = new ArrayList<>();
    Bitmap sourceBitmap;

    private int activateIndex = -1;

    private void init() {
        setClipChildren(false);

        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.parseColor(boxColor));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(boxLineWidth);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.parseColor(fontColor));
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setStrokeWidth(fontWidth);
        textPaint.setTextSize(fontSize);


        clipPaint = new Paint();
        clipPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
    }

    @Override
    protected void onLayout(boolean b, int i, int i1, int i2, int i3) {
        for (int j = 0; j < getChildCount(); j++) {
            View child = getChildAt(j);
            Rect rect = rectList.get(j).getRect();
            child.layout(rect.left, rect.top, rect.left + rect.width(), rect.top + rect.height());
        }
    }

    public void setMask(Bitmap bitmap) {
        this.sourceBitmap = bitmap;

        tempBitmap = sourceBitmap.copy(Bitmap.Config.ARGB_8888, true);
        tempCanvas = new Canvas(tempBitmap);
        //绘制半透黑遮罩
        tempCanvas.drawColor(Color.parseColor("#70000000"));

        setBackground(new BitmapDrawable(getResources(),bitmap));
    }

    public void release() {
        for (TRect tRect : rectList) {
            tRect.release();
        }
        rectList.clear();
        removeAllViews();
        removeAllViewsInLayout();
        setBackground(null);
    }



    public void updateDraw(List<TRect> rectList) {
        this.rectList = rectList;
        for (TRect tRect : rectList) {
            BoxView boxView = new BoxView(getContext(), tRect);
            addView(boxView);
        }
    }


    @Override
    protected void onDraw(Canvas canvas) {
        if (rectList == null || rectList.size() == 0) {
            return;
        }

        for (TRect tRect : rectList) {
            Rect rect = tRect.getRect();
            //绘制矩形线框
            //手绘偏移
//            paint.setPathEffect(new DiscretePathEffect(50, 10));
            //清除区域遮罩
            tempCanvas.drawRect(rect, clipPaint);

//            if (tRect.getName() != null) {
//                paint.setStyle(Paint.Style.FILL);
//                canvas.drawRect(rect.left + boxLineWidth / 2,
//                        rect.top + boxLineWidth / 2,
//                        rect.left + tRect.getName().length() * fontSize + fontSize,
//                        rect.top + fontSize + boxLineWidth,
//                        paint);
//                paint.setStyle(Paint.Style.STROKE);
//                Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();
//                float fontH = fontMetrics.bottom - fontMetrics.top;
//                canvas.drawText(tRect.getName(), rect.left + fontSize / 2,
//                        rect.top + fontH / 2 + boxLineWidth, textPaint);
//            }
        }
        canvas.drawBitmap(tempBitmap, 0, 0, null);
    }


//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        Log.d(TAG, "onTouchEvent: " + event.toString());
//
//        return true;
//    }

    private static final String TAG = "BoxImageView";

}
