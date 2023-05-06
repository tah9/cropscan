package com.nuist.gallery;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.nuist.gallery.adapter.GalleryAdapter;
import com.nuist.gallery.bean.FolderBean;
import com.nuist.gallery.bean.PictureBean;
import com.nuist.gallery.dialog.SelectFolderDialog;
import com.nuist.gallery.event.AppbarLayoutEventListener;
import com.nuist.cropscan.R;
import com.nuist.tool.screen.ScreenUtil;
import com.nuist.tool.img.BitmapUtil;

import java.util.ArrayList;
import java.util.Collections;

public class ActGallery extends AppCompatActivity {
    /*
    声明引用so库，声明原生方法
     */
    static {
        System.loadLibrary("native-gallery");
    }
    public native void native_scan(String rootPath);

    public Context context = this;
    private static final String TAG = "GalleryMain";
    private RecyclerView recyclerView;
    private GalleryAdapter picAdapter;
    private String rootPath = GalleryConfig.rootPath;

    //原生回调的照片总集
    ArrayList<PictureBean> totalList = new ArrayList<>();

    //单文件夹内的照片集
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
    private SelectFolderDialog folderDialog;
    private LinearLayout layoutTitle;


    //-1是所有文件夹标志
    private int activeFolderIndex = -1;

    int flag = 0;
    String coverPath;


    private void updateChildrenFolder() {
        new Thread(() -> {
            childrenList.clear();
            FolderBean folder = folderList.get(activeFolderIndex);
            String folderPath = folder.getPath() + folder.getName();
            Log.d(TAG, folder.toString());

            for (PictureBean pictureBean : totalList) {
                String imgPath = pictureBean.getPath();
                if ((imgPath.lastIndexOf("/") == folderPath.lastIndexOf("/") + folder.getName().length() + 1)
                        && (imgPath.contains(folderPath) || folder.getPath().isEmpty())) {
                    childrenList.add(pictureBean);
                }
            }
            Collections.sort(childrenList, (o1, o2) -> {
                return Long.compare(o2.time, o1.time);
            });
            runOnUiThread(() -> {
                recyclerView.scrollToPosition(0);
                picAdapter.updateList(childrenList);
                disPlayCover();
                barLayout.setExpanded(true);
            });
        }).start();
    }


    private void disPlayCover() {
        try {
            this.coverPath = rootPath + (activeFolderIndex == -1 ?
                    totalList.get(0).getPath() : childrenList.get(0).getPath());
        } catch (Exception e) {
            e.printStackTrace();
        }
        runOnUiThread(() -> {
            if (activeFolderIndex != -1) {
                FolderBean folder = folderList.get(activeFolderIndex);

                tvNumberTip.setText(childrenList.size() + "张照片");
                tvMainTip.setText(folder.getName().isEmpty() ? "根目录" : folder.getName());
                tvPathTip.setText(rootPath + folder.getPath());
                tvTitle.setText(folder.getName().isEmpty() ? "根目录" : folder.getName());
            } else {

            }

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
        Collections.sort(folderList, (o1, o2) -> {
            return Long.compare(o2.getTime(), o1.getTime());
        });
//        for (FolderBean folderBean : nativeList) {
//            Log.d(TAG, "nativeCallbackFolder: " + folderBean);
//        }
        runOnUiThread(() -> {
            tvMainTip.setText(folderList.size() + "个照片文件夹");
        });
    }

    //native照片回调，非主线程
    public void nativeCallback(ArrayList<PictureBean> nativeList) {
        totalList.addAll(nativeList);
        if (picAdapter == null) {
            return;
        }
        if (activeFolderIndex == -1 && coverPath == null) {
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
            tvNumberTip.setText(totalList.size() + "张照片");
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




        if (getIntent() != null) {
            startTime = getIntent().getLongExtra("time", 0);
            startSpendTime = System.currentTimeMillis() - startTime;
            startTime = System.currentTimeMillis();
        }

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
        picAdapter = new GalleryAdapter(totalList, context);
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
        back_front.setOnClickListener(v -> {
            onBackPressed();
        });
        barLayout.addOnOffsetChangedListener(new AppbarLayoutEventListener() {
            @Override
            protected void collapsed() {
                Log.d(TAG, "collapsed: ");
                bgPicFront.setImageAlpha(0);
                invisibleViews(tvMainTip, tvNumberTip, tvPathTip);
                visibleViews(layoutTitle);
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
//                if (aniRunSum == 0) {
                invisibleViews(layoutTitle);
                visibleViews(tvMainTip, tvNumberTip, tvPathTip);
//                }
            }
        });
        frontMask = findViewById(R.id.front_mask);
        backFront = findViewById(R.id.back_front);
        tvTitle = findViewById(R.id.tv_title);

        tvTitle.setOnClickListener(v -> {
            folderDialog = new SelectFolderDialog(context, folderList);
            layoutTitle.setVisibility(View.INVISIBLE);
            folderDialog.setOnDismissListener(dialog -> {
                layoutTitle.setVisibility(View.VISIBLE);
            });
            folderDialog.setTargetClickListener(position -> {
                layoutTitle.setVisibility(View.VISIBLE);
                Log.d(TAG, "initView: " + position);
                activeFolderIndex = position;
                updateChildrenFolder();
            });
        });
        layoutTitle = findViewById(R.id.layout_title);
    }

    private int duration = 100;
//    private int aniRunSum = 0;

    private void visibleViews(View... views) {
        for (View view : views) {
//            aniRunSum++;
            view.setVisibility(View.VISIBLE);
//            view.setScaleX(0f);
//            view.setScaleY(0f);
//            view.animate()
//                    .scaleX(1f)
//                    .scaleY(1f)
//                    .setDuration(duration).withEndAction(() -> {
//                        aniRunSum--;
//                    });
        }
    }

    private void invisibleViews(View... views) {
        for (View view : views) {
//            aniRunSum++;
            view.setVisibility(View.INVISIBLE);
//            view.animate()
//                    .scaleX(0f)
//                    .scaleY(0f)
//                    .setDuration(duration).withEndAction(() -> {
//                        view.setVisibility(View.INVISIBLE);
//                        aniRunSum--;
//                    });
        }
    }
}