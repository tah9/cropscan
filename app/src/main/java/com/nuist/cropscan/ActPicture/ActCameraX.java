package com.nuist.cropscan.ActPicture;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.Size;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;
import com.nuist.cropscan.R;
import com.nuist.cropscan.Tools;

public abstract class ActCameraX extends AppCompatActivity {
    public Context context = this;
    private static final String TAG = "ActCameraX";
    private PreviewView cameraView;
    ImageCapture imageCapture;
    Camera camera;
    private ImageView btn_capture;
    private ImageView btnSwitchLens;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private ProcessCameraProvider cameraProvider;
    private Preview preview;
    private ImageView picMask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_act_camera_x);

        initView();

        initCamera();

//        setWH();
    }

    protected abstract void onCameraCreate();

    private boolean onCameraCreate;

    private void setWH() {
        // 获取 CameraManager 对象
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

// 获取当前摄像头的 ID
        String cameraId = String.valueOf(CameraCharacteristics.LENS_FACING_BACK); // 后置摄像头
        CameraCharacteristics characteristics = null;
        try {
            characteristics = cameraManager.getCameraCharacteristics(cameraId);
            // 获取当前摄像头的画幅信息
            StreamConfigurationMap streamConfigurationMap = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            Size[] outputSizes = streamConfigurationMap.getOutputSizes(SurfaceTexture.class);
            float aspectRatio = (float) outputSizes[0].getWidth() / outputSizes[0].getHeight();
            int width = Tools.getWidth(context);
            int lenWidth = outputSizes[0].getWidth();
            int lenHeight = outputSizes[0].getHeight();
            float scaleSize = (float) lenWidth / width;
            ViewGroup.LayoutParams layoutParams = cameraView.getLayoutParams();
            Log.d(TAG, "setWH: " + scaleSize);
//            layoutParams.width = previewViewWidth;
            layoutParams.height = (int) (lenHeight / 1);
            Log.d(TAG, "setWH: " + layoutParams.height);

            cameraView.setLayoutParams(layoutParams);
//            Log.d(TAG, "setWH: "+outputSizes[0].toString());
//            Log.d(TAG, "setWH: "+outputSizes[1].toString());
//            for (Size outputSize : outputSizes) {
//                Log.d(TAG, "setWH: "+outputSize.toString());
//            }
        } catch (CameraAccessException e) {
            throw new RuntimeException(e);
        }

    }

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


    public abstract void clickCapture(PreviewView previewView);


    @SuppressLint("RestrictedApi")
    private void initView() {
        cameraView = (PreviewView) findViewById(R.id.act_cameraTest_pv_cameraPreview);
//        cameraView.setImplementationMode(PreviewView.ImplementationMode.PERFORMANCE);
        btn_capture = (ImageView) findViewById(R.id.btn_capture);
        btn_capture.setOnClickListener(v -> {
            if (!onCameraCreate) return;
            clickCapture(cameraView);
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
        picMask = (ImageView) findViewById(R.id.pic_mask);
    }
}