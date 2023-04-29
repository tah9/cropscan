package com.nuist.cropscan.request;

/**
 * ->  tah9  2023/4/29 15:51
 */
public class CropConfig {

    //bitmap去除顶部和底部部分，只保留中间区域
    public static float CropViewClipTopScale = 0.15f;
    public static float CropViewHeightScale = 0.75f;


    //最多框选个数，减轻服务器识别压力
    public static int MaxCropCount = 3;
}
