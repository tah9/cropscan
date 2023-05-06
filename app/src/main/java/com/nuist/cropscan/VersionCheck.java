package com.nuist.cropscan;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.core.content.FileProvider;

import com.nuist.cropscan.base.BaseAct;
import com.nuist.cropscan.dialog.DownLoadDialog;
;
import com.nuist.request.BASEURL;
import com.nuist.request.DownloadListener;
import com.nuist.request.HttpOk;
import com.nuist.tool.dialog.LoadingDialogUtils;
import com.nuist.tool.dialog.SnackUtil;
import com.nuist.tool.file.ZipUtils;
import com.nuist.webview.FileConfig;

import org.json.JSONObject;

import java.io.File;

/**
 * ->  tah9  2023/5/6 12:38
 */
public class VersionCheck {
    private BaseAct context;
    private static final String TAG = "VersionCheck";

    public interface AfterCheckListener {
        void end();
    }

    private AfterCheckListener listener;


    public VersionCheck(BaseAct context, AfterCheckListener m_listener) {
        this.context = context;
        this.listener = m_listener;
        HttpOk.getInstance().toOwnerUrl("/version/latest", o -> {
            JSONObject versionData = o.optJSONObject("data");
            check1AppVersion(versionData);
        });
    }


    private void installApk(File apkFile) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Uri apkUri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".FileProvider", apkFile);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
        } else {
            intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
        }
        context.startActivity(intent);
    }

    private void check1AppVersion(JSONObject o) throws Exception {
        //获取软件版本号，对应AndroidManifest.xml下android:versionCode
        int versionCode = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
        int netAppVersion = o.optInt("androidVersionCode");
        Log.d(TAG, "AppVersionCode: " + versionCode);
        Log.d(TAG, "netAppVersion: " + netAppVersion);

        if (netAppVersion > versionCode) {
            SnackUtil.showIndefinite(context.getWindow().getDecorView(), "版本过低!");


            String downloadUrl = BASEURL.entireHost + "/static/mobile/android/app-release.apk";
            String[] split = downloadUrl.split("/");
            String savePath = context.getFilesDir().getAbsolutePath() + "/" + split[split.length - 1];
            File saveFile = new File(savePath);

             /*
            若本地有apk，通过比对服务器和本地文件大小判断当前文件是否完整
            （暂定方案，严格来说应该做md5校验）
             */
            if (saveFile.exists()) {
                compareFile(downloadUrl, saveFile);
            } else {
                beginDownloadNewApk(downloadUrl, saveFile);
            }
        } else {
            check2WebVersion(o);
        }
    }

    private void compareFile(String downloadUrl, File saveFile) {
        HttpOk.getInstance().download(downloadUrl, null, new DownloadListener() {
            @Override
            public void FileSize(long size) {
                if (saveFile.length() == size) {
                    // 不需要下载，本地已有完整文件，直接弹出安装
                    installApk(saveFile);
                } else {
                    beginDownloadNewApk(downloadUrl, saveFile);
                }
            }
        });
    }

    //    需要下载apk，完成后弹出安装窗口
    private void beginDownloadNewApk(String downloadUrl, File saveFile) {
        DownLoadDialog downLoadDialog = new DownLoadDialog(
                context,
                downloadUrl,
                saveFile.getAbsolutePath()
        );
        downLoadDialog.setOnDismissListener(dialog -> {
            installApk(saveFile);
        });

        downLoadDialog.show();
    }

    private void check2WebVersion(JSONObject o) {
        int localVersion = context.optInt(context.getResources().getString(R.string.web_version));
        int netVersion = o.optInt("webVersion");
        Log.d(TAG, "localWebVersion: " + localVersion);
        Log.d(TAG, "netVersion: " + netVersion);
        if (netVersion > localVersion) {
            String savePath = context.getFilesDir().getAbsolutePath() + "/dist.zip";
            DownLoadDialog downLoadDialog = new DownLoadDialog(context,
                    BASEURL.entireHost + "/static/mobile/android/dist.zip",
                    savePath);
            downLoadDialog.show();

            downLoadDialog.setOnDismissListener(dialog -> {
                LoadingDialogUtils.show(((HomeAct) context));

                ZipUtils.UnZipFolderDelOri(savePath,
                        FileConfig.webFileSavePath(context));

                LoadingDialogUtils.dismiss();
                setNewWebVersion(netVersion);
                listener.end();
            });
        } else {
            listener.end();
        }
    }

    private void setNewWebVersion(int newVersion) {
        context.setInt(context.getResources().getString(R.string.web_version), newVersion);
        int localVersion = context.optInt(context.getResources().getString(R.string.web_version));
        Log.d(TAG, "setNewWebVersion > localWebVersion: " + localVersion);
    }
}
