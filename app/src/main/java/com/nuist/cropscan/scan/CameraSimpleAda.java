package com.nuist.cropscan.scan;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.nuist.cropscan.R;
import com.nuist.request.BASEURL;

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
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.recy_item_camera_bottom_tip, null);
        return new RecyclerView.ViewHolder(view) {
        };
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        ImageView pic = holder.itemView.findViewById(R.id.pic);
        String imgPath = BASEURL.entireHost + "/static/simpleImg/" + rows.optString(position);
        Glide.with(pic).load(imgPath).into(pic);
        holder.itemView.setOnClickListener(v -> {
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
