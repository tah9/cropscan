package com.nuist.cropscan;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.nuist.cropscan.base.BaseAct;
import com.nuist.cropscan.request.BASEURL;
import com.nuist.cropscan.request.HttpOk;
import com.nuist.cropscan.scan.ActCropScan;
import com.nuist.cropscan.tool.ImgTool;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * ->  tah9  2023/4/5 23:56
 */
public class HomeAct extends BaseAct {
    private RecyclerView recy;
    private static final String TAG = "HomeAct";
    private Button takePicture;
    private ImageView tl;
    private ImageView tr;
    private ImageView fl;
    private ImageView fr;
    long durTime = 3000;
    private LocationManager locationManager;

    /**
     * 判断是否开启了GPS或网络定位开关
     */
    public boolean isLocationProviderEnabled() {
        boolean result = false;
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (locationManager == null) {
            return false;
        }
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager
                .isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            result = true;
        }
        return result;
    }

    /**
     * 跳转到设置界面，引导用户开启定位服务
     */
    private void openLocationServer() {
        Intent i = new Intent();
        i.setAction(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivityForResult(i, 66);
    }

    private final LocationListener mLocationListener = new LocationListener() {

        // Provider的状态在可用、暂时不可用和无服务三个状态直接切换时触发此函数
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.d(TAG, "onStatusChanged");
        }

        // Provider被enable时触发此函数，比如GPS被打开
        @Override
        public void onProviderEnabled(String provider) {
            Log.d(TAG, "onProviderEnabled");
        }

        // Provider被disable时触发此函数，比如GPS被关闭
        @Override
        public void onProviderDisabled(String provider) {
            Log.d(TAG, "onProviderDisabled");
        }

        //当坐标改变时触发此函数，如果Provider传进相同的坐标，它就不会被触发
        @Override
        public void onLocationChanged(Location location) {
            Log.d(TAG, String.format("location: longitude: %f, latitude: %f", location.getLongitude(),
                    location.getLatitude()));
            //更新位置信息
            locationManager.removeUpdates(mLocationListener);
            lastLocal = location;
            getAddress(lastLocal.getLatitude(), lastLocal.getLongitude());
        }
    };
    Location lastLocal;

    public void getAddress(double latitude, double longitude) {
        List<Address> addressList = null;
        Geocoder geocoder = new Geocoder(context);
        try {
            addressList = geocoder.getFromLocation(latitude, longitude, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (addressList != null) {
            for (Address address : addressList) {
                Log.d(TAG, String.format("address: %s", address.toString()));

                try {
                    JSONObject localJSON = new JSONObject();
                    localJSON.put("local", address.getAddressLine(0));
                    localJSON.put("longitude", String.valueOf(address.getLatitude()));
                    localJSON.put("latitude", String.valueOf(address.getLongitude()));
                    Log.d(TAG, "getAddress: JSONObject"+localJSON);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                break;
            }
        }
    }

    /**
     * 监听位置变化
     */
    private void initLocationListener() {
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (locationManager == null) {
            return;
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            methodRequiresThreePermission();
            return;
        }
        locationManager
                .requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 10, mLocationListener);
        locationManager
                .requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 10, mLocationListener);

    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setLightStatusBar(getWindow(), true, getResources().getColor(R.color.main));

        setContentView(R.layout.act_home);
        if(!isLocationProviderEnabled()){
            openLocationServer();
        }
        initLocationListener();



        initView();
        recy.setLayoutManager(new GridLayoutManager(context, 4));
        HttpOk.getInstance().to("/plant/types", o -> {
            JSONArray arr = o.optJSONArray("rows");
            recy.setAdapter(new RecyclerView.Adapter() {
                @NonNull
                @Override
                public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                    return new RecyclerView.ViewHolder(View.inflate(context, R.layout.item_disease, null)) {
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
                        setString("name", object.optString("name"));
                        setString("fname", object.optString("fname"));
                        setString("localPicPath", "");
                        setString("bottomPic", BASEURL.picUrl(object.optString("fname") + "/cover"));
                        startActivity(new Intent(HomeAct.this, MainActivity.class));
                    });
                }

                @Override
                public int getItemCount() {
                    return arr.length() + 3;
                }
            });
        });
    }

    private void initView() {
        recy = (RecyclerView) findViewById(R.id.recy);
        takePicture = (Button) findViewById(R.id.take_picture);
        takePicture.setOnClickListener(v -> {
            startActivity(new Intent(HomeAct.this, ActCropScan.class));
        });
        tl = (ImageView) findViewById(R.id.tl);
        tr = (ImageView) findViewById(R.id.tr);
        fl = (ImageView) findViewById(R.id.fl);
        fr = (ImageView) findViewById(R.id.fr);

        int top = new Random().nextInt(3);
        int bottom = new Random().nextInt(3);
        int tlRes[] = {R.drawable.tl1, R.drawable.tl2, R.drawable.tl3};
        int trRes[] = {R.drawable.tr1, R.drawable.tr2, R.drawable.tr3};
        int flRes[] = {R.drawable.fl1, R.drawable.fl2, R.drawable.fl3};
        int frRes[] = {R.drawable.fr1, R.drawable.fr2, R.drawable.fr3};

        Glide.with(context).load(tlRes[top]).into(tl);
        Glide.with(context).load(trRes[top]).into(tr);
        Glide.with(context).load(flRes[bottom]).into(fl);
        Glide.with(context).load(frRes[bottom]).into(fr);

        ObjectAnimator.ofFloat(tl, "translationX", 0f, -800F).setDuration(durTime).start();
        ObjectAnimator.ofFloat(tl, "translationY", 0F, 800F).setDuration(durTime).start();
        ObjectAnimator.ofFloat(tr, "translationX", 0f, 800F).setDuration(durTime).start();
        ObjectAnimator.ofFloat(tr, "translationY", 0F, 800F).setDuration(durTime).start();
        ObjectAnimator.ofFloat(fl, "translationX", 0f, -800F).setDuration(durTime).start();
        ObjectAnimator.ofFloat(fl, "translationY", 0F, 800F).setDuration(durTime).start();
        ObjectAnimator.ofFloat(fr, "translationX", 0f, 800F).setDuration(durTime).start();
        ObjectAnimator.ofFloat(fr, "translationY", 0F, 800F).setDuration(durTime).start();
        ObjectAnimator.ofFloat(tl, "alpha", 1f, 0f).setDuration(durTime).start();
        ObjectAnimator.ofFloat(tr, "alpha", 1f, 0f).setDuration(durTime).start();
        ObjectAnimator.ofFloat(fl, "alpha", 1f, 0f).setDuration(durTime).start();
        ObjectAnimator.ofFloat(fr, "alpha", 1f, 0f).setDuration(durTime).start();
        new Thread(() -> {
            try {
                Thread.sleep(durTime);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            runOnUiThread(() -> {
                tl.setImageBitmap(null);
                tr.setImageBitmap(null);
                fl.setImageBitmap(null);
                fr.setImageBitmap(null);
            });
        }).start();
    }
}
