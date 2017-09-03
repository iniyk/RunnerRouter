package com.iniyk.runnerrouter;

import android.Manifest;
import android.Manifest.permission;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import android.util.Log;
import android.widget.TextView;
import android.graphics.Color;

import com.amap.api.maps.*;
import com.amap.api.maps.AMap.*;
import com.amap.api.maps.model.*;
import com.amap.api.location.*;

import java.util.ArrayList;

import static android.Manifest.permission.*;

public class MainActivity extends AppCompatActivity {
    private String cityName = null;
    private final static int ACCESS_STATE_START = 100;
    private int access_state_mask = 0;
    private int access_state = 0;
    private ArrayList<LatLng> router;
    private ArrayList<Marker> routerMarker;
    private Polyline routerView = null;

    private String[] permissions = {
            INTERNET,
            WRITE_EXTERNAL_STORAGE,
            ACCESS_NETWORK_STATE,
            ACCESS_WIFI_STATE,
            READ_PHONE_STATE,
            ACCESS_COARSE_LOCATION,
            ACCESS_FINE_LOCATION,
            ACCESS_LOCATION_EXTRA_COMMANDS
    };

    public MapView mMapView = null;
    private AMap aMap = null;
    private Button btnMyLocation = null, btnRevert = null, btnClear = null;
    private TextView txtDistance= null;
    //声明AMapLocationClient类对象
    public AMapLocationClient mLocationClient = null;
    //声明AMapLocationClientOption对象
    public AMapLocationClientOption mLocationOption = null;
    //声明定位回调监听器
    public AMapLocationListener mLocationListener = new AMapLocationListener() {
        @Override
        public void onLocationChanged(AMapLocation aMapLocation) {
            if (aMapLocation != null) {
                if (aMapLocation.getErrorCode() == 0) {
                    cityName = aMapLocation.getCity();
                    Log.i("Location", "Now Location is " + cityName);
                    Log.i("Location", "Now Location is Lat : " + aMapLocation.getLatitude() +
                            "Log : " + aMapLocation.getLongitude());

                    LatLng myLatLng = new LatLng(aMapLocation.getLatitude(), aMapLocation.getLongitude());

                    aMap.moveCamera(CameraUpdateFactory.zoomTo(17)); // 将地图移动到定位点
                    aMap.moveCamera(CameraUpdateFactory.changeLatLng(myLatLng)); // 点击定位按钮 能够将地图的中心移动到定位点

                    Marker myLocMarker = aMap.addMarker(new MarkerOptions().position(myLatLng).title("我的位置"));
                }else {
                    //定位失败时，可通过ErrCode（错误码）信息来确定失败的原因，errInfo是错误信息，详见错误码表。
                    Log.e("AmapError","location Error, ErrCode:"
                            + aMapLocation.getErrorCode() + ", errInfo:"
                            + aMapLocation.getErrorInfo());
                }
            }
        }
    };

    public OnMapLongClickListener onMapLongClickListener = new AMap.OnMapLongClickListener() {
        @Override
        public void onMapLongClick(LatLng point) {
            router.add(point);
            Marker marker = aMap.addMarker(new MarkerOptions().position(point).title("路径点" + router.size()));
            routerMarker.add(marker);
            calcDistance();
            Log.i("LongClickListener", "Lat : " + point.latitude +
                    "Log : " + point.longitude);
        }
    };

