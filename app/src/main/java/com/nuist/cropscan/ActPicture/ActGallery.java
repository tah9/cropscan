package com.nuist.cropscan.ActPicture;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.material.appbar.AppBarLayout;
import com.nuist.cropscan.ActPicture.adapter.GalleryAdapter;
import com.nuist.cropscan.ActPicture.bean.FolderBean;
import com.nuist.cropscan.ActPicture.bean.PictureBean;
import com.nuist.cropscan.ActPicture.event.AppbarLayoutEventListener;
import com.nuist.cropscan.R;
import com.nuist.cropscan.tool.ScreenUtil;
import com.nuist.cropscan.tool.Tools;
import com.nuist.cropscan.tool.img.BitmapUtil;

import java.util.ArrayList;

public class ActGallery extends AppCompatActivity {
    static {
        System.loadLibrary("my-native");
    }

    public Context context = this;
    private static final String TAG = "GalleryMain";
    private RecyclerView recyclerView;
    private GalleryAdapter picAdapter;
    private String rootPath = Environment.getExternalStorageDirectory().getAbsolutePath();

    //原生回调的图片总集
    ArrayList<PictureBean> totalList = new ArrayList<>();

    //单文件夹内的图片集
    ArrayList<PictureBean> childrenList = new ArrayList<>();

    //所有文件夹
    ArrayList<FolderBean> folderList = new ArrayList<>();


    private long stime;
    private GridLayoutManager gridLayoutManager;
    private AppBarLayout barLayout;
    private ImageView bgPic, bgPicFront;
    private TextView tvMainTip;
    private TextView tvPathTip;
    private TextView tvNumberTip;
    private ImageView back_front;
    private ImageView frontMask;
    private ImageView backFront;
    private TextView tvTitle;

    public native void native_scan(String rootPath);

    //-1是所有文件夹标志
    private int activeFolderIndex = -1;

    int flag = 0;
    String coverPath;

    private String getCoverPath() {
        return rootPath + (activeFolderIndex == -1 ?
                totalList.get(0).getPath() : folderList.get(0).getPath());
    }

    private void disPlayCover() {
        runOnUiThread(() -> {
            Glide.with(bgPicFront).load(coverPath).into(bgPicFront);
            CustomTarget<Bitmap> customTarget = new CustomTarget<Bitmap>() {
                @Override
                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                    Log.d(TAG, "onResourceReady: " + resource.getWidth());
                    Log.d(TAG, "onResourceReady: " + resource.getHeight());
                    new Thread(() -> {
                        Bitmap frostedGlass = BitmapUtil.frostedGlass(resource, 10);
                        runOnUiThread(() -> {
                            Glide.with(bgPic).load(frostedGlass).into(bgPic);
                        });
                    }).start();
                }

                @Override
                public void onLoadCleared(@Nullable Drawable placeholder) {

                }
            };
            RequestOptions options = new RequestOptions().override(500, 500);
            Glide.with(bgPic).asBitmap().apply(options).load(coverPath).into(customTarget);
        });

    }

    /*
    native文件夹回调
     */
    public void nativeCallbackFolder(ArrayList<FolderBean> nativeList) {
        folderList.addAll(nativeList);
        for (FolderBean folderBean : nativeList) {
            Log.d(TAG, "nativeCallbackFolder: " + folderBean);
        }
        runOnUiThread(() -> {
            tvMainTip.setText(folderList.size() + "个图片文件夹");
        });
    }

    //native图片回调，非主线程
    public void nativeCallback(ArrayList<PictureBean> nativeList) {
        totalList.addAll(nativeList);
        if (picAdapter == null) {
            return;
        }
        if (activeFolderIndex == -1 && coverPath == null) {
            coverPath = getCoverPath();
            disPlayCover();
        }

//        Log.d(TAG, "nativeCallback: "+System.currentTimeMillis());
        runOnUiThread(() -> {
//            setTitle("" + imgPaths.size());
            if (!spendTime) {
                spendTime = true;
//                Toast.makeText(context, "页面启动" + startSpendTime + "ms\n扫描消耗" + (System.currentTimeMillis() - startTime) + "ms", Toast.LENGTH_LONG).show();
                Log.d(TAG, "nativeCallback: " + "页面启动" + startSpendTime + "ms\n扫描消耗" + (System.currentTimeMillis() - startTime) + "ms");
            }
            tvNumberTip.setText(totalList.size() + "张图片");
            picAdapter.notifyItemRangeInserted(
                    totalList.size() - nativeList.size(), nativeList.size());
            picAdapter.notifyItemRangeChanged(
                    totalList.size() - nativeList.size(), nativeList.size());

        });
    }

    void expendTime() {
        // 结束时间
        long etime = System.currentTimeMillis();
        // 计算执行时间
        Log.d(TAG, "消耗时间: " + (etime - stime));
    }

    long startTime;
    long startSpendTime;
    boolean spendTime = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gallery_main);
        ScreenUtil.setTranslateStatusBar(getWindow());

        //设置状态栏文字颜色
