package com.nuist.cropscan.scan;

import android.content.Context;
import android.content.pm.LabeledIntent;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.nuist.cropscan.R;
import com.nuist.cropscan.view.entiry.TRect;

import java.util.List;

/**
 * ->  tah9  2023/4/28 18:33
 */
public class CropResultAdapter  extends RecyclerView.Adapter {
    List<TRect> rectList;
    Context context;

    public CropResultAdapter(List<TRect> rectList, Context context) {
        this.rectList = rectList;
        this.context = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new RecyclerView.ViewHolder(LayoutInflater.from(context).inflate(R.layout.recy_item_crop_result, parent,false)) {
        };
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        CardView cardView = (CardView) holder.itemView;
        ImageView pic = cardView.findViewById(R.id.pic);
        Glide.with(pic).load(rectList.get(position).getRectBitmap()).into(pic);
    }

    @Override
    public int getItemCount() {
        return rectList.size();
    }
}
