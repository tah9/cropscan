package com.nuist.cropscan.scan;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.FrameLayout;
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
public class CropResultAdapter extends RecyclerView.Adapter {
    List<TRect> rectList;
    Context context;
    private static final String TAG = "CropResultAdapter";

    private int activateIndex;
    private int preIndex;


    public CropResultAdapter(List<TRect> rectList, Context context, int activateIndex) {
        this.rectList = rectList;
        this.context = context;
        this.activateIndex = activateIndex;
        this.preIndex = activateIndex;
    }

    public void updateActivateIndex(int activateIndex) {
        this.activateIndex = activateIndex;
        notifyItemChanged(activateIndex);
        notifyItemChanged(preIndex);
        this.preIndex = activateIndex;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new RecyclerView.ViewHolder(LayoutInflater.from(context).inflate(R.layout.recy_item_crop_result, parent, false)) {
        };
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        FrameLayout layoutView = (FrameLayout) holder.itemView;
        ImageView pic = layoutView.findViewById(R.id.pic);
        Glide.with(pic).load(rectList.get(position).getRectBitmap()).into(pic);
        if (position == activateIndex) {
            ((CardView) layoutView.getChildAt(0)).setCardBackgroundColor(Color.BLACK);
            layoutView.setBackgroundResource(R.drawable.item_scan_card);
        } else {
            ((CardView) layoutView.getChildAt(0)).setCardBackgroundColor(Color.TRANSPARENT);
            layoutView.setBackground(null);
        }
    }

    @Override
    public int getItemCount() {
        return rectList.size();
    }
}
