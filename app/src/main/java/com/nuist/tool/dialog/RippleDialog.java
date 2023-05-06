package com.nuist.tool.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import com.nuist.cropscan.R;
import com.nuist.tool.screen.ScreenUtil;

/**
 * ->  tah9  2023/5/6 15:36
 */
public class RippleDialog {
    private static Dialog dialog;


    public static void show(Context context) {
        if (dialog == null) {
            dialog = new Dialog(context);
            View view = LayoutInflater.from(context).inflate(R.layout.dialog_ripple, null);
            dialog.setContentView(view);
            ScreenUtil.setDialogFullScreen(dialog.getWindow());
            dialog.setCanceledOnTouchOutside(false);
            dialog.setCancelable(false);
            dialog.show();
        }
    }

    public static void dismiss() {
        if (dialog != null&&dialog.isShowing()) {
            dialog.dismiss();
            dialog=null;
        }
    }
}
