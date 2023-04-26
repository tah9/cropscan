package com.nuist.cropscan.scan;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

import com.nuist.cropscan.ActPicture.ActCameraX;
import com.nuist.cropscan.HomeAct;
import com.nuist.cropscan.R;
import com.nuist.cropscan.base.FragWeb;
import com.nuist.cropscan.request.HttpOk;
import com.nuist.cropscan.tool.BitmapUtil;
import com.nuist.cropscan.tool.LocalGps;
import com.nuist.cropscan.tool.Tools;
import com.nuist.cropscan.tool.LoadingDialogUtils;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

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
        finish();
    }




    @Override
    public void clickCapture(Bitmap bitmap) {
        LoadingDialogUtils.show(this, false);

//        String path = Tools.saveBitmapFile(context, bitmap);
        picMask.setImageBitmap(bitmap);

        JSONObject object = new JSONObject();
        try {
            object.put("BASE64", BitmapUtil.bit2B64(bitmap));
            object.put("longitude", localGps.getLocal().optString("longitude"));
            object.put("latitude", localGps.getLocal().optString("latitude"));
            String json = object.toString();
//            Log.d(TAG, "clickCapture: "+json);
            FragWeb.getWebView().loadUrl("javascript:toEvalImg('" + json + "')");
        } catch (Exception e) {

        }
    }
}
