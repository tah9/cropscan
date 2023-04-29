package com.nuist.cropscan.scan;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.util.Log;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nuist.cropscan.request.BASEURL;
import com.nuist.cropscan.request.CropConfig;
import com.nuist.cropscan.request.HttpOk;
import com.nuist.cropscan.tool.BitmapUtil;
import com.nuist.cropscan.tool.Tools;
import com.nuist.cropscan.view.BoxImageView;
import com.nuist.cropscan.view.entiry.TRect;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * ->  tah9  2023/4/28 10:44
 */
public class CropEval {
    private Context context;
    private ActCropScan actCropScan;
    private Bitmap bitmap;
    private static final String TAG = "ActCropEval";
    private BoxImageView picMask;
    private RecyclerView recy_crop;


    public CropEval(Context context, ActCropScan actCropScan, Bitmap bitmap) {
        this.context = context;
        this.actCropScan = actCropScan;
        this.bitmap = bitmap;
        this.picMask = actCropScan.picMask;
        this.recy_crop = actCropScan.recy_crop;
    }

    public void beginProcess() {
        try {
            /*
            若是bitmap和现有不符合，调整
             */
            int scrWid = Tools.getWidth(context);
            int scrHei = Tools.fullScreenHeight(context);


            //现拍的bitmap尺寸正好
            if (bitmap.getWidth() == scrWid && bitmap.getHeight() == scrHei) {

            } else {
                float scale = (float) scrWid / bitmap.getWidth();
                int newHei = (int) (bitmap.getHeight() * scale);

                //以宽为基准缩放
                Matrix matrix = new Matrix();
                matrix.postScale(scale, scale);
                bitmap = Bitmap.createBitmap(bitmap,
                        0, 0, bitmap.getWidth(), bitmap.getHeight(),
                        matrix, true);
                if (newHei < scrHei) {
                    Bitmap bgBit = Bitmap.createBitmap(scrWid, scrHei, Bitmap.Config.ARGB_8888);
                    Canvas canvas = new Canvas(bgBit);
                    canvas.drawBitmap(bitmap, 0, Math.abs(newHei - scrHei) / 2f, null);
                    bitmap = bgBit;
                }
                //长图截取中间
                else {
                    bitmap = Bitmap.createBitmap(bitmap, 0, Math.abs(newHei - scrHei) / 2, scrWid, scrHei);
                }
            }
            //去除部分顶部和底部，只保留中间的bitmap
            bitmap = Bitmap.createBitmap(bitmap, 0, ((int) (bitmap.getHeight() * CropConfig.CropViewClipTopScale)),
                    bitmap.getWidth(), ((int) (bitmap.getHeight() * CropConfig.CropViewHeightScale)));

//            LoadingDialogUtils.show(this, false);
            picMask.setMask(bitmap);
            String base64 = BitmapUtil.bit2B64(bitmap);
            base64 = URLEncoder.encode(base64, StandardCharsets.UTF_8.toString());

            HttpOk.getInstance().toBDApi("https://aip.baidubce.com/rest/2.0/image-classify/v1/multi_object_detect",
                    "24.9cec12da92453b98fbfa79dca02fac64.2592000.1685101380.282335-32587397",
                    base64, o -> {
                        JSONArray bdResult = o.optJSONArray("result");
//                        Log.d(TAG, "toBDApi: " + bdResult);
                        //百度未识别到个体
                        if (bdResult.length() == 0) {
                            Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
                            rectList.add(0, new TRect(rect, null));
                            evalImg(bitmap, 0, rectList.get(0));
                            return;
                        }
                        for (int i = 0; i < bdResult.length(); i++) {
                            JSONObject rectObject = bdResult.optJSONObject(i).optJSONObject("location");
                            rectList.add(new TRect(rectObject, null));
                        }
                        removeContainRect();
                        filterRect();

                        picMask.updateDraw(rectList);

                        for (int i = 0; i < rectList.size(); i++) {
                            TRect rect = rectList.get(i);
                            rect.setRectBitmap(bitmap);
                            //截取框内bitmap
                            evalImg(rect.getRectBitmap(), i, rect);
                        }

                        recy_crop.setLayoutManager(new LinearLayoutManager(context, RecyclerView.HORIZONTAL, false));
                        recy_crop.setAdapter(new CropResultAdapter(rectList, context));
                    });
            JSONObject object = new JSONObject();
            object.put("BASE64", BitmapUtil.bit2B64(bitmap));
            object.put("longitude", actCropScan.localGps.getLocal().optString("longitude"));
            object.put("latitude", actCropScan.localGps.getLocal().optString("latitude"));
            String json = object.toString();
//            Log.d(TAG, "clickCapture: "+json);
//            FragWeb.getWebView().loadUrl("javascript:toEvalImg('" + json + "')");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    List<TRect> rectList = new ArrayList<>();

    private void filterRect() {
        Log.d(TAG, "filterRect before rectList.size: " + rectList.size());
        int controllerHeight = Tools.dpToPx(context, 50);
        int statusHeight = Tools.getStatusBarHeight(context);

        int minWh = (int) (Math.min(Tools.getHeight(context), Tools.getWidth(context)) * 0.2f);
        int minArea = minWh * minWh;
        //去除太小的目标，去除被状态栏和底部操作栏遮住的目标
        for (int i = 0; i < rectList.size(); i++) {
            Rect rect = rectList.get(i).getRect();
            if (
//                    rect.width() * rect.height() < minArea ||
                    rect.top > bitmap.getHeight() - controllerHeight ||
                            (rect.top < statusHeight && rect.height() < controllerHeight)) {
                rectList.remove(i);
                i--;
            }
        }
        //按面积从大到小排序
        Collections.sort(rectList, (rect, t1) -> Integer.compare(t1.getRect().width() * t1.getRect().height(), rect.getRect().width() * rect.getRect().height()));

        //只保留三个目标，减轻设备压力
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


    private void evalImg(Bitmap bitmap, int i, TRect rect) {
        try {
            JSONObject upJson = new JSONObject();
            upJson.putOpt("image_type", "base64");
            upJson.putOpt("image", BitmapUtil.bit2B64(bitmap));

//            HttpOk.getInstance().postToOtherUrl(upJson, BASEURL.flaskHost + "/classify", flaskResult -> {
//                rect.setName(flaskResult.optString("name"));
//                rectList.set(i, rect);
//                picMask.updateDraw(rectList);
//            });
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

}
