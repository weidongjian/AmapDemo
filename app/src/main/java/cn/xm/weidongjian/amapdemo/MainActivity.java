package cn.xm.weidongjian.amapdemo;

import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.LocationManagerProxy;
import com.amap.api.location.LocationProviderProxy;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;


public class MainActivity extends AppCompatActivity implements AMapLocationListener, AMap.OnCameraChangeListener, View.OnClickListener, LocationSource {
    private MapView mapView;
    private AMap aMap;
    private LocationManagerProxy mLocationManagerProxy;
    private Handler handler = new Handler();
    private OnLocationChangedListener listener;
    private LatLng myLocation;
    private Marker marker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mapView = (MapView) findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);

        initUI();
        initAmap();
        setUpLocationStyle();
    }

    private void initUI() {
        findViewById(R.id.button).setOnClickListener(this);
    }

    private void initAmap() {
        if (aMap == null) {
            aMap = mapView.getMap();
        }
        aMap.setLocationSource(this);// 设置定位监听
        aMap.setMyLocationEnabled(true);
        aMap.setOnCameraChangeListener(this);
        aMap.setMyLocationType(AMap.LOCATION_TYPE_LOCATE);
        CameraUpdate cameraUpdate = CameraUpdateFactory.zoomTo(15);
        aMap.moveCamera(cameraUpdate);
    }

    private void setUpLocationStyle() {
        // 自定义系统定位蓝点
        MyLocationStyle myLocationStyle = new MyLocationStyle();
        myLocationStyle.myLocationIcon(BitmapDescriptorFactory.
                fromResource(R.drawable.img_location_now));
        myLocationStyle.strokeWidth(0);
        myLocationStyle.radiusFillColor(Color.TRANSPARENT);
        aMap.setMyLocationStyle(myLocationStyle);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
        deactivate();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        if (aMapLocation != null && aMapLocation.getAMapException().getErrorCode() == 0) {
            Log.d("debug", "latitude " + aMapLocation.getAddress());
            if (listener != null) {
                listener.onLocationChanged(aMapLocation);// 显示系统小蓝点
            }
            myLocation = new LatLng(aMapLocation.getLatitude(), aMapLocation.getLongitude());
            addChooseMarker();
        }
    }

    private void addChooseMarker() {
        BitmapDescriptor chooseDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.icon_loaction_choose);
        MarkerOptions centerMarkerOption = new MarkerOptions().position(myLocation).icon(chooseDescriptor);
        marker = aMap.addMarker(centerMarkerOption);
        marker.setPositionByPixels(mapView.getWidth()/2, mapView.getHeight()/2);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                CameraUpdate update = CameraUpdateFactory.zoomTo(17f);
                aMap.animateCamera(update, 1000, null);
            }
        }, 1000);
    }

    @Override
    public void onLocationChanged(Location location) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {
        listener = onLocationChangedListener;
        mLocationManagerProxy = LocationManagerProxy.getInstance(this);
        mLocationManagerProxy.requestLocationData(
                LocationProviderProxy.AMapNetwork, -1, 100, this);
    }

    public void deactivate() {
        if (mLocationManagerProxy != null) {
            mLocationManagerProxy.removeUpdates(this);
            mLocationManagerProxy.destroy();
        }
        mLocationManagerProxy = null;
    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {

    }

    @Override
    public void onCameraChangeFinish(CameraPosition cameraPosition) {
        Log.d("debug", "onCameraChangeFinish");
        LatLng latLng = aMap.getCameraPosition().target;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button:
                marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.icon_loaction_choose_moving));
                break;
            default:
                break;
        }
    }
}
