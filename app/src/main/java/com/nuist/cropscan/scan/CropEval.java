package com.nuist.cropscan.scan;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.util.Log;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nuist.cropscan.request.BASEURL;
import com.nuist.cropscan.scan.rule.CropConfig;
import com.nuist.cropscan.request.HttpOk;
import com.nuist.cropscan.scan.rule.FormatTRect;
import com.nuist.cropscan.tool.BitmapUtil;
import com.nuist.cropscan.tool.Tools;
import com.nuist.cropscan.view.ScanLayout;
import com.nuist.cropscan.view.entiry.TRect;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;

/**
 * ->  tah9  2023/4/28 10:44
 */
public class CropEval {
    private Context context;
    private ActCropScan actCropScan;
    private Bitmap bitmap;
    private static final String TAG = "ActCropEval";
    private ScanLayout scanLayout;
    private RecyclerView recy_crop;
    private CropResultAdapter cropResultAdapter;


    public CropEval(Context context, ActCropScan actCropScan, Bitmap bitmap) {
        this.context = context;
        this.actCropScan = actCropScan;
        this.bitmap = bitmap;
        this.scanLayout = actCropScan.picMask;
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
            scanLayout.setMask(bitmap);
            String base64 = BitmapUtil.bit2B64(bitmap);
            base64 = URLEncoder.encode(base64, StandardCharsets.UTF_8.toString());

            HttpOk.getInstance().toBDApi("https://aip.baidubce.com/rest/2.0/image-classify/v1/multi_object_detect",
                    "24.9cec12da92453b98fbfa79dca02fac64.2592000.1685101380.282335-32587397",
                    base64, o -> {
                        JSONArray bdResult = o.optJSONArray("result");
                        //百度未识别到个体
                        if (bdResult == null || bdResult.length() == 0) {
                            oneTarget();
                            return;
                        }
                        for (int i = 0; i < bdResult.length(); i++) {
                            JSONObject rectObject = bdResult.optJSONObject(i).optJSONObject("location");
                            rectList.add(new TRect(rectObject, null));
                        }

                        FormatTRect formatTRect = new FormatTRect(rectList, context);
                        rectList = formatTRect.formatList();

                        if (rectList.size() == 0) {
                            oneTarget();
                            return;
                        }
                        //截取框内bitmap
                        for (int i = 0; i < rectList.size(); i++) {
                            TRect rect = rectList.get(i);
                            rect.setRectBitmap(bitmap);
                        }
                        recy_crop.setLayoutManager(new LinearLayoutManager(context, RecyclerView.HORIZONTAL, false));
                        cropResultAdapter = new CropResultAdapter(rectList, context, scanLayout.getActivateIndex());
                        recy_crop.setAdapter(cropResultAdapter);
                        scanLayout.setTargetClickListener(position -> {

                            recy_crop.scrollToPosition(position);
                            cropResultAdapter.updateActivateIndex(scanLayout.getActivateIndex());

                            Log.d(TAG, "ClickListener: " + position);
                            TRect tRect = rectList.get(position);
                            if (tRect.getName() == null && !scanLayout.getBoxViewAt(position).isLoad()) {
                                scanLayout.getBoxViewAt(position).setLoad(true);
                                evalImg(position);
                            }

                        });
                        scanLayout.setList(rectList);

//                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
//                            preRequest.cancel();
//                            Log.d(TAG, "beginProcess: 请求被取消");
//                        }, 3000);


                    });
//            JSONObject object = new JSONObject();
//            object.put("BASE64", BitmapUtil.bit2B64(bitmap));
//            object.put("longitude", actCropScan.localGps.getLocal().optString("longitude"));
//            object.put("latitude", actCropScan.localGps.getLocal().optString("latitude"));
//            String json = object.toString();
//            Log.d(TAG, "clickCapture: "+json);
//            FragWeb.getWebView().loadUrl("javascript:toEvalImg('" + json + "')");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //将整个bitmap作为识别目标
    private void oneTarget() {
        Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        TRect tRect = new TRect(rect, null);
        tRect.setBitmap(bitmap);
        rectList.add(tRect);
        evalImg(0);
    }

    List<TRect> rectList = new ArrayList<>();


    private Call preRequest = null;

    private void evalImg(int index) {
        try {
            //取消之前的识别请求
//            if (preRequest != null) {
//                Log.d(TAG, "evalImg: 请求将被取消");
//                preRequest.cancel();
//                Log.d(TAG, "evalImg: 请求已经被取消");
//            }
            JSONObject upJson = new JSONObject();
            upJson.putOpt("image_type", "base64");
            upJson.putOpt("image", BitmapUtil.bit2B64(bitmap));


            preRequest = HttpOk.getInstance().postToOtherUrl(upJson, BASEURL.flaskHost + "/classify", flaskResult -> {
                TRect rect = rectList.get(index);
                rect.setName(flaskResult.optString("name"));
                rectList.set(index, rect);
                scanLayout.notifyItemLoadEnd(index, rect);
//                picMask.updateDraw(rectList);
            });
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

}
