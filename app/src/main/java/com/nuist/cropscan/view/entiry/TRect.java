package com.nuist.cropscan.view.entiry;

import android.graphics.Bitmap;
import android.graphics.Rect;

import org.json.JSONObject;

/**
 * ->  tah9  2023/4/27 13:03
 */
public class TRect {
    Rect rect;
    String name = null;
    float score;

    public float getScore() {
        return score;
    }

    public void setScore(float score) {
        this.score = score;
    }

    public int left;
    public int right;
    public int top;
    public int bottom;
    private Bitmap rectBitmap;

    public void setRectBitmap(Bitmap bitmap) {
        this.rectBitmap = Bitmap.createBitmap(bitmap, rect.left, rect.top, rect.width(), rect.height());
    }

    public void setBitmap(Bitmap bitmap) {
        this.rectBitmap = bitmap;
    }

    public Bitmap getRectBitmap() {
        return rectBitmap;
    }

    public void release() {
        if (rectBitmap != null) {
            rectBitmap.recycle();
            rectBitmap = null;
        }
        name = null;
        rect = null;
    }

    public TRect(JSONObject o, String name) {
        int left = o.optInt("left");
        int top = o.optInt("top");
        int width = o.optInt("width");
        int height = o.optInt("height");
        this.name = name;

        this.rect = new Rect(left,
                top,
                width + left, height + top);
        setAttr();
    }

    public TRect(Rect rect, String name) {
        this.rect = rect;
        this.name = name;

        setAttr();
    }

    private void setAttr() {
        this.right = rect.right;
        this.left = rect.left;
        this.top = rect.top;
        this.bottom = rect.bottom;
    }

    @Override
    public String toString() {
        return "TRect{" +
                "rect=" + rect +
                ", name='" + name + '\'' +
                ", left=" + left +
                ", right=" + right +
                ", top=" + top +
                ", bottom=" + bottom +
                '}';
    }

    public Rect getRect() {
        return rect;
    }

    public void setRect(Rect rect) {
        this.rect = rect;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}