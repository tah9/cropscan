package com.nuist.tool.dialog;

import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.nuist.tool.screen.Tools;

/**
 * ->  tah9  2023/5/1 22:16
 */
public class SnackUtil {
    private static Snackbar snack;

    public static void showAutoDis(View view, String text) {
        showAndTime(view, text, 2000);
    }

    public static void showAndTime(View view, String text, int durTime) {
        if (snack != null && snack.isShown()) {
            snack.dismiss();
        }
        snack = Snackbar.make(view.getContext(), view,
                text, durTime);
        snack.setAnimationMode(Snackbar.ANIMATION_MODE_FADE);

        activate(snack);
    }

    public static void showIndefinite(View view, String text) {
        showAndTime(view, text, BaseTransientBottomBar.LENGTH_INDEFINITE);
    }

    public static void activate(Snackbar snackbar) {
        View snackView = snack.getView();

        TextView textView = snackView.findViewById(com.google.android.material.R.id.snackbar_text);
        textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

        try {
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) snackView.getLayoutParams();
            params.gravity = Gravity.TOP;
            params.topMargin = 2 * Tools.getStatusBarHeight(snackbar.getContext());
            snackView.setLayoutParams(params);
        } catch (Exception e) {
            CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) snackView.getLayoutParams();
            params.gravity = Gravity.TOP;
            params.topMargin = 2 * Tools.getStatusBarHeight(snackbar.getContext());
            snackView.setLayoutParams(params);
        }
        snack.show();
    }
}
