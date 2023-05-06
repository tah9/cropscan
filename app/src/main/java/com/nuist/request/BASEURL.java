package com.nuist.request;


/**
 * ->  tah9  2023/4/17 20:48
 */
public class BASEURL {
    private static final String TAG = "BASEURL";
    /*
    打包注意检查ip和webport
     */
    /*
    开发环境
     */
//    public static String ip = "http://192.168.43.205";
    //    public static String webPort = "9009";
    public static String entireWebHost = "http://192.168.43.205:9009";


    /*
    打包环境
     */
    public static String ip = "http://149.28.194.155";
//    public static String webPort = "9277";
    //    public static String entireWebHost = ip + ":" + webPort;


    /*
    不变的
     */
    public static String flaskHost = ip + ":5000";
    public static String servePort = "8087";
    public static String entireHost = ip + ":" + servePort + "/api";



    public static String picUrl(Object name) {
        return entireHost + "/static/plant/" + name + ".jpg";
    }


}
