package com.nuist.cropscan.request;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.util.concurrent.TimeUnit;

import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * ->  tah9  2023/4/18 15:02
 */
public class OkUtils {

    private OkHttpClient okHttpClient;

    public OkUtils() {
        okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .callTimeout(20, TimeUnit.SECONDS)
                .build();
    }

    public static OkUtils getInstance() {
        return OkHolder.okUtils;
    }

    private static final String TAG = "OkUtils";

    static class OkHolder {
        private static final OkUtils okUtils = new OkUtils();
    }

    public void upLoadImage(String murl, String path, String uid, Callback callback) {
        murl = BASEURL.entireHost + murl;
        Log.d(TAG, "upLoadImage: " + murl);
        File file = new File(path);// 后面的是要上传图片的地址
        MultipartBody.Builder body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("uid", uid)
                .addFormDataPart("image", file.getName(), RequestBody.create(MediaType.parse("image/jpg"), file));
        RequestBody body1 = body.build();
        Request request = new Request.Builder().url(murl).post(body1).build();
        okHttpClient.newCall(request).enqueue(callback);
    }
}

