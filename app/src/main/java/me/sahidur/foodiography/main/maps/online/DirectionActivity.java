package me.sahidur.foodiography.main.maps.online;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import me.sahidur.foodiography.R;
import me.sahidur.foodiography.main.maps.shared.DirectionMapModel;
import me.sahidur.foodiography.main.utils.AppController;
import me.sahidur.foodiography.main.utils.Config;
import me.sahidur.foodiography.main.utils.Constants;
import me.sahidur.foodiography.main.utils.MapDirectionParser;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;


public class DirectionActivity extends AppCompatActivity implements
        OnMapReadyCallback, View.OnClickListener, LocationListener, GoogleMap.OnMapLongClickListener, StepAdapter.ItemListener {

    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 0; // 10 meters
    private static final long MIN_TIME_BW_UPDATES = 10000; //
    private static final int RP_ACCESS_LOCATION = 1;

    ProgressBar vProgressBar;
    GoogleMap mMap;
    CoordinatorLayout vCoordinatorLayout;
    double mCurrentLng, mCurrentLat;
    LatLng mCurrentLocation;
    LatLng mDestinationLoc;
    ArrayList<LatLng> traceOfMe = null;
    ArrayList<DirectionMapModel> mRoutes = null;
    Polyline mPolyline = null;
    BottomSheetBehavior mBehavior;
    String TAG = "DirectionActivity:";
    boolean enable_ads_interstitial = true;
    Location mLocation;
    boolean isGPSEnabled = false;
    boolean isNetworkEnabled = false;
    boolean canGetLocation = false;
    int type = 0;
    private StepAdapter mAdapter;
    private InterstitialAd mInterstitialAd;

    FirebaseDatabase mFDatabase = FirebaseDatabase.getInstance();
    private LocationManager locationManager;
    FloatingActionButton navBtn;
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    void loadConfig() {
        DatabaseReference configRef = mFDatabase.getReference("config").child("ads_config").child(Constants.ENABLE_ADS_INTERSTITIAL);
        configRef.keepSynced(true);
        configRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                enable_ads_interstitial = dataSnapshot.getValue(Boolean.class);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Config.showToast(vCoordinatorLayout, "Database Error!");
            }
        });
    }

    public void loadInterstitial(boolean isEnable) {
        if (isEnable) {
            mInterstitialAd = new InterstitialAd(this);
            mInterstitialAd.setAdUnitId(getString(R.string.interstitial_ad_unit_id));
            AdRequest adRequest = new AdRequest.Builder()
                    //.addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                    .build();
            mInterstitialAd.loadAd(adRequest);
            mInterstitialAd.setAdListener(new AdListener() {
                public void onAdLoaded() {
                    // Call displayInterstitial() function
                    displayInterstitial();
                }
            });
        }
    }

    public void displayInterstitial() {
        // If Ads are loaded, show Interstitial else show nothing.
        if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.maps_online_activity_direction);

        loadConfig();
        getLocation();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);
        final double l1 = getIntent().getDoubleExtra("destLat", 0);
        final double l2 = getIntent().getDoubleExtra("destLng", 0);

        mDestinationLoc = new LatLng(l1, l2);

        vCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);

        vProgressBar = (ProgressBar) findViewById(R.id.progress_view);
        navBtn = (FloatingActionButton) findViewById(R.id.navBtn);

        vProgressBar.setVisibility(View.GONE);

        navBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String packageName = "com.google.android.apps.maps";
                String query = "google.navigation:q="+l1+","+l2;

                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(query));
                intent.setPackage(packageName);
                startActivity(intent);
            }
        });

        mRoutes = new ArrayList<>();

        MapFragment mapFragment = ((MapFragment) getFragmentManager().findFragmentById(R.id.map));
        mapFragment.getMapAsync(this);

        loadInterstitial(enable_ads_interstitial);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        CameraPosition position = CameraPosition.builder()
                .target(mCurrentLocation)
                .zoom(12)
                .build();

        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(false);
        mMap.getUiSettings().setMapToolbarEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.getUiSettings().setRotateGesturesEnabled(true);
        mMap.getUiSettings().isCompassEnabled();
        mMap.getUiSettings().isMapToolbarEnabled();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(position), null);
        getSupportActionBar().setTitle(getIntent().getStringExtra("placeName"));


        mMap.addMarker(new MarkerOptions()
                .position(mDestinationLoc)
                .title(getIntent().getStringExtra("placeName"))
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA)));

        traceMe(mCurrentLocation, mDestinationLoc);

        Log.w(Constants.TAG_PARENT, "Direction:" + mRoutes.size() + " - " + String.valueOf(mRoutes));

        mMap.setOnMapLongClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fabDirection:

                break;
            default:
                break;
        }
    }

    public void getLocation() {

        locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (!isGPSEnabled && !isNetworkEnabled) {
            this.canGetLocation = false;
        } else {
            this.canGetLocation = true;

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED) {
                // cek apakah perlu menampilkan info kenapa membutuhkan access fine mLocation
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_FINE_LOCATION) &&
                        ActivityCompat.shouldShowRequestPermissionRationale(this,
                                Manifest.permission.ACCESS_COARSE_LOCATION)) {

                    String[] perm = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
                    ActivityCompat.requestPermissions(this, perm,
                            RP_ACCESS_LOCATION);
                } else {
                    // request permission untuk access fine mLocation
                    String[] perm = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
                    ActivityCompat.requestPermissions(this, perm,
                            RP_ACCESS_LOCATION);
                }
            } else {
                if (isNetworkEnabled) {
                    locationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, (LocationListener) this);
                    Log.d("Network", "Network");
                    if (locationManager != null) {
                        mLocation = locationManager
                                .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if(mLocation!=null){
                            onLocationChanged(mLocation);
                        }
                    }
                }

                if (isGPSEnabled) {
                    if (mLocation == null) {
                        locationManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, (LocationListener) this);
                        Log.d("GPS Enabled", "GPS Enabled");
                        if (locationManager != null) {
                            mLocation = locationManager
                                    .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if(mLocation!=null){
                                onLocationChanged(mLocation);
                            }
                        }
                    }
                }
            }
        }
    }

    /* Remove the locationlistener updates when Activity is paused */
    @Override
    protected void onPause() {
        super.onPause();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.removeUpdates((LocationListener) this);
    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLat = location.getLatitude();
        mCurrentLng = location.getLongitude();
        mCurrentLocation = new LatLng(mCurrentLat, mCurrentLng);
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        if (type == 0) {
            mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
            type = 1;
        } else {
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            type = 0;
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
        Config.showToast(vCoordinatorLayout, R.string.enable_provider + provider);
    }

    @Override
    public void onProviderDisabled(String provider) {
        Config.showToast(vCoordinatorLayout, R.string.disable_provider + provider);
    }

    @Override
    public void onItemClick(DirectionMapModel item) {
        mBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }

    protected void showToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.maps_activity_menu, menu);
        menu.findItem(R.id.action_list_search).setVisible(false);

        return super.onCreateOptionsMenu(menu);
    }

    private String getDirectionsUrl(LatLng origin, LatLng dest) {
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        String sensor = "sensor=false";
        String units = "units=metric";
        String mode = "mode=walking&language="+getString(R.string.direction_language)+"&region=ID";
        String parameters = str_origin + "&" + str_dest + "&" + sensor + "&" + units + "&" + mode;
        String output = "json";
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;
        return url;
    }

    private void traceMe(final LatLng srcLatLng, final LatLng destLatLng) {
        String url = getDirectionsUrl(srcLatLng, destLatLng);
        vProgressBar.setVisibility(View.VISIBLE);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        MapDirectionParser parser = new MapDirectionParser();
                        List<List<HashMap<String, String>>> routes = parser.parse(response);
                        ArrayList<LatLng> points = null;

                        JSONArray jRoutes = null;
                        JSONArray jLegs = null;
                        JSONArray jSteps = null;

                        try {
                            jRoutes = response.getJSONArray("routes");
                            /** Traversing all routes */
                            for (int i = 0; i < jRoutes.length(); i++) {
                                jLegs = ((JSONObject) jRoutes.get(i)).getJSONArray("legs");
                                /** Traversing all legs */
                                for (int j = 0; j < jLegs.length(); j++) {
                                    jSteps = ((JSONObject) jLegs.get(j)).getJSONArray("steps");

                                    Log.w("JSTEP", jSteps.toString());

                                    /** Traversing all steps */
                                    for (int k = 0; k < jSteps.length(); k++) {
                                        JSONObject jo = jSteps.getJSONObject(k);

                                        JSONObject distanceObject = jo.getJSONObject("distance");
                                        JSONObject durationObject = jo.getJSONObject("duration");
                                        JSONObject startLocation = jo.getJSONObject("start_location");
                                        JSONObject endLocation = jo.getJSONObject("end_location");

                                        LatLng positionStart = new LatLng(
                                                Double.parseDouble(startLocation.get("lat").toString()),
                                                Double.parseDouble(startLocation.get("lng").toString()));

                                        LatLng positionEnd = new LatLng(
                                                Double.parseDouble(endLocation.get("lat").toString()),
                                                Double.parseDouble(endLocation.get("lng").toString()));

                                        DirectionMapModel dirObject = new DirectionMapModel();
                                        dirObject.setDistance(distanceObject.get("text").toString());
                                        dirObject.setDuration(durationObject.get("text").toString());
                                        dirObject.setHtml_instructions(jo.get("html_instructions").toString());
                                        dirObject.setStart_locaition(positionStart);
                                        dirObject.setEnd_location(positionEnd);
                                        if (jo.has("maneuver")) {
                                            dirObject.setManeuver(jo.get("maneuver").toString());
                                        } else {
                                            dirObject.setManeuver("Start");
                                        }

                                        mRoutes.add(dirObject);
                                    }
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        for (int i = 0; i < routes.size(); i++) {
                            points = new ArrayList<LatLng>();

                            List<HashMap<String, String>> path = routes.get(i);

                            // Fetching all the points in i-th route
                            for (int j = 0; j < path.size(); j++) {

                                HashMap<String, String> point = path.get(j);

                                double lat = Double.parseDouble(point.get("lat"));
                                double lng = Double.parseDouble(point.get("lng"));
                                LatLng position = new LatLng(lat, lng);

                                points.add(position);
                            }
                        }

                        drawPoints(points, mMap);

                        LatLngBounds.Builder mBoundsBuilder = new LatLngBounds.Builder();
                        mBoundsBuilder.include(srcLatLng);
                        mBoundsBuilder.include(destLatLng);
                        LatLngBounds mBounds = mBoundsBuilder.build();
                        int padding = 100;
                        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(mBounds, padding);

                        mMap.animateCamera(cu);
                        vProgressBar.setVisibility(View.INVISIBLE);

                        View bottomSheet = findViewById(R.id.bottom_sheet);
                        mBehavior = BottomSheetBehavior.from(bottomSheet);
                        mBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
                            @Override
                            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                                // React to state change
                            }

                            @Override
                            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                                // React to dragging events
                            }
                        });

                        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
                        recyclerView.setHasFixedSize(true);
                        recyclerView.setLayoutManager(new LinearLayoutManager(DirectionActivity.this));
                        TextView jalurTx = (TextView) findViewById(R.id.jalur);
                        jalurTx.setText("Routes to " + getSupportActionBar().getTitle());
                        mAdapter = new StepAdapter(DirectionActivity.this, mRoutes, DirectionActivity.this);
                        recyclerView.setAdapter(mAdapter);

                        mBehavior.setPeekHeight(getSupportActionBar().getHeight());
                        mBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        vProgressBar.setVisibility(View.INVISIBLE);
                    }
                });

        AppController.getInstance().addToReqQueue(jsonObjectRequest);
    }

    private void drawPoints(ArrayList<LatLng> points, GoogleMap mMaps) {
        if (points == null) {
            return;
        }
        traceOfMe = points;
        PolylineOptions polylineOpt = new PolylineOptions();
        for (LatLng latlng : traceOfMe) {
            polylineOpt.add(latlng);
        }
        polylineOpt.color(ContextCompat.getColor(this, R.color.colorAccent));
        if (mPolyline != null) {
            mPolyline.remove();
            mPolyline = null;
        }
        if (mMap != null) {
            mPolyline = mMap.addPolyline(polylineOpt);
        }
        if (mPolyline != null)
            mPolyline.setWidth(8);

    }
}
