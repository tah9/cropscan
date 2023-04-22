package com.nuist.cropscan;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import androidx.fragment.app.FragmentTransaction;

import com.nuist.cropscan.base.BaseAct;
import com.nuist.cropscan.base.FragWeb;
import com.nuist.cropscan.dialog.DownLoadDialog;
import com.nuist.cropscan.request.BASEURL;
import com.nuist.cropscan.request.FileConfig;
import com.nuist.cropscan.request.HttpOk;
import com.nuist.cropscan.tool.FileUtils;
import com.nuist.cropscan.tool.LoadingDialogUtils;
import com.nuist.cropscan.tool.ZipUtils;

import org.json.JSONObject;

import java.io.File;

public class ActWeb extends BaseAct {
    FragWeb fragWeb;


    private void loadPage(int newVersion) {
        if (!optString("user").isEmpty()) {
            startActivity(new Intent(ActWeb.this, HomeAct.class));
            finish();
        }
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragWeb = new FragWeb(FileConfig.webFileHome(context));
        fragmentTransaction.replace(R.id.frame, fragWeb).commit();
        setInt(getResources().getString(R.string.web_version), newVersion);
    }

    private void checkAppVersion(JSONObject o) throws Exception {
        JSONObject androidJson = o.optJSONObject("Android");
        //获取软件版本号，对应AndroidManifest.xml下android:versionCode
        int versionCode = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
        int netAppVersion = androidJson.optInt("VersionCode");
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
        JSONObject webJson = o.optJSONObject("Web");
        int netVersion = webJson.optInt("Version");
        Log.d(TAG, "localWebVersion: " + localVersion);
        Log.d(TAG, "netVersion: " + netVersion);
        if (netVersion > localVersion) {
            FileUtils.deleteDir(new File(getFilesDir().getAbsolutePath()));
            String[] list = new File(getFilesDir().getAbsolutePath()).list();
            for (String s : list) {
                Log.d(TAG, "fileList: " + s);
            }
            String zipPath = getFilesDir().getAbsolutePath() + "/dist.zip";
            DownLoadDialog downLoadDialog = new DownLoadDialog(context,
                    BASEURL.entireHost + "/download/AppWeb",
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
        HttpOk.getInstance().to("/version", o -> {
            checkAppVersion(o);
            checkWebVersion(o);
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
        checkVersion();
    }

    // 第一次按下返回键的事件
    private long firstPressedTime;

    @Override
    public void onBackPressed() {
        if (fragWeb.getWebView().getUrl().equals("file:///android_asset/dist/index.html#/main/home") || fragWeb.getWebView().getUrl().equals("file:///android_asset/dist/index.html#/main/mine")) {
            if (System.currentTimeMillis() - firstPressedTime < 2000) {
                super.onBackPressed();
            } else {
                Toast.makeText(ActWeb.this, "再按一次退出", Toast.LENGTH_SHORT).show();
                firstPressedTime = System.currentTimeMillis();
            }
        } else {
            fragWeb.getWebView().loadUrl("javascript:backPressed()");
        }
    }
}