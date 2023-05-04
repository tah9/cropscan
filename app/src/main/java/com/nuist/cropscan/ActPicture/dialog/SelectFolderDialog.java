package com.nuist.cropscan.ActPicture.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nuist.cropscan.ActPicture.adapter.SelectFolderAdapter;
import com.nuist.cropscan.ActPicture.bean.FolderBean;
import com.nuist.cropscan.R;
import com.nuist.cropscan.tool.ScreenUtil;
import com.nuist.cropscan.tool.Tools;

import java.util.ArrayList;

/**
 * ->  tah9  2023/5/3 23:54
 */
public class SelectFolderDialog extends AlertDialog implements DialogInterface.OnCancelListener, DialogInterface.OnDismissListener {
    private Context context;
    private ArrayList<FolderBean> folderList;
    private SelectFolderAdapter selectFolderAdapter;
    private RecyclerView recy;
    private ImageView back;
    private TextView tvBack;

    public SelectFolderDialog(Context context, ArrayList<FolderBean> folderList) {
        super(context);
        this.context = context;
        this.folderList = folderList;
        show();
    }


    @Override
    public void show() {
        View root = LayoutInflater.from(context).inflate(R.layout.dialog_select_folder, null);
        setView(root);
        initView(root);
        int spanCount = 4;
        selectFolderAdapter = new SelectFolderAdapter(context, folderList, spanCount);
        recy.setLayoutManager(new GridLayoutManager(context, spanCount));
        recy.setHasFixedSize(true);
        recy.setAdapter(selectFolderAdapter);

        setOnDismissListener(this);
        setOnCancelListener(this);
        ScreenUtil.setDialogFullScreen(getWindow());
        selectFolderAdapter.setTargetClickListener(position -> {
            if (targetClickListener != null) {
                targetClickListener.targetClick(position);
                dismiss();
            }
        });
//        setCanceledOnTouchOutside(false);
        super.show();
    }

    private void initView(View view) {
        recy = view.findViewById(R.id.recy);
        back = view.findViewById(R.id.back);
        tvBack = view.findViewById(R.id.tv_back);

        View.OnClickListener backClick = v -> onBackPressed();
        back.setOnClickListener(backClick);
        tvBack.setOnClickListener(backClick);
    }

    public interface TargetClickListener {
        void targetClick(int position);
    }

    private TargetClickListener targetClickListener;

    public void setTargetClickListener(TargetClickListener listener) {
        this.targetClickListener = listener;
    }

    @Override
    public void onCancel(DialogInterface dialog) {

    }

    @Override
    public void onDismiss(DialogInterface dialog) {

    }
}
