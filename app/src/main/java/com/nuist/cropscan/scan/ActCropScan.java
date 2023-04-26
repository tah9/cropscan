package com.nuist.cropscan.scan;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.util.Log;
import android.util.Pair;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

import com.nuist.cropscan.ActPicture.ActCameraX;
import com.nuist.cropscan.HomeAct;
import com.nuist.cropscan.R;
import com.nuist.cropscan.base.FragWeb;
import com.nuist.cropscan.request.HttpOk;
import com.nuist.cropscan.tool.BitmapTool;
import com.nuist.cropscan.tool.BitmapUtil;
import com.nuist.cropscan.tool.FileUtils;
import com.nuist.cropscan.tool.ImgTool;
import com.nuist.cropscan.tool.LocalGps;
import com.nuist.cropscan.tool.Tools;
import com.nuist.cropscan.tool.LoadingDialogUtils;

import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * ->  tah9  2023/3/23 19:41
 */
public class ActCropScan extends ActCameraX {
    private static final String TAG = "ActCropScan";
    public Context context = this;
    private LocalGps localGps;

    @Override
    protected void onCameraCreate() {
        Log.d(TAG, "onCameraCreate: ");
        openCamera();
        localGps = new LocalGps(this);
    }

    @Override
    public void onBackPressed() {
        LoadingDialogUtils.dismiss();
    }


    @Override
    public void clickCapture(Bitmap bitmap) {
        try {
            LoadingDialogUtils.show(this, false);
            picMask.setImageBitmap(bitmap);
            String base64 = BitmapUtil.bit2B64(bitmap);
            base64 = URLEncoder.encode(base64, StandardCharsets.UTF_8.toString());

            HttpOk.getInstance().toBDApi("https://aip.baidubce.com/rest/2.0/image-classify/v1/object_detect",
                    "24.9cec12da92453b98fbfa79dca02fac64.2592000.1685101380.282335-32587397",
                    base64, o -> {
                        JSONObject result = o.optJSONObject("result");
                        tipTv.setText(result.toString()
                                +"\nweight:"+bitmap.getWidth()+",height:"+bitmap.getHeight()
                        +"screen width:"+Tools.getWidth(context));
                        Log.d(TAG, "toBDApi: " + result);
                        picMask.drawRect(result);
                    });
            JSONObject object = new JSONObject();
            object.put("BASE64", BitmapUtil.bit2B64(bitmap));
            object.put("longitude", localGps.getLocal().optString("longitude"));
            object.put("latitude", localGps.getLocal().optString("latitude"));
            String json = object.toString();
//            Log.d(TAG, "clickCapture: "+json);
//            FragWeb.getWebView().loadUrl("javascript:toEvalImg('" + json + "')");
        } catch (Exception e) {

        }
    }
}
