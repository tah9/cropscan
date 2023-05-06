package com.nuist.gallery.adapter;

import android.content.Context;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.nuist.gallery.GalleryConfig;
import com.nuist.gallery.bean.FolderBean;
import com.nuist.cropscan.R;
import com.nuist.tool.screen.Tools;

import java.util.ArrayList;

/**
 * ->  tah9  2023/5/4 0:04
 */
public class SelectFolderAdapter extends RecyclerView.Adapter {
    private final int scrWid;
    private Context context;
    private ArrayList<FolderBean> folderList;
    private String rootPath = GalleryConfig.rootPath;
    private int gridNum;
    private int pad;

    public SelectFolderAdapter(Context context, ArrayList<FolderBean> folderList, int spanCount) {
        this.context = context;
        this.folderList = folderList;
        this.gridNum = spanCount;
        scrWid = Tools.getWidth(context);
        pad = Tools.dpToPx(context, 1);
    }

    public interface TargetClickListener {
        void targetClick(int position);
    }

    private TargetClickListener targetClickListener;

    public void setTargetClickListener(TargetClickListener listener) {
        this.targetClickListener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int position) {
        View holderItemView = LayoutInflater.from(context).inflate(R.layout.recy_item_folder, parent, false);

        ImageView pic = holderItemView.findViewById(R.id.pic);
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) pic.getLayoutParams();
        layoutParams.width = (scrWid - pad * (gridNum + 1)) / gridNum;
        layoutParams.height = ((int) (scrWid / (gridNum - 1.5f)));
        pic.setLayoutParams(layoutParams);

        GridLayoutManager.LayoutParams holderItemLayoutParams = (GridLayoutManager.LayoutParams) holderItemView.getLayoutParams();
        int marginLeft, marginRight;
        //最左边
        if (position % gridNum == 0) {
            marginLeft = pad;
            marginRight = pad / 2;
        }
        //最右边
        else if (position % gridNum == gridNum - 1) {
            marginLeft = pad / 2;
            marginRight = pad;
        } else {
            marginLeft = pad / 2;
            marginRight = pad / 2;
        }
        holderItemLayoutParams.leftMargin = marginLeft;
        holderItemLayoutParams.rightMargin = marginRight;
        holderItemLayoutParams.topMargin = pad;
        holderItemLayoutParams.bottomMargin = pad;

        holderItemView.setLayoutParams(holderItemLayoutParams);

        return new RecyclerView.ViewHolder(holderItemView) {
        };
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ImageView pic = holder.itemView.findViewById(R.id.pic);
        TextView name = holder.itemView.findViewById(R.id.name);
        TextView number = holder.itemView.findViewById(R.id.number);
        TextView path = holder.itemView.findViewById(R.id.path);

        FolderBean folder = folderList.get(position);
        Glide.with(pic).asBitmap().dontAnimate()
                .load(rootPath + folder.getFirstPath()).into(pic);
        name.setText(folder.getName().isEmpty() ? "根目录" : folder.getName());
        number.setText(folder.getSize() + "");

        if (!folder.getPath().equals("/"))
            path.setText(folder.getPath());

        holder.itemView.setOnClickListener(v -> {
            if (targetClickListener != null) {
                targetClickListener.targetClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return folderList.size();
    }
}
