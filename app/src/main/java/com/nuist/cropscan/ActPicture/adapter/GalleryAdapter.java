package com.nuist.cropscan.ActPicture.adapter;

import android.content.Context;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.nuist.cropscan.ActPicture.bean.PictureBean;
import com.nuist.cropscan.R;
import com.nuist.cropscan.tool.Tools;

import java.util.ArrayList;

/**
 * ->  tah9  2023/3/6 10:26
 */

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.ViewHolder> {
    private static final String TAG = "TestAdapter";
    private ArrayList<PictureBean> imgPaths;
    private static String rootPath;

    public static int width;

    public void updateList(ArrayList<PictureBean> newList) {
        this.imgPaths = newList;
        notifyDataSetChanged();
    }

    public GalleryAdapter(ArrayList<PictureBean> imgPaths, Context context) {
        this.imgPaths = imgPaths;
        width = Tools.getWidth(context);
        rootPath = Environment.getExternalStorageDirectory().getAbsolutePath();
    }

    public interface click {
        void back(String path);
    }

    public click mClick;

    public void setBack(click mmClick) {
        this.mClick = mmClick;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        FrameLayout frameLayout = new FrameLayout(parent.getContext());
//        ImageView pic = new ImageView(frameLayout.getContext());
//        pic.setScaleType(ImageView.ScaleType.CENTER_CROP);
//        frameLayout.addView(pic, -1, (int) (width / 3f));
        return new ViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(R.layout.recy_item_gallery_pic, null));
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PictureBean pictureBean = imgPaths.get(position);
        ImageView pic = holder.pic;
//            Log.d(TAG, "onBindViewHolder: "+pcPathBean.path);
        Glide.with(holder.itemView).asBitmap().load(rootPath + pictureBean.getPath())
//                    .skipMemoryCache(true)
//                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                .dontAnimate()
                .into(pic);

        holder.tip.setText(pictureBean.getPath());
        holder.itemView.setOnClickListener(view -> {
            mClick.back(rootPath + pictureBean.getPath());
        });
    }


    @Override
    public int getItemCount() {
        return imgPaths.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView pic;
        TextView tip;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
//            FrameLayout frame = (FrameLayout) itemView;
//            frame.measure(width/4,width/4);

//            this.pic = (ImageView) ((FrameLayout) itemView).getChildAt(0);
            this.pic = itemView.findViewById(R.id.pic);
            this.pic.getLayoutParams().width = width / 4;
            this.pic.getLayoutParams().height = width / 4;
            this.tip = itemView.findViewById(R.id.tip);
        }
    }
}
