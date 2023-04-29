package com.nuist.cropscan.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.util.Log;
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
public class ScanLayout extends ViewGroup {

    private Bitmap tempBitmap;
    private Canvas tempCanvas;

    public ScanLayout(@NonNull Context context) {
        super(context);
        init();
    }

    public ScanLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ScanLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private final int cornerRadius = 25;

    private final float fontSize = 40f;
    private final String fontColor = "#000000";
    private final float fontWidth = 2f;

    private final String boxColor = "#ffffff";
    private final float boxLineWidth = 8f;
    private final int MaskColor = Color.parseColor("#99000000");

    //    Rect rect;
    Paint paint, textPaint, clipPaint;

    List<TRect> rectList = new ArrayList<>();
    Bitmap sourceBitmap;

    private int activateIndex = 0;

    public int getActivateIndex() {
        return activateIndex;
    }

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

    public BoxView getBoxViewAt(int index) {
        return ((BoxView) getChildAt(index));
    }

    public void setMask(Bitmap bitmap) {
        this.sourceBitmap = bitmap;
        setBackground(new BitmapDrawable(getResources(), bitmap));
    }

    public void release() {
        for (TRect tRect : rectList) {
            tRect.release();
        }
        for (int i = 0; i < getChildCount(); i++) {
            getBoxViewAt(i).release();
        }
        rectList.clear();
        removeAllViews();
        removeAllViewsInLayout();
        setBackground(null);
    }

    public interface TargetClickListener {
        void targetClick(int position);
    }

    private TargetClickListener targetClickListener;

    public void setTargetClickListener(TargetClickListener listener) {
        this.targetClickListener = listener;
    }

    public void setList(List<TRect> rectList) {
        this.rectList = rectList;
        for (int i = 0; i < rectList.size(); i++) {
            TRect tRect = rectList.get(i);
            BoxView boxView = new BoxView(getContext(), tRect);
            int finalI = i;
            //监听图片点击
            boxView.getImageView().setOnClickListener(view -> {

                //更新全局下标
                this.activateIndex = finalI;
                invalidate();
                //监听回调
                targetClickListener.targetClick(finalI);

                //更新view
                openChildAndUpdateState(finalI);
            });
            addView(boxView);
        }
        Log.d(TAG, "setList: " + getChildAt(0));
        //默认自动识别首个
        getBoxViewAt(0).getImageView().performClick();
    }

    private int preIndex = -1;

    private void openChildAndUpdateState(int position) {
        getBoxViewAt(position).setBeOpen(true);
        getBoxViewAt(position).updateState();

        if (preIndex != -1) {
            getBoxViewAt(preIndex).setBeOpen(false);
            getBoxViewAt(preIndex).updateState();

        }
        preIndex = position;
    }

    public void notifyItemLoadEnd(int position, TRect tRect) {
        rectList.set(position, tRect);
        getBoxViewAt(position).setLoad(false);
        getBoxViewAt(position).updateState();
    }


    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        if (rectList == null || rectList.size() == 0) {
            return;
        }

        tempBitmap = sourceBitmap.copy(Bitmap.Config.ARGB_8888, true);
        tempCanvas = new Canvas(tempBitmap);
        //绘制半透黑遮罩
        tempCanvas.drawColor(MaskColor);

        //绘制当前目标框
        Rect rec = rectList.get(activateIndex).getRect();
        RectF recF = new RectF(rec.left, rec.top, rec.right, rec.bottom);
        canvas.drawRoundRect(recF, cornerRadius, cornerRadius, paint);

        //清除区域遮罩
        tempCanvas.drawRoundRect(recF, cornerRadius, cornerRadius, clipPaint);

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
