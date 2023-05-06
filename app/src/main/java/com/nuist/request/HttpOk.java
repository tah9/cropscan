package com.nuist.request;

import static com.nuist.request.BASEURL.entireHost;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

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

    private int MAX_TIME = 60 * 60;//一小时

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


    public Call postOwnerUrlFormData(Map<String, Object> map, String url, HttpResult result) {
        MultipartBody.Builder fm = new MultipartBody.Builder().setType(MultipartBody.FORM);
        for (String key : map.keySet()) {
            fm.addFormDataPart(key, (String) map.get(key));
        }
        return postTo(new Request.Builder().post(fm.build()), BASEURL.entireHost + url, result);
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
        Request request = new Request.Builder()
                .url(url + "?access_token=" + token)
                .method("POST", body)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .addHeader("Accept", "application/json")
                .build();
        this.okHttpClient.newCall(request).enqueue(newCallBack(HttpResult));
    }


    public void toOtherUrl(String url, HttpResult HttpResult) {
        getTo(url, HttpResult);
    }

    public void toOwnerUrl(String url, HttpResult HttpResult) {
        getTo(entireHost + url, HttpResult);
    }

    public Call postToOwnerUrl(Map<String, Object> map, String url, HttpResult HttpResult) {
        return postTo(new Request.Builder().post(HttpOk.jsonBody(new JSONObject(map))), entireHost + url, HttpResult);
    }

    public Call postToOtherUrl(Map<String, Object> map, String url, HttpResult HttpResult) {
        return postTo(new Request.Builder().post(HttpOk.jsonBody(new JSONObject(map))), url, HttpResult);
    }

    private Call postTo(Request.Builder builder, String url, HttpResult HttpResult) {
        Log.d(TAG, "postTo: " + url);
        Call call = okHttpClient.newCall(builder.url(url).build());
        call.enqueue(newCallBack(HttpResult));
        return call;
    }

    private void getTo(String url, HttpResult HttpResult) {
        Log.d(TAG, "getTo: " + url);
        okHttpClient.newCall(new Request.Builder().url(url).build()).enqueue(newCallBack(HttpResult));
    }


    private Callback newCallBack(HttpResult toResult) {
        return new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String json = response.body().string();
                //获取session的操作，session放在cookie头，且取出后含有“；”,要处理一下
//                Headers headers = response.headers();
//                List<String> cookies = headers.values("Set-Cookie");
//                for (String cookie : cookies) {
//                    Log.d(TAG, "onResponse: cookie"+cookie);
//                }
//                try {
//                    String s = cookies.get(0);
//                    String session = s.substring(0, s.indexOf(";"));
//                    Log.d(TAG, "onResponse: s"+s);
//                    Log.d(TAG, "onResponse: session"+session);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
                handler.post(() -> {
                    try {
                        toResult.promise(new JSONObject(json));
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.d(TAG, "onResponse: json转化失败");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        };
    }


}
