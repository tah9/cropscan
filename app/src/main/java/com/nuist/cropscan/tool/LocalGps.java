package com.nuist.cropscan.tool;

import static android.content.Context.LOCATION_SERVICE;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.nuist.cropscan.base.BaseAct;

import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

/**
 * ->  tah9  2023/4/23 14:20
 */
public class LocalGps {
    private static final String TAG = "LocalGps";
    private LocationManager locationManager;
    private BaseAct context;

    public void requestLocal() {
        if (!isLocationProviderEnabled()) {
            openLocationServer();
        }
        initLocationListener();
    }

    private void setLocal(Address address) {
        try {
            JSONObject localJSON = new JSONObject();
            localJSON.put("name", address.getAddressLine(0));
            localJSON.put("latitude", String.valueOf(address.getLatitude()));
            localJSON.put("longitude", String.valueOf(address.getLongitude()));
            context.setString("local", localJSON.toString());
            Log.d(TAG, "getAddress: " + localJSON);
            if (listener != null) {
                listener.updateLocal(localJSON);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public LocalGps(BaseAct context) {
        this.context = context;
        requestLocal();
    }

    public interface LocalListener {
        void updateLocal(JSONObject jsonObject);
    }

    private LocalListener listener;

    public void setListener(LocalListener listener) {
        this.listener = listener;
    }

    /**
     * 判断是否开启了GPS或网络定位开关
     */
    public boolean isLocationProviderEnabled() {
        boolean result = false;
        LocationManager locationManager = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);
        }
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
        context.startActivityForResult(i, 66);
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
                setLocal(address);
                break;
            }
        }
    }

    /**
     * 监听位置变化
     */
    private void initLocationListener() {
        locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);
        if (locationManager == null) {
            return;
        }
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 100, 10, mLocationListener);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 10, mLocationListener);
    }
}
