package com.nuist.cropscan.dialog;

import android.app.Activity;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;
import com.nuist.cropscan.tool.AniUtils;
import com.nuist.cropscan.tool.Tools;
/**
 * ->  tah9  2023/5/1 22:16
 */
public class SnackUtil {
    private static Snackbar snack;

    public static void show(Activity activity, String text) {
        if (snack != null&&snack.isShown()) {
            snack.dismiss();
        }
        snack = Snackbar.make(activity, activity.getWindow().getDecorView(),
                text, 3000);
        snack.setAnimationMode(Snackbar.ANIMATION_MODE_FADE);



//        snack.setAction("关闭", new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
////                snack.dismiss();
//            }
//        });
        View view = snack.getView();

        TextView textView = view.findViewById(com.google.android.material.R.id.snackbar_text);
        textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) view.getLayoutParams();
        params.gravity = Gravity.TOP;
        params.topMargin = 2 * Tools.getStatusBarHeight(activity);
        view.setLayoutParams(params);
        snack.show();

    }
}
