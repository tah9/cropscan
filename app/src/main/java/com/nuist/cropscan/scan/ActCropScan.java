package com.nuist.cropscan.scan;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;

import com.nuist.gallery.ActCameraX;
import com.nuist.cropscan.dialog.CropTipsDialog;
import com.nuist.cropscan.dialog.EvalDialog;
import com.nuist.tool.dialog.SnackUtil;
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
    long durTime = 300;
    private float absTurnAngle = 0;

    int turnCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCameraCreate: ");

        recyHei = Tools.dpToPx(this, 120);
        cropTipsDialog = new CropTipsDialog(this, new CropTipsDialog.windowDialogListener() {
            @Override
            public void onSelect(Bitmap bitmap) {
            /*
            选择图片后关闭镜头，隐藏图片提示弹窗，回调图片
             */
                switchCamera(false);
                cropTipsDialog.toHide();

                clickCapture(bitmap);
            }

            @Override
            public void listenerHide() {
                Log.d(TAG, "onDismiss: ");
                toHideBottomDialog();
            }
        });

        initRotationListener();
        localGps = new LocalGps(this);

        toShowBottomDialog();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        cropTipsDialog.destroy();
        cropTipsDialog = null;
    }


    private void initRotationListener() {

        RotationListener rotationListener = new RotationListener(this, this);
        rotationListener.setSensorEventListener(senAngle -> {
            if (++turnCount == 4) {
                SnackUtil.showAutoDis(btnBack, "靠近目标识别更精准哦~");
                turnCount = 0;
            }
            absTurnAngle = Math.abs(senAngle);
            Log.d(TAG, "absTurnAngle: " + absTurnAngle);

            rotationCameraBtn();

            coordinateAnimal();
        });
    }

    //横屏指标：旋转90或270（绝对值）
    private boolean isLandScape() {
        return absTurnAngle == 90 || absTurnAngle == 270;
    }

    private void rotationCameraBtn() {
        //按钮旋转
        AniUtils.rotationAni(btnBack, absTurnAngle, durTime);
        AniUtils.rotationAni(btnToGallery, absTurnAngle, durTime);
        AniUtils.rotationAni(btnSwitchLens, absTurnAngle, durTime);

        //返回按钮平移
        if (isLandScape()) {
            AniUtils.moveXAni(btnBack, 0, Tools.getWidth(context) - Tools.dpToPx(context, 65f), 300);
        } else {
            AniUtils.moveXAni(btnBack, (Tools.getWidth(context) - Tools.dpToPx(context, 65f)), 0, 300);
        }
    }

    /*
    自动处理动画
     */
    private void coordinateAnimal() {
        if (cropTipsDialog == null) return;
        /*
        相机关闭状态或弹窗展示状态，不做协调
         */
        if (!openCamera || evalDialog != null) return;

        /*
        横屏隐藏提示弹窗，竖屏展示提示弹窗
         */
        if (isLandScape()) {
            toHideBottomDialog();
        } else {
            toShowBottomDialog();
        }
    }

    private void toShowBottomDialog() {

        //拍摄控制栏上移
        ObjectAnimator.ofFloat(
                bottomControllerLayout,
                "translationY",
                -recyHei
        ).setDuration(durTime).start();


        //展示底部弹窗
        cropTipsDialog.show();
    }

    private void toHideBottomDialog() {
        if (!cropTipsDialog.beShow()) return;

        //隐藏底部弹窗
        cropTipsDialog.toHide();

        //复原拍摄控制栏
        ObjectAnimator.ofFloat(
                bottomControllerLayout,
                "translationY",
                -recyHei, 0).setDuration(durTime).start();

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
        localGps.attemptRequestLocal();

        toHideBottomDialog();

        coordinateAnimal();

        evalDialog = new EvalDialog(context, this);

        evalDialog.beginProcess(bitmap);
        evalDialog.setDismissListener(() -> {
            evalDialog = null;
            switchCamera(true);
            coordinateAnimal();
        });
    }
}
