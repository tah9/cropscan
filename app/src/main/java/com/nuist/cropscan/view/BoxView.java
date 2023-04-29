package com.nuist.cropscan.view;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.nuist.cropscan.R;
import com.nuist.cropscan.view.entiry.TRect;

/**
 * ->  tah9  2023/4/28 22:46
 */
@SuppressLint("ViewConstructor")
public class BoxView extends FrameLayout {
    private TRect rect;
    Paint paint;
    private final String boxColor = "#ffffff";
    private final float boxLineWidth = 6f;

    private final int iconWh = 130;
    private static final String TAG = "BoxView";

    private final int aniDuration=1000;
    private ImageView imageView;

    public BoxView(Context context, TRect rect) {
        super(context);
        this.rect = rect;
        init();
        Log.d(TAG, "BoxView: ");
    }


    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        Rect rec = rect.getRect();
        int l = ((int) ((rec.width() - iconWh) / 2f));
        int t = ((int) ((rec.height() - iconWh) / 2f));
        getChildAt(0).layout(l, t, l + iconWh, t + iconWh);
    }

    private void init() {
        setClipChildren(false);

        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.parseColor(boxColor));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(boxLineWidth);

        imageView = new ImageView(getContext());
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setImageBitmap(getLoadBitmap());
        addView(imageView);

//        startLoadAni();
    }
    private ObjectAnimator startLoadAni(){
        ObjectAnimator animator = ObjectAnimator.ofFloat(imageView, "rotation", 0f, 360f);
        animator.setDuration(aniDuration);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setInterpolator(new LinearInterpolator());
        animator.start();
        return animator;
    }

    private Bitmap getIconBitmap() {
        Bitmap bitmap = Bitmap.createBitmap(iconWh, iconWh, Bitmap.Config.ARGB_8888);
        // 创建一个Canvas对象，并将其与Bitmap关联
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.TRANSPARENT);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        //外圈半透明圆
        paint.setColor(Color.parseColor("#4AFFFFFF"));
        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(iconWh / 2f, iconWh / 2f, iconWh / 2f, paint);
        paint.setColor(Color.WHITE);
        canvas.drawCircle(iconWh / 2f, iconWh / 2f, iconWh / 4f, paint);
        return bitmap;
    }

    private Bitmap getLoadBitmap() {
        Bitmap res = BitmapFactory.decodeResource(getResources(), R.drawable.crop_load);
        float scale = 1.2f;
        int iconWh = ((int) (res.getWidth() * scale));
        Log.d(TAG, "getLoadBitmap: " + res);
        Bitmap bitmap = Bitmap.createBitmap(iconWh, iconWh, Bitmap.Config.ARGB_8888);
        // 创建一个Canvas对象，并将其与Bitmap关联
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.TRANSPARENT);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        //外圈半透明圆
        paint.setColor(Color.parseColor("#4AFFFFFF"));
        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(iconWh / 2f, iconWh / 2f, iconWh / 2f, paint);
        paint.setColor(Color.WHITE);
        canvas.drawBitmap(res, (iconWh-res.getWidth()) / 2f, (iconWh-res.getWidth()) / 2f, null);
        return bitmap;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
//        canvas.drawRect(rect.getRect(), paint);
    }
}
