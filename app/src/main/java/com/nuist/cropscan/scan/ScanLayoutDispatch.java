package com.nuist.cropscan.scan;

import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.google.android.material.appbar.AppBarLayout;

/**
 * ->  tah9  2023/4/30 14:51
 */
public class ScanLayoutDispatch {
    int picMaskHei;
    private AppBarLayout barLayout;
    private AppBarLayout.Behavior behavior;
    int barVerticalOffset;
    AppBarLayout.Behavior.DragCallback canDragCallBack = new AppBarLayout.Behavior.DragCallback() {
        @Override
        public boolean canDrag(@NonNull AppBarLayout appBarLayout) {
            return true;
        }
    };
    AppBarLayout.Behavior.DragCallback unCanDragCallBack = new AppBarLayout.Behavior.DragCallback() {
        @Override
        public boolean canDrag(@NonNull AppBarLayout appBarLayout) {
            return false;
        }
    };

    public ScanLayoutDispatch(int picMaskHei, AppBarLayout barLayout) {
        this.picMaskHei = picMaskHei;
        this.barLayout = barLayout;
        this.barLayout.addOnOffsetChangedListener((appBarLayout, verticalOffset) -> {
            barVerticalOffset = verticalOffset;
        });
    }

    public void dispatch(MotionEvent ev) {

        float rawY = ev.getRawY();

        if (behavior == null) {
            CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) barLayout.getLayoutParams();
            behavior = (AppBarLayout.Behavior) layoutParams.getBehavior();
            if (behavior == null) {
                layoutParams.setBehavior(new AppBarLayout.Behavior());
                behavior = (AppBarLayout.Behavior) layoutParams.getBehavior();
            }
        }
        if (rawY <= picMaskHei && barVerticalOffset == 0) {
            behavior.setDragCallback(unCanDragCallBack);
        } else if (rawY > picMaskHei) {
            behavior.setDragCallback(canDragCallBack);
        }

    }
}
