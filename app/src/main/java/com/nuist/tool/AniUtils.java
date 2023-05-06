package com.nuist.tool;

import android.animation.ObjectAnimator;
import android.view.View;
import android.view.animation.LinearInterpolator;

/**
 * ->  tah9  2023/4/30 5:58
 */
public class AniUtils {
    public static void rotationAni(View view, float angle, long time) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "rotation", view.getRotation(), angle);
        animator.setDuration(time);
        animator.setInterpolator(new LinearInterpolator());
        animator.start();
    }

    public static void moveXAni(View view, int start, int end, long time) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "translationX", start, end);
        animator.setDuration(time);
        animator.setInterpolator(new LinearInterpolator());
        animator.start();
    }

    public static ObjectAnimator moveYAni(View view, int start, int end, long time) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "translationY", start, end);
        animator.setDuration(time);
        animator.setInterpolator(new LinearInterpolator());
        animator.start();
        return animator;
    }
}
