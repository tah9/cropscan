package com.nuist.cropscan.scan;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;

import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nuist.cropscan.R;
import com.nuist.cropscan.base.FragWeb;
import com.nuist.cropscan.request.BASEURL;
import com.nuist.cropscan.request.HttpOk;
import com.nuist.cropscan.scan.rule.FormatTRectList;
import com.nuist.cropscan.scan.rule.FormatBitmap;
import com.nuist.cropscan.tool.BitmapUtil;
import com.nuist.cropscan.view.ScanLayout;
import com.nuist.cropscan.view.entiry.TRect;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private String oriBase64;
    private String encodeBase64;


    public CropEval(Context context, ActCropScan actCropScan, Bitmap bitmap) {
        this.context = context;
        this.actCropScan = actCropScan;
        this.bitmap = bitmap;
        this.scanLayout = actCropScan.picMask;
        this.recy_crop = actCropScan.recy_crop;
    }

    public void destroy() {
        Log.d(TAG, "destroy: ");
        recy_crop.setAdapter(null);
        //取消之前的识别请求
        for (Call call : requestList) {
            call.cancel();
        }
        requestList.clear();
        rectList.clear();
    }

    public void beginProcess() {
        bitmap = FormatBitmap.format(context, bitmap);

        scanLayout.setMask(bitmap);

        actCropScan.localGps.setListener(jsonObject -> {
            uploadOriginal(oriBase64, jsonObject);
        });
        oriBase64 = BitmapUtil.bit2B64(bitmap);
        try {
            encodeBase64 = URLEncoder.encode(oriBase64, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        HttpOk.getInstance().toBDApi("https://aip.baidubce.com/rest/2.0/image-classify/v1/multi_object_detect",
                "24.9cec12da92453b98fbfa79dca02fac64.2592000.1685101380.282335-32587397",
                encodeBase64, o -> {
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


                    FormatTRectList formatTRectList = new FormatTRectList(rectList, context);
                    rectList = formatTRectList.formatList();

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

                        if (tRect.getName() != null) {
                            loadWebView(tRect.getName());
                        }
                    });

                    scanLayout.setList(rectList);


                });

    }

    //将整个bitmap作为识别目标
    private void oneTarget() {
        Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        TRect tRect = new TRect(rect, null);
        tRect.setBitmap(bitmap);
        rectList.add(tRect);
        evalImg(0);
    }

    private void loadWebView(String name) {

        String url = BASEURL.entireWebHost + "/#/appResult/" + name;

        WebView webView =  actCropScan.findViewById(R.id.webView);

        WebSettings settings = webView.getSettings();
        settings.setDomStorageEnabled(true);
        settings.setJavaScriptEnabled(true);
        //允许跨域
        settings.setAllowUniversalAccessFromFileURLs(true);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
//        settings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setAllowFileAccess(true);
        webView.loadUrl(url);

//        FragmentTransaction fragmentTransaction = actCropScan.getSupportFragmentManager().beginTransaction();
//        FragWeb fragWeb = new FragWeb(url);
//        fragmentTransaction.replace(R.id.frame, fragWeb).commit();
    }

    List<TRect> rectList = new ArrayList<>();
    private List<Call> requestList = new ArrayList<>();

    /*
    上传原图，在用户识别历史记录中显示
     */
    private void uploadOriginal(String base64, JSONObject localJson) {
        Map<String, Object> map = new HashMap<>();
        map.put("image_type", "BASE64");
        map.put("image", base64);
        map.put("longitude", localJson.optString("longitude"));
        map.put("latitude", localJson.optString("latitude"));
        HttpOk.getInstance().postOwnerUrlFormData(
                map, "/classify/" + actCropScan.optString("uid"), o -> {
                    Log.d(TAG, "uploadOriginal: " + o);
                });
    }

    /*
    识别局部
     */
    private void evalImg(int index) {
        Map<String, Object> map = new HashMap<>();
        Bitmap rectOriginalBitmap = rectList.get(index).getRectBitmap();
        map.put("image_type", "base64");
//        map.put("image", BitmapUtil.bit2B64(rectOriginalBitmap));
        map.put("image", BitmapUtil.bit2B64(Bitmap.createBitmap(1,1, Bitmap.Config.ARGB_8888)));

//        FragWeb.getWebView().loadUrl("javascript:toEvalImg('" + json + "')");

        requestList.add(HttpOk.getInstance().postToOtherUrl(map, BASEURL.flaskHost + "/classify", flaskResult -> {
//        requestList.add(HttpOk.getInstance().postToOwnerUrl(map, "/classify/" + actCropScan.optString("uid"), flaskResult -> {
            TRect rect = rectList.get(index);
            rect.setName(flaskResult.optString("name"));
            rectList.set(index, rect);
            scanLayout.notifyItemLoadEnd(index, rect);

            if (scanLayout.getActivateIndex()==index){
                loadWebView(rect.getName());
            }
        }));
    }

}
