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
        okHttpClient = new OkHttpClient.Builder().connectTimeout(20, TimeUnit.SECONDS) //连接超时
                .readTimeout(20, TimeUnit.SECONDS) //读取超时
                .writeTimeout(20, TimeUnit.SECONDS) //写超时;
                .callTimeout(20, TimeUnit.SECONDS) //写超时;
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
        this.builder = new Request.Builder().post(HttpOk.jsonBody(map));
        return this;
    }


    public HttpOk upLoadFile(Map<String, String> map, String fileKey, String path) {
        MultipartBody.Builder fm = new MultipartBody.Builder().setType(MultipartBody.FORM);
        for (String key : map.keySet()) {
            fm.addFormDataPart(key, map.get(key));
        }
        File file = new File(path);
        this.builder = new Request.Builder().post(
                fm.addFormDataPart(fileKey, file.getName(),
                        RequestBody.create(MediaType.parse("image/jpg"), file)).build());
        return this;
    }


    public static RequestBody jsonBody(Map<String, Object> map) {
        return RequestBody.create(MediaType.parse("application/json;charset=utf-8"), new JSONObject(map).toString());
    }

    //x-www-form-urlencoded
//    public static RequestBody xwfBody(Map<String, Object> map) {
//
//    }

    void setback(String last, String json, back back) {
        handler.post(() -> {
            try {
                JSONObject data = new JSONObject(json);
                back.back(data);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }


    /*
    百度智能云接口
     */
    public void toBDApi(String url, String token, String base64, back back) {
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
        RequestBody body = RequestBody.create(mediaType, "image=" + base64);
//        Log.d(TAG, "baidu to: " + url);
        Request request = new Request.Builder()
                .url(url + "?access_token=" + token)
                .method("POST", body)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .addHeader("Accept", "application/json")
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
//                    setback(url, "", back);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                setback(url, response.body().string(), back);
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
                            .build()
            ).enqueue(new Callback() {
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
