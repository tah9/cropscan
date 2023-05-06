package com.nuist.cropscan;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nuist.cropscan.base.BaseAct;
import com.nuist.cropscan.scan.ActCropScan;
import com.nuist.cropscan.view.adapter.HomePlantAda;
import com.nuist.guide.Act_Login;
import com.nuist.request.HttpOk;
import com.nuist.tool.dialog.RippleDialog;
import com.nuist.tool.dialog.SnackUtil;
import com.nuist.webview.ActWeb;

import org.json.JSONArray;

import java.io.File;

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


    private void activateCreateView() {

        setContentView(R.layout.act_home);

        initView();
        if (getIntent().getStringExtra("editEmail") != null) {
            SnackUtil.showAndTime(
                    recy,
                    getIntent().getStringExtra("editEmail") + "\n登录成功",
                    1000);
        }

        recy.setLayoutManager(new GridLayoutManager(context, 4));
        HttpOk.getInstance().toOwnerUrl("/plant/types", o -> {
            JSONArray arr = o.optJSONArray("rows");
            HomePlantAda homePlantAda = new HomePlantAda(context, arr);
            recy.setAdapter(homePlantAda);
            homePlantAda.setTargetClickListener(position -> {
                setString("plant", arr.optJSONObject(position).optString("name"));
                startActivity(new Intent(HomeAct.this, ActWeb.class));
            });
        });
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        new VersionCheck(this, () -> {
            new Thread(() -> {
                new File(context.getFilesDir().getAbsolutePath() + "/app-release.apk").delete();
            }).start();

            if (optString("uid").isEmpty()) {
                startActivity(new Intent(this, Act_Login.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                finish();
            } else {
                activateCreateView();
            }
        });
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
//        List<ImageView> aniPicList = Arrays.asList(tl, tr, fl, fr);
//
//        int top = new Random().nextInt(3);
//        int bottom = new Random().nextInt(3);
//        int tlRes[] = {R.drawable.tl1, R.drawable.tl2, R.drawable.tl3};
//        int trRes[] = {R.drawable.tr1, R.drawable.tr2, R.drawable.tr3};
//        int flRes[] = {R.drawable.fl1, R.drawable.fl2, R.drawable.fl3};
//        int frRes[] = {R.drawable.fr1, R.drawable.fr2, R.drawable.fr3};
//
//        Glide.with(context).load(tlRes[top]).into(tl);
//        Glide.with(context).load(trRes[top]).into(tr);
//        Glide.with(context).load(flRes[bottom]).into(fl);
//        Glide.with(context).load(frRes[bottom]).into(fr);
//
//        ObjectAnimator.ofFloat(tl, "translationX", 0f, -800F).setDuration(durTime).start();
//        ObjectAnimator.ofFloat(tl, "translationY", 0F, 800F).setDuration(durTime).start();
//        ObjectAnimator.ofFloat(tr, "translationX", 0f, 800F).setDuration(durTime).start();
//        ObjectAnimator.ofFloat(tr, "translationY", 0F, 800F).setDuration(durTime).start();
//        ObjectAnimator.ofFloat(fl, "translationX", 0f, -800F).setDuration(durTime).start();
//        ObjectAnimator.ofFloat(fl, "translationY", 0F, 800F).setDuration(durTime).start();
//        ObjectAnimator.ofFloat(fr, "translationX", 0f, 800F).setDuration(durTime).start();
//        ObjectAnimator.ofFloat(fr, "translationY", 0F, 800F).setDuration(durTime).start();
//
//        for (ImageView imageView : aniPicList) {
//            ObjectAnimator.ofFloat(imageView, "alpha", 1f, 0f).setDuration(durTime).start();
//        }
//
//        new Handler(Looper.getMainLooper()).postDelayed(() -> {
//            for (ImageView imageView : aniPicList) {
//                imageView.setImageBitmap(null);
//            }
//        }, durTime);
    }
}
