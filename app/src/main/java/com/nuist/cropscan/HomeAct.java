package com.nuist.cropscan;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.nuist.cropscan.base.BaseAct;
import com.nuist.cropscan.base.FragWeb;
import com.nuist.cropscan.dialog.DownLoadDialog;
import com.nuist.cropscan.dialog.LoadingDialogUtils;
import com.nuist.cropscan.dialog.SnackUtil;
import com.nuist.cropscan.request.BASEURL;
import com.nuist.cropscan.request.FileConfig;
import com.nuist.cropscan.request.HttpOk;
import com.nuist.cropscan.scan.ActCropScan;
import com.nuist.cropscan.tool.FileUtils;
import com.nuist.cropscan.tool.ZipUtils;
import com.nuist.guide.Act_Login;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.Random;

/**
 * ->  tah9  2023/4/5 23:56
 */
public class HomeAct extends BaseAct {
    private RecyclerView recy;
    private static final String TAG = "HomeAct";
    private Button takePicture;
    private ImageView tl;
    private ImageView tr;
    private ImageView fl;
    private ImageView fr;
    long durTime = 1000;

    private void loadPage(int newVersion) {
        setInt(getResources().getString(R.string.web_version), newVersion);
        int localVersion = optInt(getResources().getString(R.string.web_version));
        Log.d(TAG, "localWebVersion: " + localVersion);
    }

