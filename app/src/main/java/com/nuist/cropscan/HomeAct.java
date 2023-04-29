package com.nuist.cropscan;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.nuist.cropscan.base.BaseAct;
import com.nuist.cropscan.request.BASEURL;
import com.nuist.cropscan.request.HttpOk;
import com.nuist.cropscan.scan.ActCropScan;

import org.json.JSONArray;
import org.json.JSONObject;

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
    long durTime = 3000;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setLightStatusBar(getWindow(), true, getResources().getColor(R.color.main));

        setContentView(R.layout.act_home);



        initView();
        recy.setLayoutManager(new GridLayoutManager(context, 4));
        HttpOk.getInstance().toOwnerUrl("/plant/types", o -> {
            JSONArray arr = o.optJSONArray("rows");
            recy.setAdapter(new RecyclerView.Adapter() {
                @NonNull
                @Override
                public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                    return new RecyclerView.ViewHolder(View.inflate(context, R.layout.item_disease, null)) {
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
                        setString("name", object.optString("name"));
                        setString("fname", object.optString("fname"));
                        setString("localPicPath", "");
                        setString("bottomPic", BASEURL.picUrl(object.optString("fname") + "/cover"));
//                        startActivity(new Intent(HomeAct.this, MainActivity.class));
                    });
                }

                @Override
                public int getItemCount() {
                    return arr.length() + 3;
                }
            });
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