//        View decor = getWindow().getDecorView();
//        int ui = decor.getSystemUiVisibility();
//        ui |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
//        decor.setSystemUiVisibility(ui);


        if (getIntent() != null) {
            startTime = getIntent().getLongExtra("time", 0);
            startSpendTime = System.currentTimeMillis() - startTime;
            startTime = System.currentTimeMillis();
        }

        Log.d(TAG, "onCreate: " + System.currentTimeMillis());

        //native后台扫描线程
        new Thread(() -> {
            native_scan(rootPath);
        }).start();

        initView();

        long one = System.currentTimeMillis();
//        String[] projection = {MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA, MediaStore.Images.Media.DATE_ADDED};
//        String sortOrder = MediaStore.Images.Media.DATE_ADDED + " DESC LIMIT 10";
//
//        // 查询照片
//        Cursor cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null, null, sortOrder);
//        // 遍历查询结果，获取最新的10张照片
//        if (cursor != null) {
//            List<String> latestPhotos = new ArrayList<>();
//            while (cursor.moveToNext()) {
//                @SuppressLint("Range") String photoPath = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
//                latestPhotos.add(photoPath);
//            }
//            cursor.close();
//            // 最新的10张照片的路径列表
//            Log.d(TAG, "Latest photos: " + latestPhotos.toString());
//        }
//        Log.d(TAG, "query spend time: " + (System.currentTimeMillis() - one));
    }


    private void initView() {
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(null);
        gridLayoutManager = new GridLayoutManager(context, 4);
        recyclerView.setLayoutManager(gridLayoutManager);
        picAdapter = new GalleryAdapter(totalList, context, rootPath);
        picAdapter.setBack(path -> {
            setResult(RESULT_OK, new Intent().putExtra("path", path));
            finish();
        });
        recyclerView.setAdapter(picAdapter);
        recyclerView.setItemViewCacheSize(30);


        barLayout = findViewById(R.id.barLayout);
        bgPic = findViewById(R.id.bg_pic);
        bgPicFront = findViewById(R.id.bg_pic_front);
        tvMainTip = findViewById(R.id.tv_main_tip);
        tvPathTip = findViewById(R.id.tv_path_tip);
        tvNumberTip = findViewById(R.id.tv_number_tip);
        back_front = findViewById(R.id.back_front);
        barLayout.addOnOffsetChangedListener(new AppbarLayoutEventListener() {
            @Override
            protected void collapsed() {
                Log.d(TAG, "collapsed: ");
                bgPicFront.setImageAlpha(0);
                invisibleViews(tvMainTip, tvNumberTip, tvPathTip);
                visibleViews(tvTitle);
            }

            @Override
            protected void slide(int v, int range) {
                Log.d(TAG, "slide: ");
                int alpha = 255 - (int) ((float) (-v) / (float) range * 255f);
                Log.d(TAG, "slide: " + alpha);
                bgPicFront.setImageAlpha(alpha);
            }

            @Override
            protected void burst() {
                invisibleViews(tvTitle);
                visibleViews(tvMainTip, tvNumberTip, tvPathTip);
            }
        });
        frontMask = findViewById(R.id.front_mask);
        backFront = findViewById(R.id.back_front);
        tvTitle = findViewById(R.id.tv_title);

    }

    private int duration = 100;

    private void visibleViews(View... views) {
        for (View view : views) {
            view.setVisibility(View.VISIBLE);
            view.setScaleX(0f);
            view.setScaleY(0f);
            view.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(duration);
        }
    }

    private void invisibleViews(View... views) {
        for (View view : views) {
            view.setVisibility(View.VISIBLE);
            view.animate()
                    .scaleX(0f)
                    .scaleY(0f)
                    .setDuration(duration).withEndAction(() -> {
                        view.setVisibility(View.INVISIBLE);
                    });
        }
    }
}