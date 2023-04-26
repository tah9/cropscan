package com.nuist.cropscan.ActPicture;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.common.util.concurrent.ListenableFuture;
import com.nuist.cropscan.R;
import com.nuist.cropscan.base.BaseAct;
import com.nuist.cropscan.camera.example.adapter.CameraSimpleAda;
import com.nuist.cropscan.request.HttpOk;
import com.nuist.cropscan.tool.ImgTool;
import com.nuist.cropscan.view.BoxImageView;

import org.json.JSONArray;

public class ActCameraX extends BaseAct {
    private static final String TAG = "ActCameraX";
    private PreviewView cameraView;
    ImageCapture imageCapture;
    Camera camera;
    private ImageView btnSwitchLens2;

    private ImageView btn_capture;
    private ImageView btnSwitchLens;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private ProcessCameraProvider cameraProvider;
    private Preview preview;
    public BoxImageView picMask;
    private RecyclerView tipRecy;
    public TextView tipTv;



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 99) {
            String path = data.getStringExtra("path");
            Log.d(TAG, "onActivityResult: " + path);
            Glide.with(context).asBitmap().load(path)
                    .into(new CustomTarget<Bitmap>() {
                              @Override
                              public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                  clickCapture(resource);
                              }

                              @Override
                              public void onLoadCleared(@Nullable Drawable placeholder) {

                              }
                          }
                    );
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setLightStatusBar(getWindow(), false, Color.parseColor("#303030"));
        setContentView(R.layout.activity_act_camera_x);

        initView();
        try {
            Glide.with(btnSwitchLens2).load(ImgTool.getLatestPhoto(context).second).into(btnSwitchLens2);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        initCamera();

//        setWH();
    }

    protected void onCameraCreate() {

    }

    private boolean onCameraCreate;


    protected void initCamera() {

        cameraProviderFuture = ProcessCameraProvider.getInstance(context);
        cameraProviderFuture.addListener(() -> {
            try {
                //将相机的生命周期和activity的生命周期绑定，camerax 会自己释放
                cameraProvider = cameraProviderFuture.get();
                Log.d(TAG, "initCamera: cameraProvider");
                preview = new Preview.Builder().build();
                //创建图片的 capture
                imageCapture = new ImageCapture.Builder()
                        .setFlashMode(ImageCapture.FLASH_MODE_OFF)
                        .build();
                Log.d(TAG, "initCamera: openCamera");
                openCamera();
                preview.setSurfaceProvider(cameraView.getSurfaceProvider());

                onCameraCreate();
                onCameraCreate = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(context));
    }

    private int curLens = CameraSelector.LENS_FACING_BACK;

    /*
    param:cameraSelector int
     */
    protected void openCamera() {
        //选择摄像头
        CameraSelector cameraSelector = new CameraSelector.Builder().requireLensFacing(curLens).build();

        // Unbind use cases before rebinding
        cameraProvider.unbindAll();

        // Bind use cases to camera
        //参数中如果有mImageCapture才能拍照，否则会报下错
        //Not bound to a valid Camera [ImageCapture:androidx.camera.core.ImageCapture-bce6e930-b637-40ee-b9b9-
        camera = cameraProvider.bindToLifecycle(ActCameraX.this, cameraSelector, preview, imageCapture);
    }


    public void clickCapture(Bitmap bitmap) {
    }

    ;


    @SuppressLint("RestrictedApi")
    private void initView() {
        tipTv = findViewById(R.id.tipTv);
        btnSwitchLens2 = findViewById(R.id.btn_switch_lens2);
        tipRecy = findViewById(R.id.tip_recy);
        btnSwitchLens2.setOnClickListener(view -> {
            startActivityForResult(new Intent(ActCameraX.this, ActGallery.class), 99);
        });

        cameraView = (PreviewView) findViewById(R.id.act_cameraTest_pv_cameraPreview);
//        cameraView.setImplementationMode(PreviewView.ImplementationMode.PERFORMANCE);
        btn_capture = (ImageView) findViewById(R.id.btn_capture);
        btn_capture.setOnClickListener(v -> {
            if (!onCameraCreate) return;
            clickCapture(cameraView.getBitmap());
        });
        btnSwitchLens = (ImageView) findViewById(R.id.btn_switch_lens);
        btnSwitchLens.setOnClickListener(v -> {
            curLens = curLens == CameraSelector.LENS_FACING_BACK ? CameraSelector.LENS_FACING_FRONT : CameraSelector.LENS_FACING_BACK;
            picMask.setImageBitmap(cameraView.getBitmap());
            picMask.bringToFront();
            openCamera();
            new Handler().postDelayed(() -> {
                picMask.setImageBitmap(null);
            }, 1000);
        });
        picMask = findViewById(R.id.pic_mask);

        HttpOk.getInstance().to("/simpleImg", o -> {
            JSONArray rows = o.optJSONArray("rows");
            Log.d(TAG, "initView: " + rows);
            if (rows != null && rows.length() != 0) {
                tipRecy.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
                tipRecy.setAdapter(new CameraSimpleAda(context, rows).setOnClickSimPic(this::clickCapture));
                tipRecy.bringToFront();
            }

        });
    }
}