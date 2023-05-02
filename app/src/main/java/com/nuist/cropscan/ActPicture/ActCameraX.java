package com.nuist.cropscan.ActPicture;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.common.util.concurrent.ListenableFuture;
import com.nuist.cropscan.R;
import com.nuist.cropscan.base.BaseAct;
import com.nuist.cropscan.tool.ImgTool;

public abstract class ActCameraX extends BaseAct {
    private static final String TAG = "ActCameraX";
    public PreviewView cameraView;
    public ImageCapture imageCapture;
    public Camera camera;
    public ImageView btnToGallery;

    public ImageView btn_capture;
    public ImageView btnSwitchLens;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private ProcessCameraProvider cameraProvider;
    public Preview preview;
    public ConstraintLayout bottomControllerLayout;
    public View btnBack;
    public boolean openCamera = false;

    @SuppressLint("RestrictedApi")
    @Override
    protected void onResume() {
        super.onResume();
        if (preview != null && preview.getCamera() != null && !openCamera) {
            switchCamera(false);
        }
    }

    @SuppressLint("RestrictedApi")
    public void switchCamera(boolean open) {
        this.openCamera = open;
        if (preview == null || preview.getCamera() == null) {
            return;
        }
        if (open) {
            preview.getCamera().open();
        } else {
            preview.getCamera().close();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 99 && data != null) {
            String path = data.getStringExtra("path");
            Log.d(TAG, "onActivityResult: " + path);
            CustomTarget<Bitmap> customTarget = new CustomTarget<Bitmap>() {

                @Override
                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                    clickCapture(resource);
                }

                @Override
                public void onLoadCleared(@Nullable Drawable placeholder) {

                }
            };
            Glide.with(context).asBitmap().load(path).into(customTarget);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setLightStatusBar(getWindow(), false, Color.parseColor("#303030"));
    }


    //必须设置
    protected void initCameraX() {

        ConstraintLayout root = (ConstraintLayout) LayoutInflater.from(context).inflate(R.layout.activity_act_camera_x, null);

        setContentView(root);

        initView();

        Glide.with(btnToGallery).load(ImgTool.getLatestPhoto(context).second).into(btnToGallery);

//        if (!openCamera) {
//
//        } else {
        initCamera();
//        }
    }

    private boolean onCameraCreate;


    private void initCamera() {

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
        openCamera = true;
        //选择摄像头
        CameraSelector cameraSelector = new CameraSelector.Builder().requireLensFacing(curLens).build();

        // Unbind use cases before rebinding
        cameraProvider.unbindAll();

        // Bind use cases to camera
        //参数中如果有mImageCapture才能拍照，否则会报下错
        //Not bound to a valid Camera [ImageCapture:androidx.camera.core.ImageCapture-bce6e930-b637-40ee-b9b9-
        camera = cameraProvider.bindToLifecycle(ActCameraX.this, cameraSelector, preview, imageCapture);
    }


    public abstract void clickCapture(Bitmap bitmap);

    ;


    private void initView() {
        Window window = getWindow();
        //使得布局延伸到状态栏和导航栏区域
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);

        //透明状态栏/导航栏
        window.setStatusBarColor(Color.argb(50, 0, 0, 0));
        window.setNavigationBarColor(Color.TRANSPARENT);
        //这样的效果跟上述的主题设置效果类似

        btnToGallery = findViewById(R.id.btn_to_gallery);
        btnToGallery.setOnClickListener(view -> {
            startActivityForResult(new Intent(ActCameraX.this, ActGallery.class), 99);
        });
        btnBack = findViewById(R.id.back_btn);
        btnBack.setOnClickListener(v -> {
            finish();
        });


        bottomControllerLayout = findViewById(R.id.bottom_controller_layout);
        cameraView = findViewById(R.id.act_cameraTest_pv_cameraPreview);
        btn_capture = findViewById(R.id.btn_capture);
        btn_capture.setOnClickListener(v -> {
            if (!onCameraCreate) return;
            clickCapture(cameraView.getBitmap());
        });
        btnSwitchLens = findViewById(R.id.btn_switch_lens);
        btnSwitchLens.setOnClickListener(v -> {
            curLens = curLens == CameraSelector.LENS_FACING_BACK ? CameraSelector.LENS_FACING_FRONT : CameraSelector.LENS_FACING_BACK;
            openCamera();
        });
    }

}