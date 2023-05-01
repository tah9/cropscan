package com.nuist.cropscan.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nuist.cropscan.R;
import com.nuist.cropscan.camera.example.adapter.CameraSimpleAda;
import com.nuist.cropscan.request.HttpOk;

import org.json.JSONArray;

/**
 * ->  tah9  2023/4/28 12:31
 */
public class CropTipsDialog {
    private static final String TAG = "CropTipsDialog";
    public final Dialog dialog;
    public JSONArray rows;
    public LinearLayoutManager linearLayoutManager;
    public RecyclerView tipRecy;
    public CameraSimpleAda cameraSimpleAda;

    private windowDialogListener listener;

    public CropTipsDialog(Activity activity, windowDialogListener listener) {
        this.listener = listener;
        //1、使用Dialog、设置style
        dialog = new Dialog(activity, R.style.DialogCropBottomTips);
        //2、设置布局
        View root = View.inflate(activity, R.layout.dialog_crop_tip, null);
        dialog.setContentView(root);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);

        Window window = dialog.getWindow();
        window.setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        //设置弹出位置
        window.setGravity(Gravity.CENTER_VERTICAL);
        //设置弹出动画
        window.setWindowAnimations(R.style.main_menu_animStyle);
        //设置对话框大小
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.show();
//        int orientation = activity.getResources().getConfiguration().orientation;
//        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
//            //设置弹出位置
        window.setGravity(Gravity.BOTTOM);
//        } else if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
////            // 当前为横屏
//            window.setGravity(Gravity.RIGHT);
//        }


        tipRecy = root.findViewById(R.id.tip_recy);

        root.findViewById(R.id.slide_btn).setOnClickListener(v -> {
            dialog.dismiss();
            listener.onDismiss();
        });
        HttpOk.getInstance().toOwnerUrl("/simpleImg", o -> {
            Log.d(TAG, "CropTipsDialog: " + o);
            rows = o.optJSONArray("rows");
            if (rows != null && rows.length() != 0) {
                linearLayoutManager = new LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false);
                tipRecy.setLayoutManager(linearLayoutManager);

                cameraSimpleAda = new CameraSimpleAda(activity, rows).setOnClickSimPic(resource -> {
                    dialog.dismiss();
                    listener.onSelect(resource);
                });
                tipRecy.setAdapter(cameraSimpleAda);
            }
        });
    }

    public interface windowDialogListener {
        /**
         * 选中图片
         */
        void onSelect(Bitmap bitmap);


        /**
         * 取消
         */
        void onDismiss();
    }

    public void toDismiss() {
        if (dialog != null) {
            dialog.dismiss();
            listener.onDismiss();
        }
    }
}