package com.nuist.cropscan.request;


import android.app.Activity;
import android.content.Context;
import android.util.Log;

import java.io.File;

/**
 * ->  tah9  2023/4/17 20:48
 */
public class BASEURL {
    private static final String TAG = "BASEURL";
    public static String ip = "http://192.168.43.205";
    //    public static String ip = "http://149.28.194.155";
    public static String servePort = "8087";
//    public static String webPort = "9277";
    public static String webPort = "9009";
    public static String entireHost = ip + ":" + servePort + "/api";

    public static String entireWebHost = ip + ":" + webPort;

    public static String flaskHost = ip + ":5000";


    public static String picUrl(Object name) {
        return entireHost + "/static/plant/" + name + ".jpg";
    }


}
