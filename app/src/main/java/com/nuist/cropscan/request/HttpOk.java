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

    private int MAX_TIME = 600;

    public HttpOk() {
        handler = new Handler(Looper.getMainLooper());
        okHttpClient = new OkHttpClient.Builder().connectTimeout(MAX_TIME, TimeUnit.SECONDS) //连接超时
                .readTimeout(MAX_TIME, TimeUnit.SECONDS) //读取超时
                .writeTimeout(MAX_TIME, TimeUnit.SECONDS) //写超时;
                .callTimeout(MAX_TIME, TimeUnit.SECONDS) //写超时;
                .build();
    }


    public static HttpOk getInstance() {
        if (httpOk == null) {
            httpOk = new HttpOk();
        }
        return httpOk;
    }

    public interface HttpResult {
        void promise(JSONObject o) throws Exception;
    }


    public HttpOk upLoadFile(Map<String, String> map, String fileKey, String path) {
        MultipartBody.Builder fm = new MultipartBody.Builder().setType(MultipartBody.FORM);
        for (String key : map.keySet()) {
            fm.addFormDataPart(key, map.get(key));
        }
        File file = new File(path);
//        this.builder = new Request.Builder().post(
//                fm.addFormDataPart(fileKey, file.getName(),
//                        RequestBody.create(MediaType.parse("image/jpg"), file)).build());
        return this;
    }


    public static RequestBody jsonBody(JSONObject jsonObject) {
        return RequestBody.create(MediaType.parse("application/json;charset=utf-8"), jsonObject.toString());
    }

    //x-www-form-urlencoded
//    public static RequestBody xwfBody(Map<String, Object> map) {
//
//    }


    /*
    百度智能云接口
     */
    public void toBDApi(String url, String token, String base64, HttpResult HttpResult) {
        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
        RequestBody body = RequestBody.create(mediaType, "image=" + base64);
//        Log.d(TAG, "baidu to: " + url);
        Request request = new Request.Builder()
                .url(url + "?access_token=" + token)
                .method("POST", body)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .addHeader("Accept", "application/json")
                .build();
        this.okHttpClient.newCall(request).enqueue(getNewCallBack(HttpResult));
    }


    public void toOtherUrl(String url, HttpResult HttpResult) {
        getTo(url, HttpResult);
    }

    public void toOwnerUrl(String url, HttpResult HttpResult) {
        getTo(entireHost + url, HttpResult);
    }

    public void postToOwnerUrl(JSONObject jsonObject, String url, HttpResult HttpResult) {
        postTo(new Request.Builder().post(HttpOk.jsonBody(jsonObject)), url, HttpResult);
    }

    public void postToOtherUrl(JSONObject jsonObject, String url, HttpResult HttpResult) {
        postTo(new Request.Builder().post(HttpOk.jsonBody(jsonObject)), url, HttpResult);
    }

    private void postTo(Request.Builder builder, String url, HttpResult HttpResult) {
        Log.d(TAG, "postTo: " + url);
        okHttpClient.newCall(builder.url(url).build()).enqueue(getNewCallBack(HttpResult));
    }

    private void getTo(String url, HttpResult HttpResult) {
        Log.d(TAG, "getTo: " + url);
        okHttpClient.newCall(new Request.Builder().url(url).build()).enqueue(getNewCallBack(HttpResult));
    }

    private int failSum = 0;
    private int MaxFailCount = 5;

    private Callback getNewCallBack(HttpResult toResult) {
        return new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                handler.post(() -> {
                    try {
                        String json = response.body().string();
                        JSONObject data = new JSONObject(json);
                        toResult.promise(data);
                    } catch (Exception e) {
                        if (response.message().isEmpty() && failSum < MaxFailCount) {
                            Log.d(TAG, "onResponse: 重试第" + failSum + "次");
                            failSum++;
                            call.clone().enqueue(getNewCallBack(toResult));
                        } else {
                            failSum = 0;
                            Log.d(TAG, "onResponse: 重试依然失败 > "+call.request().url());
                        }
                        e.printStackTrace();
                    }
                });
            }
        };
    }


}
