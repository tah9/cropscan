package com.nuist.cropscan.request;

import static com.nuist.cropscan.request.BASEURL.entireHost;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * ->  tah9  2021/6/2 9:28
 */
public class HttpOk {
    private static final String TAG = "HttpOk";
    static HttpOk httpOk;
    private final Handler handler;
    private final OkHttpClient okHttpClient;


    public HttpOk() {
        handler = new Handler(Looper.getMainLooper());
        okHttpClient = new OkHttpClient.Builder().connectTimeout(20 , TimeUnit.SECONDS) //连接超时
                .readTimeout(20 , TimeUnit.SECONDS) //读取超时
                .writeTimeout(20 , TimeUnit.SECONDS) //写超时;
                .callTimeout(20 , TimeUnit.SECONDS) //写超时;
                .build();
    }

    Request.Builder builder;

    public static HttpOk getInstance() {
        if (httpOk == null) {
            httpOk = new HttpOk();
        }
        return httpOk;
    }

    public interface back {
        void back(JSONObject o) throws Exception;
    }

    public HttpOk setBuilder(Request.Builder builder) {
        this.builder = builder;
        return this;
    }

    public HttpOk setPost(Map<String, Object> map) {
        this.builder = new Request.Builder().post(HttpOk.body(map));
        return this;
    }

    public HttpOk setPostFile(String path) {
//        MediaType MEDIA_TYPE = MediaType.parse("image/*");
        // form 表单形式上传
        MultipartBody.Builder multipartBodyBuilder = new MultipartBody.Builder().setType(MultipartBody.FORM);
        multipartBodyBuilder.addFormDataPart("image",
                path,
                RequestBody.create(MediaType.parse("image/*"), new File(path)));
//        MediaType mediaType3 = MediaType.parse("text/x-markdown; charset=utf-8");
//        RequestBody requestBody3 = RequestBody.create(mediaType3, new File(path));
        this.builder = new Request.Builder().post(multipartBodyBuilder.build());
        return this;
    }


    public static RequestBody body(Map<String, Object> map) {
        return RequestBody.create(MediaType.parse("application/json;charset=utf-8"), new JSONObject(map).toString());
    }

    void setback(String last, String json, back back) {
        handler.post(() -> {
            try {
                JSONObject data = new JSONObject(json);
                int code = data.optInt("code");
                if (code == 200) {
                    back.back(data);
                } else {
                    throw new Exception();
                }
            } catch (Exception e) {
                System.out.println(last);
                System.out.println(json);
                e.printStackTrace();
            }
        });
    }

    public void to(String url, back back) {
        try {
            String wholeUrl = entireHost + url;
            Log.d(TAG, "to: " + wholeUrl);
            if (builder == null) {
                builder = new Request.Builder();
            }
            okHttpClient.newCall(builder.url(wholeUrl)
//                    .addHeader("Authorization", "APPCODE b814b598bc66459eba5923357b18bba0")
                    .build()).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
//                    setback(url, "", back);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    setback(wholeUrl, response.body().string(), back);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
//            setback(url, "", back);
        }
    }
}
