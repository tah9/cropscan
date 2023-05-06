package com.nuist.cropscan.view.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.nuist.cropscan.R;
import com.nuist.request.BASEURL;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * ->  tah9  2023/5/6 14:31
 */
public class HomePlantAda extends RecyclerView.Adapter {
    private Context context;
    private JSONArray arr;

    public HomePlantAda(Context context, JSONArray rows) {
        this.context = context;
        this.arr = rows;
    }

    public interface TargetClickListener {
        void targetClick(int position);
    }

    private TargetClickListener targetClickListener;

    public void setTargetClickListener(TargetClickListener listener) {
        this.targetClickListener = listener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new RecyclerView.ViewHolder(LayoutInflater.from(context).inflate(R.layout.recy_item_disease, parent, false)) {
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
            if (targetClickListener != null) {
                targetClickListener.targetClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return arr.length() + 3;
    }
}
