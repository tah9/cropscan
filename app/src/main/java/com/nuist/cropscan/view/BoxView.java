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
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.nuist.cropscan.R;
import com.nuist.cropscan.scan.rule.CropConfig;
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

    private final int iconWh = CropConfig.IconWH;
    private static final String TAG = "BoxView";

    private final int loadAniDuration = 1000;
    private final int initViewAniDuration = 250;

    private boolean beOpen = false;

    public void setBeOpen(boolean beOpen) {
        this.beOpen = beOpen;
    }

    public boolean isBeOpen() {
        return beOpen;
    }

    private boolean load = false;

    public boolean isLoad() {
        return load;
    }

    public void setLoad(boolean load) {
        this.load = load;
    }

    private ImageView imageView;

    public BoxView(Context context, TRect rect) {
        super(context);
        this.rect = rect;
        init();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (getChildCount() == 0) {
            return;
        }
        Rect rec = rect.getRect();
        int l = ((int) ((rec.width() - iconWh) / 2f));
        int t = ((int) ((rec.height() - iconWh) / 2f));
        getChildAt(0).layout(l, t, l + iconWh, t + iconWh);
    }

    public ImageView getImageView() {
        return imageView;
    }


    private void init() {
        setClipChildren(false);

        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.parseColor(boxColor));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(boxLineWidth);

        imageView = new ImageView(getContext());
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        addView(imageView);

        iconBitmap = getIconBitmap();
        loadBitmap = getLoadBitmap();

        updateState();
    }


    /*
        按钮的几种状态：
        1.显示与隐藏关键点：
            -beOpen&&getName!=null
            是否是选中框纽并且加载完成，是则隐藏按钮并拦截事件，否均显示
        2.加载状态与默认状态关键点：
            -isLoad&&getName==null
            加载状态，其他均为默认状态
            -getName==null||getName!=null
            都是默认状态
     */
    public void updateState() {

        if (isBeOpen() && rect.getName() != null) {
            imageView.animate()
                    .scaleX(0f)
                    .scaleY(0f)
                    .setDuration(initViewAniDuration)
                    .withEndAction(() -> imageView.setVisibility(View.INVISIBLE));
            return;
        } else {
            imageView.setVisibility(VISIBLE);
        }

        if (isLoad() && rect.getName() == null) {
            imageView.setImageBitmap(loadBitmap);
            this.loadAni = startLoadAni();
        } else {
            //清除动画
            if (loadAni != null) {
                loadAni.cancel();
            }
            imageView.setImageBitmap(iconBitmap);
            imageView.setScaleX(0f);
            imageView.setScaleY(0f);
            imageView.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(initViewAniDuration);
        }
    }


    public void release() {
        this.loadBitmap.recycle();
        this.loadBitmap = null;
        this.iconBitmap.recycle();
        this.iconBitmap = null;
    }


    private ObjectAnimator loadAni = null;

    private Bitmap iconBitmap, loadBitmap;

    private ObjectAnimator startLoadAni() {
        ObjectAnimator animator = ObjectAnimator.ofFloat(imageView, "rotation", 0f, 360f);
        animator.setDuration(loadAniDuration);
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
        Bitmap bitmap = Bitmap.createBitmap(iconWh, iconWh, Bitmap.Config.ARGB_8888);
        // 创建一个Canvas对象，并将其与Bitmap关联
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.TRANSPARENT);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        //外圈半透明圆
        paint.setColor(Color.parseColor("#4A000000"));
        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(iconWh / 2f, iconWh / 2f, iconWh / 2f, paint);
        paint.setColor(Color.WHITE);
        canvas.drawBitmap(res, (iconWh - res.getWidth()) / 2f, (iconWh - res.getWidth()) / 2f, null);
        return bitmap;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
//        canvas.drawRect(rect.getRect(), paint);
    }
}
