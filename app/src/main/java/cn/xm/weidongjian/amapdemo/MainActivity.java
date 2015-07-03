package cn.xm.weidongjian.amapdemo;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

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
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.GeocodeSearch.OnGeocodeSearchListener;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;

import cn.xm.weidongjian.amapdemo.utils.ToastUtil;


public class MainActivity extends AppCompatActivity implements AMapLocationListener, AMap.OnCameraChangeListener, View.OnClickListener, LocationSource, OnGeocodeSearchListener {
    private MapView mapView;
    private AMap aMap;
    private LocationManagerProxy mLocationManagerProxy;
    private Handler handler = new Handler();
    private OnLocationChangedListener listener;
    private LatLng myLocation = null;
    private Marker centerMarker;
    private boolean isMovingMarker = false;
    private BitmapDescriptor movingDescriptor, chooseDescripter, successDescripter;
    private ValueAnimator animator = null;
    private GeocodeSearch geocodeSearch;

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

        aMap.setMyLocationType(AMap.LOCATION_TYPE_LOCATE);
        CameraUpdate cameraUpdate = CameraUpdateFactory.zoomTo(15);
        aMap.moveCamera(cameraUpdate);

        movingDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.icon_loaction_choose_moving);
        chooseDescripter = BitmapDescriptorFactory.fromResource(R.drawable.icon_loaction_choose);
        successDescripter = BitmapDescriptorFactory.fromResource(R.drawable.icon_usecarnow_position_succeed);

        geocodeSearch = new GeocodeSearch(this);
        geocodeSearch.setOnGeocodeSearchListener(this);
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
        MarkerOptions centerMarkerOption = new MarkerOptions().position(myLocation).icon(chooseDescripter);
        centerMarker = aMap.addMarker(centerMarkerOption);
        centerMarker.setPositionByPixels(mapView.getWidth() / 2, mapView.getHeight() / 2);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                CameraUpdate update = CameraUpdateFactory.zoomTo(17f);
                aMap.animateCamera(update, 1000, new AMap.CancelableCallback() {
                    @Override
                    public void onFinish() {
                        aMap.setOnCameraChangeListener(MainActivity.this);
                    }
                    @Override
                    public void onCancel() {
                    }
                });
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
        if (centerMarker != null) {
            setMovingMarker();
        }
    }

    @Override
    public void onCameraChangeFinish(CameraPosition cameraPosition) {
        LatLonPoint point = new LatLonPoint(cameraPosition.target.latitude, cameraPosition.target.longitude);
        RegeocodeQuery query = new RegeocodeQuery(point, 50, GeocodeSearch.AMAP);
        geocodeSearch.getFromLocationAsyn(query);
        if (centerMarker != null) {
            animMarker();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button:
                animMarker();
                break;
            default:
                break;
        }
    }

    private void setMovingMarker() {
        if (isMovingMarker)
            return;

        isMovingMarker = true;
        centerMarker.setIcon(movingDescriptor);
    }

    private void animMarker() {
        isMovingMarker = false;
        if (animator != null) {
            animator.start();
            return;
        }
        animator = ValueAnimator.ofFloat(mapView.getHeight()/2, mapView.getHeight()/2 - 30);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.setDuration(150);
        animator.setRepeatCount(1);
        animator.setRepeatMode(ValueAnimator.REVERSE);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                Float value = (Float) animation.getAnimatedValue();
                centerMarker.setPositionByPixels(mapView.getWidth() / 2, Math.round(value));
            }
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                centerMarker.setIcon(chooseDescripter);
            }
        });
        animator.start();
    }

    private void endAnim() {
        if (animator != null && animator.isRunning())
            animator.end();
    }

    @Override
    public void onRegeocodeSearched(RegeocodeResult regeocodeResult, int i) {
        if(i == 0){
            if(regeocodeResult != null&& regeocodeResult.getRegeocodeAddress() != null){
                String addressName = regeocodeResult.getRegeocodeAddress().getFormatAddress() + "附近";
                ToastUtil.show(MainActivity.this, addressName);
                endAnim();
                centerMarker.setIcon(successDescripter);
            }else{
                ToastUtil.show(MainActivity.this, R.string.no_result);
            }
        }else{
            ToastUtil.show(MainActivity.this, R.string.error_network);
        }
    }

    @Override
    public void onGeocodeSearched(GeocodeResult geocodeResult, int rCode) {
    }
}
