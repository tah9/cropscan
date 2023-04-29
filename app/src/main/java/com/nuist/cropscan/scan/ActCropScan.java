package com.nuist.cropscan.scan;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.AppBarLayout;
import com.nuist.cropscan.ActPicture.ActCameraX;
import com.nuist.cropscan.R;
import com.nuist.cropscan.dialog.CropTipsDialog;
import com.nuist.cropscan.dialog.LoadingDialogUtils;
import com.nuist.cropscan.request.FileConfig;
import com.nuist.cropscan.tool.LocalGps;
import com.nuist.cropscan.tool.Tools;
import com.nuist.cropscan.view.BoxImageView;

import java.io.FileOutputStream;
import java.io.IOException;

/**
 * ->  tah9  2023/3/23 19:41
 */
public class ActCropScan extends ActCameraX {
    private static final String TAG = "ActCropScan";
    public Context context = this;
    public LocalGps localGps;
    public BoxImageView picMask;
    private CropTipsDialog cropTipsDialog;
    public RecyclerView recy_crop;
    private AppBarLayout barLayout;
    private AppBarLayout.Behavior behavior;


    @SuppressLint("WrongThread")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCameraCreate: ");
        picMaskHei = (int) (Tools.fullScreenHeight(context) * 0.75f - Tools.dpToPx(context, 50));

        initCameraX(R.layout.act_crop_eval);

        initView();
        localGps = new LocalGps(this);


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cropTipsDialog.toDismiss();
    }


    @SuppressLint("RestrictedApi")
    private void initView() {
        barLayout = findViewById(R.id.barLayout);

        picMask = findViewById(R.id.pic_mask);
        recy_crop = findViewById(R.id.recy_crop);
        owner_layout.setVisibility(View.GONE);
        findViewById(R.id.re_capture_btn).setOnClickListener(v -> {
            owner_layout.setVisibility(View.GONE);
            picMask.release();
            preview.getCamera().open();
            toDisplayBottomDialog();
        });
        toDisplayBottomDialog();


    }

    int picMaskHei;
    int barVerticalOffset;
    AppBarLayout.Behavior.DragCallback canDragCallBack = new AppBarLayout.Behavior.DragCallback() {
        @Override
        public boolean canDrag(@NonNull AppBarLayout appBarLayout) {
            return true;
        }
    };
    AppBarLayout.Behavior.DragCallback unCanDragCallBack = new AppBarLayout.Behavior.DragCallback() {
        @Override
        public boolean canDrag(@NonNull AppBarLayout appBarLayout) {
            return false;
        }
    };

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {

        barLayout.addOnOffsetChangedListener((appBarLayout, verticalOffset) -> {
            barVerticalOffset = verticalOffset;
        });
        float rawY = ev.getRawY();

        if (behavior == null) {
            CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) barLayout.getLayoutParams();
            behavior = (AppBarLayout.Behavior) layoutParams.getBehavior();
            if (behavior == null) {
                layoutParams.setBehavior(new AppBarLayout.Behavior());
                behavior = (AppBarLayout.Behavior) layoutParams.getBehavior();
            }
        }
        if (rawY <= picMaskHei && barVerticalOffset == 0) {
            behavior.setDragCallback(unCanDragCallBack);
        } else if (rawY > picMaskHei) {
            behavior.setDragCallback(canDragCallBack);
        }
        return super.dispatchTouchEvent(ev);
    }

    private boolean onDisplayBottomDialog = false;

    private void toDisplayBottomDialog() {
        long durTime = 500;

        //拍摄控制栏上移
        int recyHei = Tools.dpToPx(context, 120);
        ObjectAnimator.ofFloat(bottomControllerLayout, "translationY", -recyHei).setDuration(durTime).start();

        //展示底部弹窗
        cropTipsDialog = new CropTipsDialog(this, new CropTipsDialog.windowDialogListener() {
            @Override
            public void onSelect(Bitmap bitmap) {
                clickCapture(bitmap);
            }

            @Override
            public void onDismiss() {
                onDisplayBottomDialog = false;
                //复原拍摄控制栏
                ObjectAnimator.ofFloat(bottomControllerLayout, "translationY", -recyHei, 0).setDuration(durTime).start();
            }
        });
        onDisplayBottomDialog = true;
    }


    @Override
    public void onBackPressed() {
        finish();
        LoadingDialogUtils.dismiss();
    }


    @SuppressLint("RestrictedApi")
    @Override
    public void clickCapture(Bitmap bitmap) {
        //关闭摄像头
        preview.getCamera().close();

        //隐藏底部弹窗
        if (onDisplayBottomDialog) cropTipsDialog.toDismiss();

        //显示协调布局
        owner_layout.setVisibility(View.VISIBLE);

        //交给验证类处理
        CropEval cropEval = new CropEval(context, this, bitmap);
        cropEval.beginProcess();

    }

}