    private double calcDistance() {
        if (routerView != null) {
            routerView.remove();
            routerView = null;
        }
        double ret = 0.0;
        for (int i=0; i<router.size() - 1; ++i) {
            ret += AMapUtils.calculateLineDistance(router.get(i), router.get(i+1));
        }

        if (txtDistance != null) {

            txtDistance.setText(String.format("%.4f", ret / 1000.0) + "km");
            if (router.size() > 1) {
                routerView = aMap.addPolyline(new PolylineOptions().
                        addAll(router).width(5).color(Color.argb(120, 211, 1, 1)));
            }
        }

        return ret;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.cityName = new String("北京");
        router = new ArrayList<>();
        routerMarker = new ArrayList<>();
        Log.i("MainActivity", "MainActivity OnCreating.");
        requestPermissions();

        setContentView(R.layout.activity_main);
        //获取地图控件引用
        mMapView = (MapView) findViewById(R.id.map);
        //在activity执行onCreate时执行mMapView.onCreate(savedInstanceState)，创建地图
        mMapView.onCreate(savedInstanceState);

        aMap = mMapView.getMap();
        aMap.setOnMapLongClickListener(onMapLongClickListener);

        btnMyLocation = (Button) findViewById(R.id.btn_location);
        btnRevert = (Button) findViewById(R.id.btn_revert);
        btnClear = (Button) findViewById(R.id.btn_clear);

        txtDistance = (TextView) findViewById(R.id.txt_distance);

        btnRevert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Marker lastMarker = routerMarker.get(routerMarker.size() - 1);
                routerMarker.remove(routerMarker.size() - 1);
                lastMarker.destroy();
                router.remove(router.size() - 1);

                calcDistance();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，销毁地图
        mMapView.onDestroy();
    }
    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView.onResume ()，重新绘制加载地图
        mMapView.onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView.onPause ()，暂停地图的绘制
        mMapView.onPause();
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //在activity执行onSaveInstanceState时执行mMapView.onSaveInstanceState (outState)，保存地图当前的状态
        mMapView.onSaveInstanceState(outState);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,String[] arg_permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, arg_permissions,grantResults);
        int accessed_permission = requestCode - ACCESS_STATE_START;
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if ((accessed_permission >= 0) && (accessed_permission < permissions.length)) {
                access_state_mask |= (1 << accessed_permission);
                access_state += 1;
            }
            if (checkAccessStateMask()) {
                getAndRenderCityName();
            } else {
                requestNextPermissions();
            }
        }
    }

    private void requestPermissions() {
        access_state_mask = 0;
        access_state = 0;
        requestNextPermissions();
    }

    private void requestNextPermissions() {

        if(getApplicationContext().checkSelfPermission(permissions[access_state]) != PackageManager.PERMISSION_GRANTED) {
            Log.i("AccessInfo", "Trying to request " + permissions[access_state] + " permission.");
            requestPermissions( new String[] { ACCESS_FINE_LOCATION }, ACCESS_STATE_START + access_state);
        } else {
            Log.i("AccessInfo", permissions[access_state] + " permission has been granted.");
            access_state_mask |= (1 << access_state);
            access_state += 1;
            if (checkAccessStateMask()) {
                getAndRenderCityName();
            } else {
                requestNextPermissions();
            }
        }
    }

    private boolean checkAccessStateMask() {
        return ((access_state_mask ^ ((1 << permissions.length) - 1)) == 0);
    }

    private void getAndRenderCityName() {
        //初始化定位
        mLocationClient = new AMapLocationClient(getApplicationContext());
        //设置定位回调监听
        mLocationClient.setLocationListener(mLocationListener);

        //初始化AMapLocationClientOption对象
        mLocationOption = new AMapLocationClientOption();
        //设置定位模式为AMapLocationMode.Hight_Accuracy，高精度模式。
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Device_Sensors);

        //获取一次定位结果：
        //该方法默认为false。
        mLocationOption.setOnceLocation(true);

        //获取最近3s内精度最高的一次定位结果：
        //设置setOnceLocationLatest(boolean b)接口为true，启动定位时SDK会返回最近3s内精度最高的一次定位结果。如果设置其为true，setOnceLocation(boolean b)接口也会被设置为true，反之不会，默认为false。
        mLocationOption.setOnceLocationLatest(true);
        mLocationOption.setWifiScan(false);

        //给定位客户端对象设置定位参数
        mLocationClient.setLocationOption(mLocationOption);
        //启动定位
        mLocationClient.startLocation();
    }
}