package me.sahidur.foodiography.main.maps.online;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import me.sahidur.foodiography.R;
import me.sahidur.foodiography.main.maps.shared.ClusterMarkerLocation;
import me.sahidur.foodiography.main.maps.shared.DataSearchAdapter;
import me.sahidur.foodiography.main.places.PlacesModel;
import me.sahidur.foodiography.main.utils.AppController;
import me.sahidur.foodiography.main.utils.Config;
import me.sahidur.foodiography.main.utils.Constants;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;


public class MapsActivity extends AppCompatActivity implements
        ClusterManager.OnClusterItemInfoWindowClickListener<ClusterMarkerLocation>,
        AdapterView.OnItemClickListener,
        TextWatcher, OnMapReadyCallback, View.OnClickListener, GoogleMap.OnMapLongClickListener,LocationListener {

    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 0; // 5 meters
    private static final long MIN_TIME_BW_UPDATES = 10000; // 1 minute
    private static final int RP_ACCESS_LOCATION = 1;

    private LocationManager mLocationManager;
    private boolean isGPSEnabled = false;
    private boolean isNetworkEnabled = false;
    private Location mLocation;

    ProgressBar mProgressBar;
    EditText vEtSearch;
    GoogleMap mMap;
    FloatingActionButton btnDir;
    CoordinatorLayout vCoordinatorLayout;
    ListView vListView;

    double mCurrentLng, mCurrentLat;

    LatLng mCurrentLocation;

    FirebaseDatabase mFDatabase = FirebaseDatabase.getInstance();

    List<PlacesModel> mPlacesModelList;

    ClusterManager<ClusterMarkerLocation> mClusterManager;

    ClusterMarkerLocation mClickedClusterItem;

    DataSearchAdapter mDataSearchAdapter;

    Polyline mPolyline = null;

    Dialog dSearchDialog;

    CustomRenderer mCustomRenderer;

    GoogleMap.InfoWindowAdapter mGlidInfoWindow;

    String TAG = "MapsActivity:";
    int type = 0;

    AdView mAdView;

    boolean enable_ads_banner = true;

    void loadConfig(){
        DatabaseReference configRef = mFDatabase.getReference("config").child("ads_config").child(Constants.ENABLE_ADS_BANNER);
        configRef.keepSynced(true);
        configRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                enable_ads_banner = dataSnapshot.getValue(Boolean.class);
                loadAds(enable_ads_banner);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(Constants.TAG_PARENT, TAG + ":"+databaseError.getMessage());
            }
        });
    }

    void loadAds(boolean enableThis){
        if(enableThis){
            mAdView.setVisibility(View.GONE);
            mAdView.setAdListener(new AdListener() {
                @Override
                public void onAdLoaded() {
                    mAdView.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAdOpened() {
                    mAdView.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAdFailedToLoad(int i) {
                    mAdView.setVisibility(View.GONE);
                }

                @Override
                public void onAdClosed() {
                    mAdView.setVisibility(View.GONE);
                }
            });

            AdRequest adRequest = new AdRequest
                    .Builder()
                    //.addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                    .build();

            mAdView.loadAd(adRequest);
        }else{
            mAdView.setVisibility(View.GONE);
        }

    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.maps_online_activity_maps);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);
        }

        mAdView = (AdView)findViewById(R.id.adView);
        vCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);

        loadConfig();

        getLocation();

        btnDir = (FloatingActionButton) findViewById(R.id.fabDirection);
        btnDir.hide();

        dSearchDialog = new Dialog(MapsActivity.this);
        dSearchDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dSearchDialog.setContentView(R.layout.shared_search_dialog);
        dSearchDialog.setCancelable(true);

        mFDatabase = FirebaseDatabase.getInstance();

        mProgressBar = (ProgressBar) findViewById(R.id.progress_view);
        mProgressBar.setVisibility(View.GONE);

        String[] cuText = getIntent().getStringExtra("mCurrentLocation").split(",");
        mCurrentLat = Double.parseDouble(cuText[0]);
        mCurrentLng = Double.parseDouble(cuText[1]);
        mCurrentLocation = new LatLng(mCurrentLat, mCurrentLng);

        vListView = (ListView) dSearchDialog.findViewById(R.id.recycler_view);

        mPlacesModelList = new ArrayList<>();

        MapFragment mapFragment = ((MapFragment) getFragmentManager().findFragmentById(R.id.map));
        mapFragment.getMapAsync(this);

        //initCamera();

        vEtSearch = (EditText) dSearchDialog.findViewById(R.id.etCari);
        vEtSearch.clearFocus();

        vEtSearch.addTextChangedListener(this);
        vEtSearch.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    Toast.makeText(MapsActivity.this, v.getText() + "",
                            Toast.LENGTH_LONG).show();
                    handled = true;
                }
                return handled;
            }
        });

        vListView.setOnItemClickListener(this);
        vListView.setTextFilterEnabled(true);

        getSupportActionBar().setTitle("My Location");

        btnDir.setOnClickListener(this);

    }

    public void getLocation() {
        mLocationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        isGPSEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        isNetworkEnabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (!isGPSEnabled && !isNetworkEnabled) {
            Config.showSettingsAlert(MapsActivity.this);
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED) {

                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_FINE_LOCATION) &&
                        ActivityCompat.shouldShowRequestPermissionRationale(this,
                                Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    String[] perm = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
                    ActivityCompat.requestPermissions(this, perm,
                            RP_ACCESS_LOCATION);
                } else {
                    String[] perm = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
                    ActivityCompat.requestPermissions(this, perm,
                            RP_ACCESS_LOCATION);
                }
            } else {
                if (isNetworkEnabled) {
                    mLocationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                    Log.d(Constants.TAG_PARENT, TAG + "networkStatus:Network enabled");
                    if (mLocationManager != null) {
                        mLocation = mLocationManager
                                .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    }
                }

                if (isGPSEnabled) {
                    if (mLocation == null) {
                        mLocationManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                        Log.d(Constants.TAG_PARENT, TAG + "gpsStatus:GPS Enabled");
                        if (mLocationManager != null) {
                            mLocation = mLocationManager
                                    .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onClusterItemInfoWindowClick(final ClusterMarkerLocation myItem) {
        onBackPressed();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        dSearchDialog.dismiss();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(mDataSearchAdapter.getItem(position).getPlaceName());
        }

        String[] sp = mDataSearchAdapter.getItem(position).getLatlong().split(",");
        double a = Double.parseDouble(sp[0].trim());
        double b = Double.parseDouble(sp[1].trim());

        for (Marker marker : mClusterManager.getMarkerCollection().getMarkers()) {
            mClickedClusterItem = new ClusterMarkerLocation(new LatLng(a, b),
                    mDataSearchAdapter.getItem(position).getPlaceId(),
                    mDataSearchAdapter.getItem(position).getPlaceName(),
                    Config.cDist(mDataSearchAdapter.getItem(position).getDistance()),
                    mDataSearchAdapter.getItem(position).getImageThumbnail(),
                    mDataSearchAdapter.getItem(position).getPlaceName(),
                    mDataSearchAdapter.getItem(position).getLatlong(),
                    mDataSearchAdapter.getItem(position).getDescription(),
                    mDataSearchAdapter.getItem(position).getCategory(),
                    mDataSearchAdapter.getItem(position).getAddress(),
                    mDataSearchAdapter.getItem(position).getInfo(),
                    mDataSearchAdapter.getItem(position).getFacilities(),
                    mDataSearchAdapter.getItem(position).getDistance());

            if (marker.getPosition().latitude == a &&
                    marker.getPosition().longitude == b) {
                marker.showInfoWindow();

                int zoom = (int) mMap.getCameraPosition().zoom;
                CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(new LatLng(
                        a + (double) 250 / Math.pow(2, zoom),
                        b), zoom);
                mMap.moveCamera(cu);
            }
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        mDataSearchAdapter.getFilter().filter(s.toString());
    }

    @Override
    public void afterTextChanged(Editable s) {
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
        CameraPosition position;
        mClusterManager = new ClusterManager<>(this, mMap);

        String[] newLoc = getIntent().getStringExtra("latlong").split(",");

        double mLatitude = Double.parseDouble(newLoc[0].trim());
        double mLongitude = Double.parseDouble(newLoc[1].trim());
        LatLng fromLoc = new LatLng(mLatitude, mLongitude);
        position = CameraPosition.builder()
                .target(fromLoc)
                .zoom(16)
                .build();


        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(false);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.getUiSettings().setRotateGesturesEnabled(true);
        mMap.getUiSettings().isCompassEnabled();
        mMap.getUiSettings().isMapToolbarEnabled();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(position), null);
        getSupportActionBar().setTitle("My Location");
        mMap.setOnCameraChangeListener(mClusterManager);
        mMap.setInfoWindowAdapter(mClusterManager.getMarkerManager());
        mMap.setOnInfoWindowClickListener(mClusterManager);

        mMap.setOnMarkerClickListener(mClusterManager);

        mClusterManager
                .setOnClusterItemClickListener(new ClusterManager.OnClusterItemClickListener<ClusterMarkerLocation>() {
                    @Override
                    public boolean onClusterItemClick(ClusterMarkerLocation item) {
                        mClickedClusterItem = item;

                        for (Marker marker : mClusterManager.getMarkerCollection().getMarkers()) {
                            if (marker.getPosition().latitude == item.getPosition().latitude &&
                                    marker.getPosition().longitude == item.getPosition().longitude) {
                                marker.showInfoWindow();
                                btnDir.show();

                                int zoom = (int) mMap.getCameraPosition().zoom;
                                CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(
                                        new LatLng(
                                                item.getPosition().latitude + (double) 200 / Math.pow(2, zoom),
                                                item.getPosition().longitude),
                                        zoom);
                                mMap.animateCamera(cu);

                                getSupportActionBar().setTitle(item.getmTitle());
                            }
                        }
                        return true;
                    }
                });

        mGlidInfoWindow = new GlideInfoWindowAdapter(this);
        mClusterManager.getMarkerCollection().setOnInfoWindowAdapter(mGlidInfoWindow);

        mClusterManager.setOnClusterItemInfoWindowClickListener(this);

        mCustomRenderer = new CustomRenderer(this, mMap, mClusterManager);
        mClusterManager.setRenderer(mCustomRenderer);
        mCustomRenderer.setMarkersToCluster(false);

        getDataIntent(mLatitude, mLongitude,
                getIntent().getStringExtra("placeId"),
                getIntent().getStringExtra("placeName"),
                Config.cDist(getIntent().getFloatExtra("distance",0)),
                getIntent().getStringExtra("imageThumbnail"),
                getIntent().getStringExtra("placeName"),
                getIntent().getStringExtra("latlong"),
                getIntent().getStringExtra("description"),
                getIntent().getStringExtra("category"),
                getIntent().getStringExtra("address"),
                getIntent().getStringExtra("info"),
                getIntent().getStringExtra("facilities"),
                getIntent().getFloatExtra("distance", 0));

        getSupportActionBar().setTitle(getIntent().getStringExtra("placeName"));

        for (Marker marker : mClusterManager.getMarkerCollection().getMarkers()) {
            mClickedClusterItem = new ClusterMarkerLocation(new LatLng(mLatitude, mLongitude),
                    getIntent().getStringExtra("placeId"),
                    getIntent().getStringExtra("placeName"),
                    Config.cDist(getIntent().getFloatExtra("distance",0)),
                    getIntent().getStringExtra("imageThumbnail"), getIntent().getStringExtra("placeName"),
                    getIntent().getStringExtra("latlong"), getIntent().getStringExtra("description"),
                    getIntent().getStringExtra("category"), getIntent().getStringExtra("address"),
                    getIntent().getStringExtra("info"),
                    getIntent().getStringExtra("facilities"),
                    getIntent().getFloatExtra("distance", 0));

            if (marker.getPosition().latitude == mLatitude &&
                    marker.getPosition().longitude == mLongitude) {
                marker.showInfoWindow();

                int zoom = (int) mMap.getCameraPosition().zoom;
                CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(new LatLng(
                        mLatitude + (double) 250 / Math.pow(2, zoom),
                        mLongitude), zoom);
                mMap.moveCamera(cu);
            }

        }


        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                btnDir.hide();
                mCustomRenderer.setMarkersToCluster(false);
            }
        });

        mMap.setOnMapLongClickListener(this);

        MapStyleOptions styleNight = MapStyleOptions.loadRawResourceStyle(this, R.raw.map_default);
        mMap.setMapStyle(styleNight);
    }

    @Override
    public void onClick(View v) {
        Intent go;
        switch (v.getId()) {

            case R.id.fabDirection:
                if (cek_status(this)) {
                    go = new Intent(this, DirectionActivity.class);
                    go.putExtra("destLat", mClickedClusterItem.getPosition().latitude);
                    go.putExtra("destLng", mClickedClusterItem.getPosition().longitude);
                    go.putExtra("placeName", mClickedClusterItem.getmTitle());
                    go.putExtra("category", mClickedClusterItem.getmSnippet());
                    startActivity(go);
                } else {

                }

                break;
            default:
                break;
        }
    }

    public boolean cek_status(Context cek) {
        ConnectivityManager cm = (ConnectivityManager) cek.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();

        return info != null && info.isConnected();
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
            case R.id.action_list_search:
                dSearchDialog.show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initMarkers(double mLatitude,
                             double mLongitude,
                             String placeId,
                             String mTitle,
                             String mSnippet,
                             String imageThumbnail,
                             String placeName,
                             String latlong,
                             String description,
                             String category,
                             String address,
                             String info,
                             String facilities,
                             float distance) {
        mClusterManager.addItem(new
                ClusterMarkerLocation(new LatLng(mLatitude, mLongitude),
                placeId,
                mTitle,
                mSnippet,
                imageThumbnail,
                placeName,
                latlong,
                description,
                category,
                address,
                info,
                facilities,
                distance));
    }

    public void getDataIntent(double mLatitude,
                              double mLongitude,
                              String placeId,
                              String mTitle,
                              String mSnippet,
                              String imageThumbnail,
                              String placeName,
                              String latlong,
                              String description,
                              String category,
                              String address,
                              String info,
                              String facilities,
                              float distance) {
        initMarkers(mLatitude, mLongitude, placeId,
                mTitle,
                mSnippet,
                imageThumbnail,
                placeName,
                latlong,
                description,
                category,
                address,
                info,
                facilities,
                distance);
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

    @Override
    public void onLocationChanged(Location location) {
        double lat = location.getLatitude();
        double lng = location.getLongitude();
        mCurrentLocation = new LatLng(lat, lng);
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case RP_ACCESS_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    this.recreate();
                }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mLocationManager.removeUpdates(this);
        AppController.getApplication(this).unregisterOttoBus(this);
    }

    class GlideInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {
        private final Map<Marker, Bitmap> images = new HashMap<>();
        private final Map<Marker, Target<Bitmap>> targets = new HashMap<>();
        private final View myContentsView;
        Context mContext;

        @SuppressLint("InflateParams")
        GlideInfoWindowAdapter(Context mContext) {
            myContentsView = getLayoutInflater().inflate(
                    R.layout.maps_online_custom_info_window, null);
            this.mContext = mContext;
        }

        public View getInfoContents(Marker marker) {
            ImageView thumbnail = (ImageView) myContentsView.findViewById(R.id.gambar);

            TextView tvTitle = ((TextView) myContentsView
                    .findViewById(R.id.txtTitle));
            TextView tvSnippet = ((TextView) myContentsView
                    .findViewById(R.id.txtSnippet));

            tvTitle.setText(mClickedClusterItem.getmTitle());
            tvSnippet.setText(mClickedClusterItem.getmSnippet());

            String pathUrl = "gallery/" + mClickedClusterItem.getPlaceId() + "/" + mClickedClusterItem.getImageThumbnail() + "?alt=media";
            String urlImg;

            if (mClickedClusterItem.getImageThumbnail().equalsIgnoreCase("no_image.pngge.png")) {
                urlImg = "https://firebasestorage.googleapis.com/v0/b/"+Constants.FIREBASE_PROJECT_ID+".appspot.com/o/no_image.pngge.png?alt=media";
            } else {
                urlImg = "https://firebasestorage.googleapis.com/v0/b/"+Constants.FIREBASE_PROJECT_ID+".appspot.com/o/" + pathUrl.replace("/", "%2F");
            }

            Bitmap image = images.get(marker);
            if (image == null) {
                Glide.with(mContext)
                        .load(urlImg)
                        .asBitmap()
                        .dontAnimate()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(getTarget(marker));
                return null;
            } else {
                thumbnail.setImageBitmap(image);
            }
            return myContentsView;
        }

        public View getInfoWindow(Marker marker) {
            return null;
        }

        private Target<Bitmap> getTarget(Marker marker) {
            Target<Bitmap> target = targets.get(marker);
            if (target == null) {
                target = new InfoTarget(marker);
            }
            return target;
        }

        private class InfoTarget extends SimpleTarget<Bitmap> {
            Marker marker;

            InfoTarget(Marker marker) {
                super(100, 100); // otherwise Glide will load original sized bitmap which is huge
                this.marker = marker;
            }

            @Override
            public void onLoadStarted(Drawable placeholder) {
                mProgressBar.setVisibility(View.VISIBLE);
                super.onLoadStarted(placeholder);
            }

            @Override
            public void onLoadCleared(Drawable placeholder) {
                images.remove(marker);
            }

            @Override
            public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                mProgressBar.setVisibility(View.GONE);

                images.put(marker, resource);
                btnDir.show();
                marker.showInfoWindow();
                if (mPolyline != null) {
                    mPolyline.remove();
                }
            }
        }
    }

    public class CustomRenderer extends DefaultClusterRenderer<ClusterMarkerLocation> {
        private static final int MIN_CLUSTER_SIZE = 1;
        private final IconGenerator mClusterIconGenerator = new IconGenerator(getApplicationContext());
        private boolean shouldCluster = true;

        public CustomRenderer(Context context, GoogleMap map,
                              ClusterManager<ClusterMarkerLocation> clusterManager) {
            super(context, map, clusterManager);
        }

        @Override
        protected void onBeforeClusterItemRendered(ClusterMarkerLocation item,
                                                   MarkerOptions markerOptions) {
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(setMarker(R.drawable.ic_marker)));

        }

        public Bitmap setMarker(int drawable) {
            int height = 100;
            int width = 100;
            BitmapDrawable bitmapdraw = (BitmapDrawable) ContextCompat.getDrawable(MapsActivity.this, drawable);
            Bitmap b = bitmapdraw.getBitmap();
            Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);
            return smallMarker;
        }

        @Override
        protected void onClusterItemRendered(ClusterMarkerLocation clusterItem, Marker marker) {
            super.onClusterItemRendered(clusterItem, marker);
        }


        public void setMarkersToCluster(boolean toCluster) {
            this.shouldCluster = toCluster;
        }

        @Override
        protected boolean shouldRenderAsCluster(Cluster<ClusterMarkerLocation> cluster) {
            if (shouldCluster) {
                return cluster.getSize() > MIN_CLUSTER_SIZE;
            } else {
                return shouldCluster;
            }
        }
    }
}
