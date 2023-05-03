package com.nuist.cropscan.ActPicture.event;

import android.util.Log;

import com.google.android.material.appbar.AppBarLayout;

/**
 * ->  tah9  2023/5/3 21:19
 */
public abstract class AppbarLayoutEventListener implements AppBarLayout.OnOffsetChangedListener {
    private static final String TAG = "CoverAlphaByAppbar";


    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
        if (verticalOffset == 0) {
            //完全展开了
            burst();
        } else if (verticalOffset == -appBarLayout.getTotalScrollRange()) {
            //折叠了
            collapsed();
        } else {
            //滑动中
            slide(verticalOffset, appBarLayout.getTotalScrollRange());
        }

    }


    //收缩
    protected abstract void collapsed();

    //滑动
    protected abstract void slide(int v, int range);

    //展开
    protected abstract void burst();


}
