package com.nuist.cropscan;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.webkit.WebView;
import android.widget.Toast;

import androidx.fragment.app.FragmentTransaction;

import com.nuist.cropscan.base.BaseAct;
import com.nuist.cropscan.base.FragWeb;
import com.nuist.cropscan.dialog.DownLoadDialog;
import com.nuist.cropscan.request.BASEURL;
import com.nuist.cropscan.request.FileConfig;
import com.nuist.cropscan.request.HttpOk;
import com.nuist.cropscan.tool.FileUtils;
import com.nuist.cropscan.dialog.LoadingDialogUtils;
import com.nuist.cropscan.tool.LocalGps;
import com.nuist.cropscan.tool.ZipUtils;

import org.json.JSONObject;

import java.io.File;

public class ActWeb extends BaseAct {
    FragWeb fragWeb;


    private void loadPage(int newVersion) {
        setLightStatusBar(getWindow(), true, getResources().getColor(R.color.main));
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragWeb = new FragWeb(FileConfig.webFileHome(context));
        fragmentTransaction.replace(R.id.frame, fragWeb).commit();
        setInt(getResources().getString(R.string.web_version), newVersion);
        int localVersion = optInt(getResources().getString(R.string.web_version));
        Log.d(TAG, "localWebVersion: " + localVersion);
    }

    private void checkAppVersion(JSONObject o) throws Exception {
        //获取软件版本号，对应AndroidManifest.xml下android:versionCode
        int versionCode = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
        int netAppVersion = o.optInt("androidVersionCode");
        Log.d(TAG, "AppVersionCode: " + versionCode);
        Log.d(TAG, "netAppVersion: " + netAppVersion);

        if (netAppVersion > versionCode) {
            Toast toast = Toast.makeText(context, "版本过低，请更新版本!", Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
            Uri uri = Uri.parse("http://149.28.194.155:9001");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
            finish();
            throw new Exception();
        }
    }

    private void checkWebVersion(JSONObject o) {
        int localVersion = optInt(getResources().getString(R.string.web_version));
        int netVersion = o.optInt("webVersion");
        Log.d(TAG, "localWebVersion: " + localVersion);
        Log.d(TAG, "netVersion: " + netVersion);
        if (netVersion > localVersion) {
            //清空已下载文件
            FileUtils.deleteDir(new File(getFilesDir().getAbsolutePath()));

            String zipPath = getFilesDir().getAbsolutePath() + "/dist+" + netVersion + ".zip";
            DownLoadDialog downLoadDialog = new DownLoadDialog(context,
                    BASEURL.entireHost + "/static/mobile/android/dist.zip",
                    zipPath);
            downLoadDialog.show();
            downLoadDialog.setOnDismissListener(dialogInterface -> {
                LoadingDialogUtils.show(this);
                ZipUtils.UnZipFolderDelOri(zipPath,
                        FileConfig.webFilePath(context));
                LoadingDialogUtils.dismiss();
                loadPage(netVersion);
            });
        } else {
            loadPage(netVersion);
        }
    }

    private void checkVersion() {
        HttpOk.getInstance().toOwnerUrl("/version/latest", o -> {
            JSONObject versionData = o.optJSONObject("data");
            checkAppVersion(versionData);
            checkWebVersion(versionData);
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.framelayout);
    }

    @Override
    public void afterPermission() {
        super.afterPermission();
//        checkVersion();
        new LocalGps(this);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragWeb = new FragWeb(BASEURL.entireWebHost);
        fragmentTransaction.replace(R.id.frame, fragWeb).commit();
    }

    // 第一次按下返回键的事件
    private long firstPressedTime;

    @Override
    public void onBackPressed() {
        WebView webView = fragWeb.getWebView();
        Log.d(TAG, "onBackPressed: " + webView.getUrl());
        if (webView.getUrl().equals("file://" + FileConfig.webFileHome(context) + "#/home")
                || webView.getUrl().equals("file://" + FileConfig.webFileHome(context) + "#/login")
                || webView.getUrl().equals(BASEURL.entireWebHost+"/#/home")
                || webView.getUrl().equals(BASEURL.entireWebHost+"/#/login")

        ) {
            Log.d(TAG, "onBackPressed: 符合");
            if (System.currentTimeMillis() - firstPressedTime < 2000) {
                super.onBackPressed();
            } else {
                Toast.makeText(ActWeb.this, "再按一次退出", Toast.LENGTH_SHORT).show();
                firstPressedTime = System.currentTimeMillis();
            }
        } else {
            webView.loadUrl("javascript:backPressed()");
        }
    }
}