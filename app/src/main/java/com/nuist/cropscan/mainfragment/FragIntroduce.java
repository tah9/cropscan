package com.nuist.cropscan.mainfragment;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.nuist.cropscan.R;
import com.nuist.cropscan.base.BaseFrag;
import com.nuist.cropscan.request.HttpOk;
import com.nuist.cropscan.request.BASEURL;
import com.nuist.cropscan.view.StickyScrollView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * ->  tah9  2023/4/5 20:52
 */
public class FragIntroduce extends BaseFrag {

    private ImageView picCover;
    private StickyScrollView scrollView;
    private TextView target;
    private TextView name;
    private TextView type;
    private ImageView star1;
    private ImageView pic1;

    private TextView t1;
    private ImageView pic2;
    private TextView t2;
    private ImageView pic3;
    private TextView t3;
    private ImageView pic4;
    private TextView t4;

    private View frameSolid;
    private TextView frameText;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_introduce, null);
        initView(view);

        HttpOk.getInstance().to("/plantByName/" + optString("name"), o -> {
            JSONObject json = o.optJSONObject("rows");
            Log.d(TAG, "onCreateView: " + json);
            target.setText(json.optString("plant"));
            name.setText(json.optString("name") + "病");
            type.setText(json.optString("type"));
            t1.setText(json.optString("desc"));
            t2.setText(json.optString("region"));
            t3.setText(json.optString("period"));
            t4.setText(json.optString("antidote"));
            Glide.with(context).load(BASEURL.picUrl(json.optString("fname") + "/" + 1)).into(pic1);
            Glide.with(context).load(BASEURL.picUrl(json.optString("fname") + "/" + 2)).into(pic2);
            Glide.with(context).load(BASEURL.picUrl(json.optString("fname") + "/" + 3)).into(pic3);
            Glide.with(context).load(BASEURL.picUrl(json.optString("fname") + "/" + 4)).into(pic4);


        });
        return view;
    }

    private void initView(View view) {
        picCover = (ImageView) view.findViewById(R.id.pic_cover);

        scrollView = (StickyScrollView) view.findViewById(R.id.main_content);


        target = (TextView) view.findViewById(R.id.target);
        name = (TextView) view.findViewById(R.id.name);
        type = (TextView) view.findViewById(R.id.type);
        star1 = (ImageView) view.findViewById(R.id.star1);
        pic1 = (ImageView) view.findViewById(R.id.pic1);


        t1 = view.findViewById(R.id.t1);
        pic2 = view.findViewById(R.id.pic2);
        t2 = view.findViewById(R.id.t2);
        pic3 = view.findViewById(R.id.pic3);
        t3 = view.findViewById(R.id.t3);
        pic4 = view.findViewById(R.id.pic4);
        t4 = view.findViewById(R.id.t4);


        frameSolid = view.findViewById(R.id.frame_solid);
        frameText = view.findViewById(R.id.frame_text);


        String localPicPath = optString("localPicPath");
        if (localPicPath.isEmpty()) {
            frameSolid.setVisibility(View.GONE);
            frameText.setVisibility(View.GONE);

            Glide.with(context).load(BASEURL.picUrl(optString("fname") + "/cover")).into(picCover);
        } else {
            picCover.setPadding(5,5,5,5);
            Glide.with(context).load(localPicPath).into(picCover);
            frameText.setText(optString("name") + "病");
            frameText.setBackgroundColor(Color.parseColor(optString("color")));
            frameSolid.setBackgroundColor(Color.parseColor(optString("color")));
            frameText.bringToFront();
        }
    }
}
