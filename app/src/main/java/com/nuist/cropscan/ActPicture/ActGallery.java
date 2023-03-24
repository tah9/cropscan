package com.nuist.cropscan.ActPicture;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nuist.cropscan.PcPathBean;
import com.nuist.cropscan.R;
import com.nuist.cropscan.adapter.TestAdapter;

import java.util.ArrayList;
import java.util.List;

public class ActGallery extends AppCompatActivity {
    static {
        System.loadLibrary("my-native");
    }

    public Context context = this;
    private static final String TAG = "GalleryMain";
    private RecyclerView recyclerView;
    private TestAdapter picAdapter;
    private String rootPath = Environment.getExternalStorageDirectory().getAbsolutePath();

    ArrayList<PcPathBean> imgPaths = new ArrayList<>();
    ArrayList<PcPathBean> conPaths = new ArrayList<>();
    private long stime;
    private GridLayoutManager gridLayoutManager;

    public native void native_scan(String rootPath);


    int flag = 0;

    //native回调
    public void nativeCallback(ArrayList<PcPathBean> nativeList) {
        imgPaths.addAll(nativeList);
        if (picAdapter == null) {
            return;
        }

//        Log.d(TAG, "nativeCallback: "+System.currentTimeMillis());
        runOnUiThread(() -> {
//            setTitle("" + imgPaths.size());
            if (!spendTime) {
                spendTime = true;
                Toast.makeText(context, "页面启动" + startSpendTime + "ms\n扫描消耗" + (System.currentTimeMillis() - startTime) + "ms", Toast.LENGTH_LONG).show();
                Log.d(TAG, "nativeCallback: " + "页面启动" + startSpendTime + "ms\n扫描消耗" + (System.currentTimeMillis() - startTime) + "ms");
            }
            picAdapter.notifyItemRangeInserted(
                    imgPaths.size() - nativeList.size(), nativeList.size());
            picAdapter.notifyItemRangeChanged(
                    imgPaths.size() - nativeList.size(), nativeList.size());
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

//        long one = System.currentTimeMillis();
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

//    ArrayList<PcDirBean> imgFolders = new ArrayList<>();

    private void initView() {
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(null);
        gridLayoutManager = new GridLayoutManager(context, 4);
        recyclerView.setLayoutManager(gridLayoutManager);
        picAdapter = new TestAdapter(imgPaths, context, rootPath);
        recyclerView.setAdapter(picAdapter);
        recyclerView.setItemViewCacheSize(30);
    }
}