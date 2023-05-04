package com.nuist.cropscan.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.AppBarLayout;
import com.nuist.cropscan.R;
import com.nuist.cropscan.base.BaseAct;
import com.nuist.cropscan.request.BASEURL;
import com.nuist.cropscan.request.FileConfig;
import com.nuist.cropscan.request.HttpOk;
import com.nuist.cropscan.scan.ActCropScan;
import com.nuist.cropscan.scan.CropResultAdapter;
import com.nuist.cropscan.scan.ScanLayoutDispatch;
import com.nuist.cropscan.scan.rule.FormatTRectList;
import com.nuist.cropscan.tool.img.Base64Until;
import com.nuist.cropscan.tool.ScreenUtil;
import com.nuist.cropscan.tool.Tools;
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
 * ->  tah9  2023/5/2 17:19
 */
public class EvalDialog extends AlertDialog implements DialogInterface.OnCancelListener, DialogInterface.OnDismissListener {
    private ScanLayoutDispatch dispatch;
    private BaseAct act;
    private AppBarLayout barLayout;
    private ScanLayout scanLayout;
    private RecyclerView recyCrop;
    private ImageView reCaptureBtn;
    private NestedScrollView nestedView;
    private WebView webView;
    private String oriBase64;
    private String encodeBase64;
    private CropResultAdapter cropResultAdapter;
    List<TRect> rectList = new ArrayList<>();
    private List<Call> requestList = new ArrayList<>();

    private static final String TAG = "EvalDialog";

    public EvalDialog(@NonNull Context context, BaseAct act) {
        super(context);
        this.act = act;
        show();
    }

    /*
        分发顶部框图事件和内容区触摸事件
    */
    @Override
    public boolean dispatchTouchEvent(@NonNull MotionEvent ev) {
        dispatch.dispatch(ev);
        return super.dispatchTouchEvent(ev);
    }


    @Override
    public void show() {

        View root = LayoutInflater.from(act).inflate(R.layout.dialog_eval, null);
        setView(root);
        initView(root);

        dispatch = new ScanLayoutDispatch((int) (Tools.fullScreenHeight(act) * 0.75f - Tools.dpToPx(act, 50)),
                barLayout);
        setOnDismissListener(this);
        setOnCancelListener(this);

        ScreenUtil.setDialogFullScreen(getWindow());

        setCanceledOnTouchOutside(false);
        super.show();

        SnackUtil.showAutoDis(root, "服务器算力有限，识别较慢请耐心等待~");
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        if (listener != null) {
            listener.dismiss();
        }
    }

    public interface dismissListener {
        void dismiss();
    }

    private dismissListener listener;

    public void setDismissListener(dismissListener listener) {
        this.listener = listener;
    }

    private void initView(View view) {
        barLayout = view.findViewById(R.id.barLayout);
        scanLayout = view.findViewById(R.id.scan_layout);
        recyCrop = view.findViewById(R.id.recy_crop);
        reCaptureBtn = view.findViewById(R.id.back_front);
        nestedView = view.findViewById(R.id.nestedView);
        webView = view.findViewById(R.id.webView);


        reCaptureBtn.setOnClickListener(v -> {
            dismiss();
            if (listener != null) {
                listener.dismiss();
            }
        });

        ImageView bgPic = view.findViewById(R.id.bg_pic);
        ViewGroup.LayoutParams layoutParams = bgPic.getLayoutParams();
        layoutParams.height = Tools.fullScreenHeight(act);
        bgPic.setLayoutParams(layoutParams);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        Log.d(TAG, "destroy: ");
        recyCrop.setAdapter(null);
        //取消之前的识别请求
        for (Call call : requestList) {
            call.cancel();
        }
        requestList.clear();
        rectList.clear();
    }


    public void beginProcess(Bitmap bitmap) {
        ((ActCropScan) act).localGps.setListener(jsonObject -> {
            uploadOriginal(oriBase64, jsonObject);
        });

        process(bitmap);
    }

    public void recordProcess(Bitmap bitmap) {
        process(bitmap);
    }

