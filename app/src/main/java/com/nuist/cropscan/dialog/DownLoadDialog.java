package com.nuist.cropscan.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Window;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.nuist.cropscan.R;
import com.nuist.cropscan.view.NumProgressView;
import com.nuist.request.DownloadListener;
import com.nuist.request.HttpOk;

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


    private void setSetting() {
        setCancelable(false);
        setCanceledOnTouchOutside(false);//点击外部Dialog不会消失
        Window window = getWindow();
        window.setGravity(Gravity.CENTER);//设置dialog显示居中
        window.setBackgroundDrawable(new ColorDrawable(0x00000000));
    }

    private String byte2Other(long length) {
        long size = length / 1024;
        if (size < 1024) {
            return size + "KB";
        } else {
            return size / 1024 + "." + size % 1024 % 100 + "MB";
        }
    }

    long totalSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setSetting();
        setContentView(R.layout.dialog_download);
        initView();
        HttpOk.getInstance().download(downUrl, savePath, new DownloadListener() {
            @Override
            public void Downloading(int progress) {
                String fileInfo = byte2Other((long) (totalSize * (progress / 100f)));
                info.setText(fileInfo);
                progressView.setProgress(progress);
            }

            @Override
            public void End() {
                Log.d(TAG, "download success");
                dismiss();
            }

            @Override
            public void FileSize(long size) {
                totalSize = size;
                name.setText("资源大小" + byte2Other(size));
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
