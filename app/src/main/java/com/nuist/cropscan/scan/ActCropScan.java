package com.nuist.cropscan.scan;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.camera.core.CameraSelector;
import androidx.camera.view.PreviewView;

import com.bumptech.glide.Glide;
import com.nuist.cropscan.ActPicture.ActCameraX;
import com.nuist.cropscan.R;
import com.nuist.cropscan.Tools;
import com.nuist.cropscan.request.HttpOk;

/**
 * ->  tah9  2023/3/23 19:41
 */
public class ActCropScan extends ActCameraX {
    private static final String TAG = "ActCropScan";
    public Context context = this;

    @Override
    protected void onCameraCreate() {
        Log.d(TAG, "onCameraCreate: ");
        openCamera();
    }

    @Override
    public void clickCapture(PreviewView previewView) {
        Bitmap bitmap = previewView.getBitmap();
//            screenImage.setImageBitmap(bitmap);
        String path = Tools.saveBitmapFile(context, bitmap);
        Log.d(TAG, "initView: " + path);
        Dialog dialog = new Dialog(context);
        View dialogRoot = LayoutInflater.from(context).inflate(R.layout.dialogresult, null);
        dialog.setContentView(dialogRoot);
        TextView title = dialogRoot.findViewById(R.id.dialogtitle);
        ImageView resultPic = dialogRoot.findViewById(R.id.dialogpic);
        resultPic.setImageBitmap(bitmap);

        dialog.getWindow().setGravity(Gravity.CENTER);

        dialog.show();
        HttpOk.getInstance().setPostFile(path).to("/classify", o -> {
            Log.d(TAG, "initView: " + o.toString());
            Glide.with(context).load(o.getString("path")).into(resultPic);
            title.setText("识别完成");
//                dialog.dismiss();
        });
    }
}
