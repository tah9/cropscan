package com.nuist.cropscan.scan;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.AppBarLayout;
import com.nuist.cropscan.ActPicture.ActCameraX;
import com.nuist.cropscan.R;
import com.nuist.cropscan.dialog.CropTipsDialog;
import com.nuist.cropscan.dialog.LoadingDialogUtils;
import com.nuist.cropscan.tool.AniUtils;
import com.nuist.cropscan.tool.LocalGps;
import com.nuist.cropscan.tool.Tools;
import com.nuist.cropscan.view.ScanLayout;

/**
 * ->  tah9  2023/3/23 19:41
 */
public class ActCropScan extends ActCameraX {
    private static final String TAG = "ActCropScan";
    public Context context = this;
    public LocalGps localGps;
    public ScanLayout picMask;
    private CropTipsDialog cropTipsDialog;
    public RecyclerView recy_crop;
    private AppBarLayout barLayout;
    int picMaskHei;
    private ScanLayoutDispatch dispatch;
    private CropEval cropEval;

    @SuppressLint("WrongThread")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCameraCreate: ");

        initCameraX(R.layout.act_crop_eval);

        initView();
        localGps = new LocalGps(this);

        dispatch = new ScanLayoutDispatch((int) (Tools.fullScreenHeight(context) * 0.75f - Tools.dpToPx(context, 50)),
                barLayout);

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
            if (cropEval != null) {
                cropEval.destroy();
            }
            owner_layout.setVisibility(View.GONE);
            picMask.release();
            preview.getCamera().open();

            toDisplayBottomDialog();
        });

//        View frame = findViewById(R.id.webView);
//        NestedScrollView.LayoutParams params = (NestedScrollView.LayoutParams) frame.getLayoutParams();
//        int height = Tools.fullScreenHeight(context) - Tools.dpToPx(context, 90);
//        frame.setLayoutParams(new FrameLayout.LayoutParams(-1,height));
//        frame.postDelayed(() -> {
//            Log.d(TAG, "initView: "+frame.getHeight());
//        },4*1000);
//
//
//
//        View nestedView = findViewById(R.id.nestedView);
//        CoordinatorLayout.LayoutParams p2 = (CoordinatorLayout.LayoutParams) nestedView.getLayoutParams();
//        params.height = Tools.fullScreenHeight(context) - Tools.dpToPx(context, 90);
//        nestedView.setLayoutParams(p2);
//        nestedView.postDelayed(() -> {
//            Log.d(TAG, "initView: " + nestedView.getHeight());
//        },4*1000);






        toDisplayBottomDialog();


        RotationListener rotationListener = new RotationListener(this, this);
        rotationListener.setSensorEventListener(v -> {

            //按钮旋转
            AniUtils.rotationAni(btnBack, v, 300);
            AniUtils.rotationAni(btnToGallery, v, 300);
            AniUtils.rotationAni(btnSwitchLens, v, 300);

            //返回按钮平移
            if (Math.abs(v) == 90) {
                AniUtils.moveXAni(btnBack, 0, Tools.getWidth(context) - Tools.dpToPx(context, 65f), 300);
            } else {
                AniUtils.moveXAni(btnBack, (Tools.getWidth(context) - Tools.dpToPx(context, 65f)), 0, 300);
            }

            if (Math.abs(v) == 90) {
                cropTipsDialog.toDismiss();
            } else if (picMask.getBackground() == null
                    && !cropTipsDialog.dialog.isShowing()) {
                toDisplayBottomDialog();
            }
        });
    }


    /*
    分发顶部框图事件和内容区触摸事件
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        dispatch.dispatch(ev);
        return super.dispatchTouchEvent(ev);
    }


    private void toDisplayBottomDialog() {
        if (cropTipsDialog != null && cropTipsDialog.dialog.isShowing()) {
            return;
        }
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
                //复原拍摄控制栏
                if (bottomControllerLayout.getTranslationY() == -recyHei) {
                    ObjectAnimator.ofFloat(bottomControllerLayout, "translationY", -recyHei, 0).setDuration(durTime).start();
                }
            }
        });
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
        localGps.requestLocal();

        //隐藏底部弹窗
        if (cropTipsDialog.dialog.isShowing()) cropTipsDialog.toDismiss();

        //显示协调布局
        owner_layout.setVisibility(View.VISIBLE);

        //交给验证类处理
        cropEval = new CropEval(context, this, bitmap);
        cropEval.beginProcess();
    }


}
