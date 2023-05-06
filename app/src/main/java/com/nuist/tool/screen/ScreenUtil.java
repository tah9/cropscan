package com.nuist.tool.screen;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

/**
 * ->  tah9  2023/5/3 19:01
 */
public class ScreenUtil {

    //设置状态栏文字颜色
    public static void setStatusBarFontColor(Window window, boolean black) {
        View decor = window.getDecorView();
        int ui = decor.getSystemUiVisibility();
        if (black) {
            ui |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        } else {
            ui &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        }
        decor.setSystemUiVisibility(ui);
    }

    public static void setDialogFullScreen(Window window) {
        setTranslateStatusBar(window);
        ScreenUtil.setTranslateStatusBar(window);

        WindowManager.LayoutParams layoutParams = window.getAttributes();
        layoutParams.width = -1;
        layoutParams.height = -1;
        layoutParams.gravity = Gravity.CENTER;

        window.getDecorView().setPadding(0, 0, 0, 0);
        if (Build.VERSION.SDK_INT >= 28) {
            layoutParams.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }
        window.setAttributes(layoutParams);
    }

    public static void setTranslateStatusBar(Window window) {
        window.setBackgroundDrawable(new ColorDrawable(0x00000000));
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

        window.getDecorView().setBackgroundColor(Color.TRANSPARENT);
        window.setStatusBarColor(Color.TRANSPARENT);
        window.setNavigationBarColor(Color.TRANSPARENT);
        window.setType(WindowManager.LayoutParams.TYPE_APPLICATION_PANEL);
    }

    public static void setStatusBarBgColor(Window window,int color) {
        setTranslateStatusBar(window);
        //设置状态栏颜色
        window.setStatusBarColor(color);
        // 去掉系统状态栏下的windowContentOverlay
        View v = window.findViewById(android.R.id.content);
        if (v != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                v.setForeground(null);
            }
        }
    }
}
