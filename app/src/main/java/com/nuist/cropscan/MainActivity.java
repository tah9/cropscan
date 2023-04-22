package com.nuist.cropscan;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.nuist.cropscan.base.BaseAct;
import com.nuist.cropscan.base.FragWeb;
import com.nuist.cropscan.mainfragment.FragIntroduce;
import com.nuist.cropscan.request.BASEURL;

public class MainActivity extends BaseAct {
    public Context context = this;
    private static final String TAG = "MainActivity";
    private BottomNavigationView bottomNav;
    private ImageView bottomPic;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_main);
        initView();


    }


    private void initView() {
        setLightStatusBar(getWindow(), false, Color.parseColor("#2c2c2c"));

        bottomNav = (BottomNavigationView) findViewById(R.id.bottom_nav);

        bottomNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                bottomNav.setBackgroundColor(getResources().getColor(R.color.main));
                findViewById(R.id.view).setBackgroundColor(getResources().getColor(R.color.main));
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                switch (item.getItemId()) {
                    case R.id.ke:
                        fragmentTransaction.replace(R.id.frame, new FragIntroduce()).commit();
                        return true;
                    case R.id.user:
                        fragmentTransaction.replace(R.id.frame,
                                new FragWeb(BASEURL.entireWebHost
                                        + "#/mine?uid=" + getUser().optString("uid"),"#f9ece4")).commit();
                        return true;
                    case R.id.history:
//                        setLightStatusBar(getWindow(), false, Color.parseColor("#2c2c2c"));
                        bottomNav.setBackgroundColor(Color.TRANSPARENT);
                        findViewById(R.id.view).setBackgroundColor(Color.TRANSPARENT);
                        fragmentTransaction.replace(R.id.frame,
                                new FragWeb(BASEURL.entireWebHost
                                        + "#/order?uid=" + getUser().optString("uid"))).commit();
                        return true;
                    default:
                        return true;
                }
            }
        });
        bottomNav.setSelectedItemId(R.id.ke);
        bottomPic = (ImageView) findViewById(R.id.bottom_pic);
        Glide.with(bottomPic).load(optString("bottomPic")).into(bottomPic);
        bottomPic.setOnClickListener(view -> {
            startActivity(new Intent(MainActivity.this, HomeAct.class));
        });
    }
}