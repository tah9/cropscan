package com.nuist.cropscan.base;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

/**
 * ->  tah9  2023/4/5 20:52
 */
public class BaseFrag extends Fragment {
    public Context context;
    public static final String TAG = "BaseFrag";

    public void setString(String key, String value) {
        ((BaseAct) getActivity()).setString(key, value);
    }

    public String optString(String key) {
        return ((BaseAct) getActivity()).optString(key);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity();
    }
}
