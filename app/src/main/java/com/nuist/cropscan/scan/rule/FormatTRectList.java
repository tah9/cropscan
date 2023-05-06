package com.nuist.cropscan.scan.rule;

import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.Log;
import android.widget.Toast;

import com.nuist.tool.screen.Tools;
import com.nuist.cropscan.view.entiry.TRect;

import java.util.Collections;
import java.util.List;

/**
 * ->  tah9  2023/4/29 17:22
 */
public class FormatTRectList {
    List<TRect> rectList;
    Context context;
    private static final String TAG = "FormatTRect";

    public FormatTRectList(List<TRect> rectList, Context context) {
        this.rectList = rectList;
        this.context = context;
    }

    public List<TRect> formatList() {
        removeContainRect();
        filterRect();
        return rectList;
    }

    private void filterRect() {
        Log.d(TAG, "filterRect before rectList.size: " + rectList.size());
        int controllerHeight = Tools.dpToPx(context, 50);
        int scrHei = Tools.fullScreenHeight(context);
        int statusHeight = Tools.getStatusBarHeight(context);
        int scrWid = Tools.getWidth(context);

        int minWh = CropConfig.IconWH;
        int minArea = minWh * minWh;
        //去除太小的目标，去除被状态栏和底部操作栏遮住的目标
        for (int i = 0; i < rectList.size(); i++) {
            Rect rect = rectList.get(i).getRect();
            if (rect.width() * rect.height() < minArea
                    || rect.top > CropConfig.CropViewHeightScale * scrHei - controllerHeight
                    || (rect.top < statusHeight && rect.height() < controllerHeight)) {
                rectList.remove(i);
                i--;
            }
        }
        //按面积从大到小排序
//        Collections.sort(rectList, (rect, t1) -> Integer.compare(t1.getRect().width() * t1.getRect().height(), rect.getRect().width() * rect.getRect().height()));
        // 按中心点距离排序
        Point screenCenter = new Point();
        screenCenter.x = ((int) (scrWid / 2f));
        screenCenter.y = ((int) ((scrHei * CropConfig.CropViewHeightScale) / 2f));
        Collections.sort(rectList, new RectComparator(screenCenter));

        //只保留部分目标，减轻设备压力
        if (rectList.size() > CropConfig.MaxCropCount) {
            Toast.makeText(context, String.format("暂仅支持%s个目标", CropConfig.MaxCropCount), Toast.LENGTH_LONG).show();
            rectList = rectList.subList(0, CropConfig.MaxCropCount);
            Log.d(TAG, "裁剪完成");
        }
        Log.d(TAG, "filterRect after rectList.size: " + rectList.size());
    }


    /*
    去除包含其他Rect的Rect，避免重复检测。
     */
    private void removeContainRect() {
        Log.d(TAG, "removeContainRect before rectList.size: " + rectList.size());


        for (int i = 0; i < rectList.size(); i++) {
            Rect rect1 = rectList.get(i).getRect();
            boolean isContaining = false;
            for (int j = 0; j < rectList.size(); j++) {
                if (i == j) {
                    continue; // 跳过当前元素
                }
                Rect rect2 = rectList.get(j).getRect();
                if (rect1.contains(rect2)) {
                    isContaining = true;
                    break; // 如果找到被包含的Rect，则这是一个包含其他Rect的Rect
                }
            }
            if (isContaining) {
                rectList.remove(i);
                i--; // 调整索引以考虑下一个Rect对象
            }
        }
        Log.d(TAG, "removeContainRect after rectList.size: " + rectList.size());
    }
}
