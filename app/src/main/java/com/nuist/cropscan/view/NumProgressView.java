package com.nuist.cropscan.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class NumProgressView extends View {

    /**
     * 进度条画笔的宽度(dp)
     */

    private int paintProgressWidth = 3;

    /**
     * 文字百分比的字体大小(sp)
     */

    private int paintTextSize = 10;

    /**
     * 左侧已完成进度条的颜色
     */

    private int paintLeftColor = 0xff112b28;

    /**
     * 右侧未完成进度条的颜色
     */

    private int paintRightColor = 0xffaaaaaa;

    /**
     * 百分比文字的颜色
     */

    private int paintTextColor = 0xff000000;

    /**
     * Contxt
     */

    private Context context;

    /**
     * 主线程传过来进程 0 - 100
     */

    private int progress;

    /**
     * 得到自定义视图的宽度
     */

    private int viewWidth;

    /**
     * 得到自定义视图的Y轴中心点
     */

    private int viewCenterY;

    /*
    进度条起点，留一部分空间给线首的圆形
     */
    private int startX = paintTextSize;

    /**
     * 画左边已完成进度条的画笔
     */

    private Paint paintLeft = new Paint();

    /**
     * 画右边未完成进度条的画笔
     */

    private Paint paintRight = new Paint();

    /**
     * 画中间的百分比文字的画笔
     */

    private Paint paintText = new Paint();


    /**
     * 画文字时底部的坐标
     */


    /**
     * 包裹文字的矩形
     */


    /**
     * 文字总共移动的长度(即从0%到100%文字左侧移动的长度)
     */

    private int paintProgressWidthPx;
    private float baseline;

    public NumProgressView(Context context, AttributeSet attrs) {

        super(context, attrs);

        this.context = context;

// 构造器中初始化数据

        initData();

    }


    /**
     * 将dip或dp值转换为px值，保证尺寸大小不变
     */

    public int dip2px(Context context, float dipValue) {

        final float scale = context.getResources().getDisplayMetrics().density;

        return (int) (dipValue * scale + 0.5f);

    }

    /**
     * 将sp值转换为px值，保证文字大小不变
     */

    public int sp2px(Context context, float spValue) {

        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;

        return (int) (spValue * fontScale + 0.5f);

    }

    /**
     * 初始化数据
     */

    private void initData() {

//设置进度条画笔的宽度

        paintProgressWidthPx = dip2px(context, paintProgressWidth);

//设置百分比文字的尺寸

        int paintTextSizePx = sp2px(context, paintTextSize);

// 已完成进度条画笔的属性

        paintLeft.setColor(paintLeftColor);

        paintLeft.setStrokeWidth(paintProgressWidthPx);

        paintLeft.setAntiAlias(true);

        paintLeft.setStyle(Paint.Style.FILL);
        //cap 设置起终点圆角
        paintLeft.setStrokeCap(Paint.Cap.ROUND);

// 未完成进度条画笔的属性

        paintRight.setColor(paintRightColor);

        paintRight.setStrokeWidth(paintProgressWidthPx);

        paintRight.setAntiAlias(true);

        paintRight.setStyle(Paint.Style.FILL);

// 百分比文字画笔的属性

        paintText.setColor(paintTextColor);

        paintText.setTextSize(paintTextSizePx);

        paintText.setAntiAlias(true);

        paintText.setTypeface(Typeface.DEFAULT_BOLD);

    }


    @Override

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        getMeasure();

    }

    /**
     * 得到视图等的高度宽度尺寸数据
     */

    private void getMeasure() {
        if (viewWidth != 0) {
            return;
        }

        //得到自定义视图的高度

        int viewHeight = getMeasuredHeight();

        viewWidth = getMeasuredWidth();

        viewCenterY = viewHeight / 2;


        /*
        获取基线高度
         */
        Paint.FontMetrics fontMetrics = paintText.getFontMetrics();
        Log.d(TAG, "fontMetrics.ascent: " + fontMetrics.ascent);
        Log.d(TAG, "fontMetrics.descent: " + fontMetrics.descent);
        baseline = viewCenterY - (fontMetrics.ascent + fontMetrics.descent) / 2;

    }

    private static final String TAG = "NumProgressView";

    @Override

    protected void onDraw(Canvas canvas) {

        super.onDraw(canvas);


//得到float型进度

        float progressFloat = progress / 100.0f;

//当前文字移动的长度
        float textWidth = paintText.measureText(progress + "%");

        float currentMovedLength = (viewWidth - textWidth - startX) * progressFloat;

//画左侧已经完成的进度条，长度为从Veiw左端到文字的左侧

        canvas.drawLine(startX, viewCenterY, currentMovedLength, viewCenterY, paintLeft);
//画右侧未完成的进度条


        canvas.drawLine(startX + currentMovedLength + textWidth, viewCenterY, viewWidth, viewCenterY, paintRight);


//画文字(注意：文字要最后画，因为文字和进度条可能会有重合部分，所以要最后画文字，用文字盖住重合的部分)

        canvas.drawText(progress + "%", currentMovedLength + paintTextSize, baseline, paintText);


    }


    /**
     * @param progress 外部传进来的当前进度
     */

    public void setProgress(int progress) {

        this.progress = progress;

        invalidate();

    }

}


    