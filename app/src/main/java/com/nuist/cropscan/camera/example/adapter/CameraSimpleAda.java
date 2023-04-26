package com.nuist.cropscan.camera.example.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.nuist.cropscan.R;
import com.nuist.cropscan.request.BASEURL;

import org.json.JSONArray;

/**
 * ->  tah9  2023/4/26 20:17
 */
public class CameraSimpleAda extends RecyclerView.Adapter {
    private static final String TAG = "CameraSimpleAda";

    private Context context;
    private JSONArray rows;

    public CameraSimpleAda(Context context, JSONArray rows) {
        this.context = context;
        this.rows = rows;
    }

    public interface ClickSimPic {
        void getPic(Bitmap resource);
    }

    private ClickSimPic tapPic;

    public CameraSimpleAda setOnClickSimPic(ClickSimPic tapPic) {
        this.tapPic = tapPic;
        return this;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new RecyclerView.ViewHolder(LayoutInflater.from(context).inflate(R.layout.cameratip, null)) {
        };
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ImageView pic = holder.itemView.findViewById(R.id.pic);
        String imgPath = BASEURL.entireHost + "/static/simpleImg/" + rows.optString(position);
        Glide.with(pic).load(imgPath).into(pic);
        Log.d(TAG, "onBindViewHolder: " + BASEURL.entireHost + "/static/simpleImg/" + rows.optString(position));
        holder.itemView.setOnClickListener(view -> {
            Glide.with(context).asBitmap().load(imgPath).into(new CustomTarget<Bitmap>() {
                @Override
                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                    if (tapPic != null) {
                        tapPic.getPic(resource);
                    }
                }

                @Override
                public void onLoadCleared(@Nullable Drawable placeholder) {

                }
            });
        });
    }

    @Override
    public int getItemCount() {
        return rows.length();
    }
}
