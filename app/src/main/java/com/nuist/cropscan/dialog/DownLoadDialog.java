package com.nuist.cropscan.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.arialyy.annotations.Download;
import com.arialyy.aria.core.Aria;
import com.arialyy.aria.core.task.DownloadTask;
import com.arialyy.aria.core.task.ITask;
import com.arialyy.aria.util.CommonUtil;
import com.nuist.cropscan.R;
import com.nuist.cropscan.view.NumProgressView;

/**
 * ->  tah9  2023/4/20 13:41
 */
public class DownLoadDialog extends Dialog {
    private static final String TAG = "DownLoadDialog";
    private String downUrl, savePath;
    private Context context;

    public DownLoadDialog(@NonNull Context context, String downUrl, String savePath) {
        super(context);
        Aria.download(this).register();
        // 修改最大下载数，调用完成后，立即生效
        Aria.get(context).getDownloadConfig()
                .setThreadNum(1)
//                .setMaxSpeed(1024)
                .setUpdateInterval(50)
                .setConvertSpeed(true);

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

    @Download.onTaskPre
    public void onTaskPre(DownloadTask task) {
        name.setText(CommonUtil.formatFileSize(task.getFileSize()));
    }

    @Download.onTaskRunning
    public void onTaskRunning(DownloadTask task) {
        int p = task.getPercent();    //任务进度百分比
        String speed = task.getConvertSpeed();    //转换单位后的下载速度，单位转换需要在配置文件中打开
        info.setText(speed);
        progressView.setProgress(p);
    }
    @Download.onTaskComplete
    public void onTaskComplete(DownloadTask task) {
        Log.d(TAG, "onTaskComplete: ");
        progressView.setProgress(100);
        info.setVisibility(View.INVISIBLE);
        dismiss();
    }

    private void setSetting() {
        setCanceledOnTouchOutside(false);//点击外部Dialog不会消失
        Window dialogWindow = getWindow();
        dialogWindow.setGravity(Gravity.CENTER);//设置dialog显示居中
//        dialogWindow.setWindowAnimations();设置动画效果


//        WindowManager windowManager = ((Activity)context).getWindowManager();
//        Display display = windowManager.getDefaultDisplay();
//        WindowManager.LayoutParams lp = getWindow().getAttributes();
//        lp.width = display.getWidth()*4/5;// 设置dialog宽度为屏幕的4/5
//        getWindow().setAttributes(lp);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setSetting();
        setContentView(R.layout.dialog_load);
        initView();

        long taskId = Aria.download(this)
                .load(downUrl)     //读取下载地址
                .setFilePath(savePath) //设置文件保存的完整路径
                .create();   //创建并启动下载

    }


    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        Log.d(TAG, "onDetachedFromWindow: ");
        Aria.download(this).unRegister();
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
