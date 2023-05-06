package com.nuist.cropscan.scan;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;

import com.nuist.gallery.ActCameraX;
import com.nuist.cropscan.dialog.CropTipsDialog;
import com.nuist.cropscan.dialog.EvalDialog;
import com.nuist.tool.dialog.SnackUtil;
import com.nuist.cropscan.scan.rule.FormatBitmap;
import com.nuist.tool.AniUtils;
import com.nuist.tool.sensor.LocalGps;
import com.nuist.tool.screen.Tools;
import com.nuist.tool.sensor.RotationListener;

/**
 * ->  tah9  2023/3/23 19:41
 */
public class ActCropScan extends ActCameraX {
    private static final String TAG = "ActCropScan";
    public Context context = this;
    public LocalGps localGps;
    private CropTipsDialog cropTipsDialog;
    private EvalDialog evalDialog;
    private int recyHei;
    long durTime = 500;
    private float angle = 0;

    int turnCount = 0;

    @SuppressLint("WrongThread")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCameraCreate: ");

        initCameraX();

        initView();
        localGps = new LocalGps(this);

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        cropTipsDialog.toDismiss();
    }


    private void initView() {

        toDisplayBottomDialog();


        RotationListener rotationListener = new RotationListener(this, this);
        rotationListener.setSensorEventListener(senAngle -> {
            if (++turnCount == 4) {
                SnackUtil.showAutoDis(btnBack, "靠近目标识别更精准哦~");
                turnCount = 0;
            }
            angle = senAngle;
            //按钮旋转
            AniUtils.rotationAni(btnBack, angle, 300);
            AniUtils.rotationAni(btnToGallery, angle, 300);
            AniUtils.rotationAni(btnSwitchLens, angle, 300);

            //返回按钮平移
            if (Math.abs(angle) == 90) {
                AniUtils.moveXAni(btnBack, 0, Tools.getWidth(context) - Tools.dpToPx(context, 65f), 300);
            } else {
                AniUtils.moveXAni(btnBack, (Tools.getWidth(context) - Tools.dpToPx(context, 65f)), 0, 300);
            }

            autoShowTipsDialog();

        });
    }

    private void autoShowTipsDialog() {
        if (cropTipsDialog != null && Math.abs(angle) == 90) {
            cropTipsDialog.toDismiss();
        } else if (evalDialog == null
                && cropTipsDialog != null && !cropTipsDialog.dialog.isShowing()) {
            toDisplayBottomDialog();
        }
    }

    private void toDisplayBottomDialog() {
        if (cropTipsDialog != null && cropTipsDialog.dialog.isShowing()) {
            return;
        }

        //拍摄控制栏上移
        recyHei = Tools.dpToPx(context, 120);


        ObjectAnimator.ofFloat(bottomControllerLayout, "translationY", -recyHei).setDuration(durTime).start();


        //展示底部弹窗
        cropTipsDialog = new CropTipsDialog(this, new CropTipsDialog.windowDialogListener() {
            @Override
            public void onSelect(Bitmap bitmap) {
                clickCapture(bitmap);
            }

            @Override
            public void onDismiss() {
                if (evalDialog != null) {
                    switchCamera(false);
                }
                //复原拍摄控制栏
                if (bottomControllerLayout.getTranslationY() == -recyHei) {
                    ObjectAnimator.ofFloat(bottomControllerLayout, "translationY", -recyHei, 0).setDuration(durTime).start();
                }
            }
        });
    }


    @Override
    public void onBackPressed() {
        if (hasNewCapture) {
            //有新的识别操作，通知刷新
            setResult(101);
        }

        finish();
    }

    private boolean hasNewCapture = false;

    @Override
    public void clickCapture(Bitmap bitmap) {
        hasNewCapture = true;
        //关闭摄像头
        switchCamera(false);
        localGps.requestLocal();


        //隐藏底部弹窗
        if (cropTipsDialog.dialog.isShowing()) {
            cropTipsDialog.toDismiss();
        }
        if (bottomControllerLayout.getTranslationY() == -recyHei) {
            ObjectAnimator.ofFloat(bottomControllerLayout, "translationY", -recyHei, 0).setDuration(durTime).start();
        }


        evalDialog = new EvalDialog(context, this);

        //裁剪bitmap上下部分后显示
        evalDialog.beginProcess(FormatBitmap.format(context, bitmap));
        evalDialog.setDismissListener(() -> {
            evalDialog = null;
            switchCamera(true);
            autoShowTipsDialog();
        });
    }
}
