package com.nuist.cropscan.scan;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.Toast;

import com.nuist.cropscan.ActPicture.ActCameraX;
import com.nuist.cropscan.MainActivity;
import com.nuist.cropscan.tool.Tools;
import com.nuist.cropscan.request.OkUtils;
import com.nuist.cropscan.tool.LoadingDialogUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * ->  tah9  2023/3/23 19:41
 */
public class ActCropScan extends ActCameraX {
    private static final String TAG = "ActCropScan";
    public Context context = this;

    @Override
    protected void onCameraCreate() {
        Log.d(TAG, "onCameraCreate: ");
        openCamera();
    }

    @Override
    public void clickCapture(Bitmap bitmap) {
        LoadingDialogUtils.show(this, false);

        String path = Tools.saveBitmapFile(context, bitmap);
        picMask.setImageBitmap(bitmap);
        OkUtils.getInstance().upLoadImage("/classify", path,getIntent().getStringExtra("uid") , new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                LoadingDialogUtils.dismiss();
                try {
                    String string = response.body().string();
                    Log.d(TAG, "onResponse: " + string);
                    JSONObject o = new JSONObject(string);
                    if (o.optInt("acc") == 1) {
                        setString("plant", o.optString("plant"));
//                            //识别完成
//                            setString("localPicPath", path);
//                            setString("name", o.optString("name"));
//                            setString("color", o.optString("color"));
//                            setString("bottomPic", path);
//
//                            Log.d(TAG, "clickCapture: " + o);
//                            startActivity(new Intent(ActCropScan.this, MainActivity.class));
                        finish();
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(context, "检测失败", Toast.LENGTH_SHORT).show();
                                picMask.setImageBitmap(null);
                            }
                        });
                    }

                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        });
//        HttpOk.getInstance().setPostFile(path).to("/classify", o -> {
//
////            //识别完成
////            setString("localPicPath", path);
////            setString("name", o.optString("name"));
////            setString("color",o.optString("color"));
////            Log.d(TAG, "clickCapture: "+o);
////            LoadingDialogUtils.dismiss();
////            startActivity(new Intent(ActCropScan.this, MainActivity.class));
//////            finish();
//        });
    }
}
