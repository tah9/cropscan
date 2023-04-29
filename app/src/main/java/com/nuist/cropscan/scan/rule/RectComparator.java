package com.nuist.cropscan.scan.rule;

import android.graphics.Point;
import android.graphics.Rect;

import com.nuist.cropscan.view.entiry.TRect;

import java.util.Comparator;

/**
 * ->  tah9  2023/4/29 17:21
 */
class RectComparator implements Comparator<TRect> {
    private Point center;

    public RectComparator(Point center) {
        this.center = center;
    }

    @Override
    public int compare(TRect rect1, TRect rect2) {
        int distance1 = getDistance(center, getRectCenter(rect1.getRect()));
        int distance2 = getDistance(center, getRectCenter(rect2.getRect()));
        return distance1 - distance2;
    }

    private Point getRectCenter(Rect rect) {
        return new Point(rect.centerX(), rect.centerY());
    }

    private int getDistance(Point p1, Point p2) {
        int dx = p1.x - p2.x;
        int dy = p1.y - p2.y;
        return (int) Math.sqrt(dx * dx + dy * dy);
    }
}