package com.nuist.cropscan.tool;

/**
 * ->  tah9  2023/4/6 5:19
 */

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;

import androidx.appcompat.app.AlertDialog;

import com.nuist.cropscan.R;

import java.lang.ref.WeakReference;


public class LoadingDialogUtils {
    private static AlertDialog loadingDialog;
    private static WeakReference<Activity> reference;

    private static void init(Activity act) {
        init(act, -1);
    }

    private static void init(Activity activity, int res) {
        if (loadingDialog == null || reference == null || reference.get() == null || reference.get().isFinishing()) {
            reference = new WeakReference<>(activity);
            loadingDialog = new AlertDialog.Builder(reference.get()).create();
            if (res > 0) {
                View view = LayoutInflater.from(activity).inflate(res, null);
                loadingDialog.setView(view);
                loadingDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                // 去掉阴影层，这样就没有暗淡色的背景了
//                loadingDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            } else {
                loadingDialog.setMessage("加载中...");
            }
            loadingDialog.setCancelable(false);
        }
    }

    public static void setCancelable(boolean b) {
//        if (loadingDialog == null) return;
//        loadingDialog.setCancelable(b);
    }
//    public static void destroyOnCancel(Activity act){
//        act.finish();
//    }



    /**
     * 显示等待框
     */
    public static void show(Activity act) {
        show(act, false);
    }

    public static void show(Activity act, boolean isCancelable) {
        show(act, R.layout.dialog_loading, isCancelable);
    }

    public static void show(Activity activity, int res, boolean isCancelable) {
        init(activity, res);
        loadingDialog.show();
        setCancelable(isCancelable);
    }

    /**
     * 隐藏等待框
     */
    public static void dismiss() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
            loadingDialog = null;
            reference = null;
        }
    }
}