    private void checkAppVersion(JSONObject o) throws Exception {
        //获取软件版本号，对应AndroidManifest.xml下android:versionCode
        int versionCode = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
        int netAppVersion = o.optInt("androidVersionCode");
        Log.d(TAG, "AppVersionCode: " + versionCode);
        Log.d(TAG, "netAppVersion: " + netAppVersion);

        if (netAppVersion > versionCode) {
            SnackUtil.show(this,"版本过低，请更新版本!");
            Toast toast = Toast.makeText(context, "版本过低，请更新版本!", Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
            Uri uri = Uri.parse("http://149.28.194.155:9001");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
            new Handler(getMainLooper()).postDelayed(() -> {
                finish();
            }, 3000);
            throw new Exception();
        }
    }

    private void checkWebVersion(JSONObject o) {
        int localVersion = optInt(getResources().getString(R.string.web_version));
        int netVersion = o.optInt("webVersion");
        Log.d(TAG, "localWebVersion: " + localVersion);
        Log.d(TAG, "netVersion: " + netVersion);
//        if (netVersion > localVersion) {
//            //清空已下载文件
//            FileUtils.deleteDir(new File(getFilesDir().getAbsolutePath()));
//
//            String zipPath = getFilesDir().getAbsolutePath() + "/dist+" + netVersion + ".zip";
//            DownLoadDialog downLoadDialog = new DownLoadDialog(context,
//                    BASEURL.entireHost + "/static/mobile/android/dist.zip",
//                    zipPath);
//            downLoadDialog.show();
//            downLoadDialog.setOnDismissListener(dialogInterface -> {
//                LoadingDialogUtils.show(this);
//                ZipUtils.UnZipFolderDelOri(zipPath,
//                        FileConfig.webFilePath(context));
//                LoadingDialogUtils.dismiss();
//                loadPage(netVersion);
//            });
//        } else {
        loadPage(netVersion);
//        }
    }

    private void checkVersion() {
        HttpOk.getInstance().toOwnerUrl("/version/latest", o -> {
            JSONObject versionData = o.optJSONObject("data");
            checkAppVersion(versionData);
            checkWebVersion(versionData);



            createView();
        });
    }

    private void createView() {
        if (optString("uid").isEmpty()) {
            startActivity(new Intent(this, Act_Login.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            finish();
        }
        setContentView(R.layout.act_home);

        initView();
        recy.setLayoutManager(new GridLayoutManager(context, 4));
        HttpOk.getInstance().toOwnerUrl("/plant/types", o -> {
            JSONArray arr = o.optJSONArray("rows");
            recy.setAdapter(new RecyclerView.Adapter() {
                @NonNull
                @Override
                public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                    return new RecyclerView.ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_disease, parent, false)) {
                    };
                }

                @Override
                public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
                    JSONObject object = arr.optJSONObject(position);
                    TextView textView = holder.itemView.findViewById(R.id.name);
                    if (object == null) {
                        textView.setBackgroundColor(Color.TRANSPARENT);
                        return;
                    }
                    textView.setText(object.optString("name"));
                    ImageView pic = holder.itemView.findViewById(R.id.pic);
                    Glide.with(pic).load(BASEURL.picUrl(object.optString("fname") + "/cover")).into(pic);
                    holder.itemView.setOnClickListener(view -> {
                        setString("plant", object.optString("name"));
//                        setString("fname", object.optString("fname"));
//                        setString("localPicPath", "");
//                        setString("bottomPic", BASEURL.picUrl(object.optString("fname") + "/cover"));
                        startActivity(new Intent(HomeAct.this, ActWeb.class));
                    });
                }

                @Override
                public int getItemCount() {
                    return arr.length() + 3;
                }
            });
        });
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkVersion();
    }

    private void initView() {
        recy = (RecyclerView) findViewById(R.id.recy);
        takePicture = (Button) findViewById(R.id.take_picture);
        takePicture.setOnClickListener(v -> {
            startActivity(new Intent(HomeAct.this, ActCropScan.class));
        });
        tl = (ImageView) findViewById(R.id.tl);
        tr = (ImageView) findViewById(R.id.tr);
        fl = (ImageView) findViewById(R.id.fl);
        fr = (ImageView) findViewById(R.id.fr);

        int top = new Random().nextInt(3);
        int bottom = new Random().nextInt(3);
        int tlRes[] = {R.drawable.tl1, R.drawable.tl2, R.drawable.tl3};
        int trRes[] = {R.drawable.tr1, R.drawable.tr2, R.drawable.tr3};
        int flRes[] = {R.drawable.fl1, R.drawable.fl2, R.drawable.fl3};
        int frRes[] = {R.drawable.fr1, R.drawable.fr2, R.drawable.fr3};

        Glide.with(context).load(tlRes[top]).into(tl);
        Glide.with(context).load(trRes[top]).into(tr);
        Glide.with(context).load(flRes[bottom]).into(fl);
        Glide.with(context).load(frRes[bottom]).into(fr);

        ObjectAnimator.ofFloat(tl, "translationX", 0f, -800F).setDuration(durTime).start();
        ObjectAnimator.ofFloat(tl, "translationY", 0F, 800F).setDuration(durTime).start();
        ObjectAnimator.ofFloat(tr, "translationX", 0f, 800F).setDuration(durTime).start();
        ObjectAnimator.ofFloat(tr, "translationY", 0F, 800F).setDuration(durTime).start();
        ObjectAnimator.ofFloat(fl, "translationX", 0f, -800F).setDuration(durTime).start();
        ObjectAnimator.ofFloat(fl, "translationY", 0F, 800F).setDuration(durTime).start();
        ObjectAnimator.ofFloat(fr, "translationX", 0f, 800F).setDuration(durTime).start();
        ObjectAnimator.ofFloat(fr, "translationY", 0F, 800F).setDuration(durTime).start();
        ObjectAnimator.ofFloat(tl, "alpha", 1f, 0f).setDuration(durTime).start();
        ObjectAnimator.ofFloat(tr, "alpha", 1f, 0f).setDuration(durTime).start();
        ObjectAnimator.ofFloat(fl, "alpha", 1f, 0f).setDuration(durTime).start();
        ObjectAnimator.ofFloat(fr, "alpha", 1f, 0f).setDuration(durTime).start();
        new Thread(() -> {
            try {
                Thread.sleep(durTime);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            runOnUiThread(() -> {
                tl.setImageBitmap(null);
                tr.setImageBitmap(null);
                fl.setImageBitmap(null);
                fr.setImageBitmap(null);
            });
        }).start();
    }
}
