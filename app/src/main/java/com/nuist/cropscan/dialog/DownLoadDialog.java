package com.nuist.cropscan.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Window;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.nuist.cropscan.HomeAct;
import com.nuist.cropscan.R;
import com.nuist.cropscan.request.FileConfig;
import com.nuist.cropscan.tool.ZipUtils;
import com.nuist.cropscan.view.NumProgressView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * ->  tah9  2023/4/20 13:41
 */
public class DownLoadDialog extends Dialog {
    private static final String TAG = "DownLoadDialog";
    private String downUrl, savePath;
    private Context context;

    public DownLoadDialog(@NonNull Context context, String downUrl, String savePath) {
        super(context);
        this.downUrl = downUrl;
        this.savePath = savePath;
        this.context = context;
    }

    public DownLoadDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    protected DownLoadDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

//        name.setText(CommonUtil.formatFileSize(task.getFileSize()));

//
//    @Download.onTaskComplete
//    public void onTaskComplete(DownloadTask task) {
//        Log.d(TAG, "onTaskComplete: ");
//        progressView.setProgress(100);
//        info.setVisibility(View.INVISIBLE);
//        dismiss();
//    }

    private void setSetting() {
        setCanceledOnTouchOutside(false);//点击外部Dialog不会消失
        Window dialogWindow = getWindow();
        dialogWindow.setGravity(Gravity.CENTER);//设置dialog显示居中
    }

    private String byte2Other(long length) {
        long size = length / 1024;
        if (size < 1024) {
            return size + "KB";
        } else {
            return size / 1024 + "MB";
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setSetting();
        setContentView(R.layout.dialog_download);
        initView();
        OkHttpClient okHttpClient = new OkHttpClient();

        Request request = new Request.Builder().url(downUrl).build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // 下载失败
                e.printStackTrace();
                Log.d(TAG, "download failed");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                InputStream is = null;
                byte[] buf = new byte[2048];
                int len = 0;
                FileOutputStream fos = null;
                // 储存下载文件的目录
                try {
                    is = response.body().byteStream();
                    long total = response.body().contentLength();
                    info.post(() -> {
                        name.setText("文件大小：" + byte2Other(total));
                    });
                    File downloadFile = new File(savePath);
                    fos = new FileOutputStream(downloadFile);
                    long sum = 0;
                    while ((len = is.read(buf)) != -1) {
                        fos.write(buf, 0, len);
                        sum += len;
                        int p = (int) (sum * 1.0f / total * 100);
                        String fileInfo = byte2Other(downloadFile.length());
                        info.post(() -> {
                            info.setText(fileInfo);
                            progressView.setProgress(p);
                        });
                    }
                    fos.flush();
                    Log.d(TAG, "download success");

                    info.post(() -> {
                        LoadingDialogUtils.show(((HomeAct) context));
                    });
                    ZipUtils.UnZipFolderDelOri(downloadFile.getAbsolutePath(),
                            FileConfig.webFileSavePath(context));
                    info.post(() -> {
                        LoadingDialogUtils.dismiss();
                        dismiss();
                    });
                    // 下载完成
//                    listener.onDownloadSuccess();
//                    Log.i("DOWNLOAD", "totalTime=" + (System.currentTimeMillis() - startTime));
                } catch (Exception e) {
                    e.printStackTrace();
//                    listener.onDownloadFailed();
                    Log.d(TAG, "download failed");
                } finally {
                    try {
                        if (is != null)
                            is.close();
                    } catch (IOException e) {
                    }
                    try {
                        if (fos != null)
                            fos.close();
                    } catch (IOException e) {
                    }
                }
            }
        });

    }


    NumProgressView progressView;
    TextView title, name, info;

    private void initView() {
        progressView = findViewById(R.id.progressView);
        title = findViewById(R.id.title);
        name = findViewById(R.id.name);
        info = findViewById(R.id.info);
    }
}