    public void process(Bitmap bitmap) {
        scanLayout.setMask(bitmap);
        oriBase64 = Base64Until.bit2B64(bitmap);
        try {
            encodeBase64 = URLEncoder.encode(oriBase64, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        HttpOk.getInstance().toBDApi("https://aip.baidubce.com/rest/2.0/image-classify/v1/multi_object_detect",
                "24.9cec12da92453b98fbfa79dca02fac64.2592000.1685101380.282335-32587397",
                encodeBase64, o -> {
                    JSONArray bdResult = o.optJSONArray("result");
                    Log.d(TAG, "process: " + bdResult);
                    //百度未识别到个体
                    if (bdResult == null || bdResult.length() == 0) {
                        oneTarget(bitmap);
                    } else {
                        for (int i = 0; i < bdResult.length(); i++) {
                            JSONObject rectObject = bdResult.optJSONObject(i).optJSONObject("location");
                            rectList.add(new TRect(rectObject, null));
                        }

                    }
                    FormatTRectList formatTRectList = new FormatTRectList(rectList, act);
                    rectList = formatTRectList.formatList();

                    if (rectList.size() == 0) {
                        oneTarget(bitmap);
                    } else {
                        //截取框内bitmap
                        for (int i = 0; i < rectList.size(); i++) {
                            TRect rect = rectList.get(i);
                            rect.setRectBitmap(bitmap);
                        }
                    }

                    recyCrop.setLayoutManager(new LinearLayoutManager(act, RecyclerView.HORIZONTAL, false));
                    cropResultAdapter = new CropResultAdapter(rectList, act, scanLayout.getActivateIndex());
                    recyCrop.setAdapter(cropResultAdapter);

                    scanLayout.setTargetClickListener(position -> {
                        loadWebView("识别中~");
                        recyCrop.scrollToPosition(position);
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
    private void oneTarget(Bitmap bitmap) {
        Log.d(TAG, "oneTarget: ");
        Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        TRect tRect = new TRect(rect, null);
        tRect.setBitmap(bitmap);
        rectList.add(tRect);
    }

    private void initializeWebView() {
        if (webView.getUrl() != null) {
            return;
        }
        WebSettings settings = webView.getSettings();
        //禁止调用外部浏览器
        webView.setWebViewClient(new WebViewClient());
        settings.setDomStorageEnabled(true);
        settings.setJavaScriptEnabled(true);
        //允许跨域
        settings.setAllowUniversalAccessFromFileURLs(true);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setAllowFileAccess(true);
    }

    private void loadWebView(String name) {
        initializeWebView();
        String url = FileConfig.webFileUrlHome(act) + "#/appResult/" + name;
        webView.loadUrl(url);
        Log.d(TAG, ": " + url);
        webView.loadUrl("javascript:window.location.reload( true )");
    }


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
                map, "/classify/" + act.optString("uid"), o -> {
                    Log.d(TAG, "uploadOriginal: " + o);
                });
//        Toast.makeText(act, "", Toast.LENGTH_LONG).show();
        Toast toast = Toast.makeText(act, "服务器算力有限，识别较慢请等待\n靠近拍摄识别结果更精准", Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();

//        SnackUtil.show(act,"");
    }

    /*
    识别局部
     */
    private void evalImg(int index) {
        Map<String, Object> map = new HashMap<>();
        Bitmap rectOriginalBitmap = rectList.get(index).getRectBitmap();
        map.put("image_type", "base64");
        map.put("image", Base64Until.bit2B64(rectOriginalBitmap));
//        map.put("image", BitmapUtil.bit2B64(Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)));

//        FragWeb.getWebView().loadUrl("javascript:toEvalImg('" + json + "')");

        requestList.add(HttpOk.getInstance().postToOtherUrl(map, BASEURL.flaskHost + "/classify", flaskResult -> {
//        requestList.add(HttpOk.getInstance().postToOwnerUrl(map, "/classify/" + actCropScan.optString("uid"), flaskResult -> {
            TRect rect = rectList.get(index);
            rect.setName(flaskResult.optString("name"));
            rectList.set(index, rect);
            scanLayout.notifyItemLoadEnd(index, rect);

            if (scanLayout.getActivateIndex() == index) {
                loadWebView(rect.getName());
            }
        }));
    }

}
