package com.nuist.cropscan.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.AppBarLayout;
import com.nuist.cropscan.ActPicture.ActCameraX;
import com.nuist.cropscan.R;
import com.nuist.cropscan.base.BaseAct;
import com.nuist.cropscan.request.BASEURL;
import com.nuist.cropscan.request.HttpOk;
import com.nuist.cropscan.scan.ActCropScan;
import com.nuist.cropscan.scan.CropResultAdapter;
import com.nuist.cropscan.scan.ScanLayoutDispatch;
import com.nuist.cropscan.scan.rule.FormatTRectList;
import com.nuist.cropscan.tool.BitmapUtil;
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
public class EvalDialog extends AlertDialog implements DialogInterface.OnDismissListener {
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

        View root = LayoutInflater.from(act).inflate(R.layout.act_crop_eval, null);
        setView(root);
        initView(root);

        dispatch = new ScanLayoutDispatch((int) (Tools.fullScreenHeight(act) * 0.75f - Tools.dpToPx(act, 50)),
                barLayout);
        setOnDismissListener(this);

        setFullScreen();

        super.show();
    }

    private void setFullScreen() {
        Window window = getWindow();
        window.setBackgroundDrawable(new ColorDrawable(0x00000000));
        //去除阴影
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        );
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

        window.getDecorView().setBackgroundColor(Color.TRANSPARENT);
        window.setStatusBarColor(Color.TRANSPARENT);
        window.setNavigationBarColor(Color.TRANSPARENT);
        window.setType(WindowManager.LayoutParams.TYPE_APPLICATION_PANEL);
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        layoutParams.width = -1;
        layoutParams.height = -1;
        layoutParams.gravity = Gravity.CENTER;

        window.getDecorView().setPadding(0, 0, 0, 0);
        if (Build.VERSION.SDK_INT >= 28) {
            layoutParams.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }
        window.setAttributes(layoutParams);
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
        reCaptureBtn = view.findViewById(R.id.re_capture_btn);
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

//        View frame = findViewById(R.id.webView);
//        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) webView.getLayoutParams();
//        int height = Tools.fullScreenHeight(act) - Tools.dpToPx(act, 90);
//        params.height=height;
//        webView.setLayoutParams(params);
//        webView.postDelayed(() -> {
//            Log.d(TAG, "initView: " + webView.getHeight());
//        }, 2 * 1000);
//
//
//        CoordinatorLayout.LayoutParams p2 = (CoordinatorLayout.LayoutParams) nestedView.getLayoutParams();
//        params.height = Tools.fullScreenHeight(act) - Tools.dpToPx(act, 90);
//        nestedView.setLayoutParams(p2);
//        nestedView.postDelayed(() -> {
//            Log.d(TAG, "initView: " + nestedView.getHeight());
//        }, 2 * 1000);
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
                        oneTarget(bitmap);
                        return;
                    }
                    for (int i = 0; i < bdResult.length(); i++) {
                        JSONObject rectObject = bdResult.optJSONObject(i).optJSONObject("location");
                        rectList.add(new TRect(rectObject, null));
                    }


                    FormatTRectList formatTRectList = new FormatTRectList(rectList, act);
                    rectList = formatTRectList.formatList();

                    if (rectList.size() == 0) {
                        oneTarget(bitmap);
                        return;
                    }
                    //截取框内bitmap
                    for (int i = 0; i < rectList.size(); i++) {
                        TRect rect = rectList.get(i);
                        rect.setRectBitmap(bitmap);
                    }

                    recyCrop.setLayoutManager(new LinearLayoutManager(act, RecyclerView.HORIZONTAL, false));
                    cropResultAdapter = new CropResultAdapter(rectList, act, scanLayout.getActivateIndex());
                    recyCrop.setAdapter(cropResultAdapter);

                    scanLayout.setTargetClickListener(position -> {

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
        Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        TRect tRect = new TRect(rect, null);
        tRect.setBitmap(bitmap);
        rectList.add(tRect);
        evalImg(0);
    }

    private void loadWebView(String name) {

        String url = BASEURL.entireWebHost + "/#/appResult/" + name;

        WebSettings settings = webView.getSettings();
        webView.setWebViewClient(new WebViewClient());
        settings.setDomStorageEnabled(true);
        settings.setJavaScriptEnabled(true);
        //允许跨域
        settings.setAllowUniversalAccessFromFileURLs(true);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setAllowFileAccess(true);
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
    }

    /*
    识别局部
     */
    private void evalImg(int index) {
        Map<String, Object> map = new HashMap<>();
        Bitmap rectOriginalBitmap = rectList.get(index).getRectBitmap();
        map.put("image_type", "base64");
//        map.put("image", BitmapUtil.bit2B64(rectOriginalBitmap));
        map.put("image", BitmapUtil.bit2B64(Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)));

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
