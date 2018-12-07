package me.sahidur.foodiography.main.pedometer;

import android.app.Activity;
import android.os.Bundle;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;
import me.sahidur.foodiography.R;

import java.util.List;

public class Trace extends Activity {
    public BaiduMap mBaiduMap = null;
    public MapView mMapView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /* Initialize context information with ApplicationContext
         *  noted that this should be set before setContentView() */
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_show_map);

        mMapView = (MapView) findViewById(R.id.bmapView);
        mBaiduMap = mMapView.getMap();
        mBaiduMap.setMyLocationEnabled(true);
        mBaiduMap.setMyLocationConfigeration(new MyLocationConfiguration(
                        MyLocationConfiguration.LocationMode.FOLLOWING, false, null));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();

        List<LatLng> mHistoryTrace =PedometerService.getHistoryTrace();
        MyLocationData mCurrentLocation = PedometerService.getCurrentLocation();
        float zoomLevel = 17; //TODO: remove this constant

        // display the trace
        BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(R.drawable.ic_notification);
        if(mHistoryTrace.size() > 2) {
            mBaiduMap.addOverlay(
                    new PolylineOptions()
                            .color(R.color.CornflowerBlue)
                            .points(mHistoryTrace)
                            .visible(true));

            for(LatLng point : mHistoryTrace) {
                mBaiduMap.addOverlay(new MarkerOptions().position(point).icon(bitmap));
            }
        }

        // move and zoom to current position
        if(mCurrentLocation != null) {
            mBaiduMap.setMyLocationData(mCurrentLocation);
            mBaiduMap.animateMapStatus(
                    MapStatusUpdateFactory.newLatLngZoom(
                            new LatLng(
                                    mCurrentLocation.latitude,
                                    mCurrentLocation.longitude),
                            zoomLevel));
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }
}
