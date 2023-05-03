package com.nuist.cropscan.tool;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

/**
 * ->  tah9  2023/5/3 19:01
 */
public class ScreenUtil {
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
}
