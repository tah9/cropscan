package com.nuist.cropscan.dialog;

/**
 * ->  tah9  2023/4/6 5:19
 */

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

import androidx.appcompat.app.AlertDialog;

import com.nuist.cropscan.R;

import java.lang.ref.WeakReference;


public class LoadingDialogUtils {
    private static AlertDialog loadingDialog;
    private static WeakReference<Activity> reference;
    private static ObjectAnimator animator;


    public static void show(Activity activity) {
        if (loadingDialog == null || reference == null || reference.get() == null || reference.get().isFinishing()) {
            reference = new WeakReference<>(activity);
            loadingDialog = new AlertDialog.Builder(reference.get()).create();
            View view = LayoutInflater.from(activity).inflate(R.layout.dialog_loading, null);
            loadingDialog.setView(view);
            ImageView pic = view.findViewById(R.id.pic_progress);
            animator = ObjectAnimator.ofFloat(pic, "rotation", 0, 360);
            animator.setRepeatCount(ValueAnimator.INFINITE);
            animator.setInterpolator(new LinearInterpolator());
            animator.start();
            loadingDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            // 去掉阴影层，这样就没有暗淡色的背景了
//                loadingDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            loadingDialog.setCancelable(false);
            loadingDialog.show();
        }
    }


    /**
     * 隐藏等待框
     */
    public static void dismiss() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
            animator.cancel();
            animator = null;
            loadingDialog = null;
            reference = null;
        }
    }
}