package me.sahidur.foodiography.main.ar.rotation;

import android.Manifest;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.opengl.Matrix;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import me.sahidur.foodiography.R;
import me.sahidur.foodiography.main.ar.model.PlacesModel;
import me.sahidur.foodiography.main.contextmenu.ContextMenuDialogFragment;
import me.sahidur.foodiography.main.contextmenu.MenuObject;
import me.sahidur.foodiography.main.contextmenu.MenuParams;
import me.sahidur.foodiography.main.contextmenu.interfaces.OnMenuItemClickListener;
import me.sahidur.foodiography.main.description.DescriptionActivity;
import me.sahidur.foodiography.main.utils.Config;
import me.sahidur.foodiography.main.utils.Constants;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class ARActivity extends AppCompatActivity implements SensorEventListener, LocationListener, OnMenuItemClickListener {

    public static final int RP_ACCESS_LOCATION = 1;
    final static String TAG = "ARActivity";
    private final static int REQUEST_CAMERA_PERMISSIONS_CODE = 11;

    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 0; // 10 meters
    private static final long MIN_TIME_BW_UPDATES = 10000;//1000 * 60 * 1; // 1 minute

    LinearLayout infl;
    FrameLayout acar;
    Toolbar mToolbar;
    int showDist;
    TextView txtFilterDist;
    SeekBar seekFilter;

    String categoryID, categoryName;
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    List<PlacesModel> mPlacesModelList;
    List<PlacesModel> mStringFilterList;
    boolean useOrientation;
    FragmentManager mFragmentManager;
    ContextMenuDialogFragment mMenuDialogFragment;
    Sensor compass;
    private boolean isGPSEnabled = false;
    private boolean isNetworkEnabled = false;
    private Location mLocation;
    private SurfaceView surfaceView;
    private FrameLayout cameraContainerLayout;
    private AROverlayView arOverlayView;
    private Camera camera;
    private ARCamera arCamera;
    private TextView tvCurrentLocation;
    private SensorManager sensorManager;
    private LocationManager mLocationManager;
    private DrawSurfaceView mDrawView;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    private void initMenuFragment() {
        MenuParams menuParams = new MenuParams();
        menuParams.setActionBarSize((int) getResources().getDimension(R.dimen.tool_bar_height));
        menuParams.setMenuObjects(getMenuObjects());
        menuParams.setClosableOutside(false);
        mMenuDialogFragment = ContextMenuDialogFragment.newInstance(menuParams);
        mMenuDialogFragment.setItemClickListener(this);
    }

    private List<MenuObject> getMenuObjects() {
        List<MenuObject> menuObjects = new ArrayList<>();

        MenuObject close = new MenuObject();
        close.setResource(R.drawable.ic_clear);

        MenuObject itemA = new MenuObject(getString(R.string.ar_activity_use_orientation_menu));
        itemA.setResource(R.drawable.ic_orientation);
        itemA.setMenuTextAppearanceStyle(R.style.TextAppearance_FontPath_Menu);

        MenuObject itemB = new MenuObject(getString(R.string.ar_activity_use_rotation_menu));
        itemB.setResource(R.drawable.ic_rotation);
        itemB.setMenuTextAppearanceStyle(R.style.TextAppearance_FontPath_Menu);

        menuObjects.add(close);
        menuObjects.add(itemA);
        menuObjects.add(itemB);

        return menuObjects;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ar_activity_main);

        SharedPreferences settings = getSharedPreferences("DIST", MODE_PRIVATE);
        showDist = settings.getInt("DISTANCE_FILTER", 10); //10 is the default value

        useOrientation = false;

        mFragmentManager = getSupportFragmentManager();
        initMenuFragment();

        sensorManager = (SensorManager) this.getSystemService(SENSOR_SERVICE);

        compass = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);

        cameraContainerLayout = (FrameLayout) findViewById(R.id.camera_container_layout);
        infl = (LinearLayout) findViewById(R.id.infl);
        acar = (FrameLayout) findViewById(R.id.activity_ar);
        surfaceView = (SurfaceView) findViewById(R.id.surface_view);
        tvCurrentLocation = (TextView) findViewById(R.id.tv_current_location);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);

        categoryID = getIntent().getStringExtra("categoryId");
        categoryName = getIntent().getStringExtra("categoryName");
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(categoryName);

        mPlacesModelList = new ArrayList<>();
        mStringFilterList = new ArrayList<>();

        loadAllPlaces(categoryID);

        arOverlayView = new AROverlayView(ARActivity.this, infl, showDist);

        mDrawView = (DrawSurfaceView) findViewById(R.id.drawSurfaceView);

        txtFilterDist = (TextView) findViewById(R.id.txtDistanceFilter);
        txtFilterDist.setText(Integer.toString(showDist) + " Km");
        seekFilter = (SeekBar) findViewById(R.id.filterDistance);
        seekFilter.getProgressDrawable().setColorFilter(ContextCompat.getColor(this, R.color.colorAccent), PorterDuff.Mode.SRC_IN);
        seekFilter.setMax(30);
        seekFilter.setProgress(showDist); // Set it to zero so it will start at the left-most edge
        seekFilter.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progress = progress + 1; // Add the minimum value (1)

                txtFilterDist.setText(Integer.toString(progress) + " Km");
                showDist = progress;

                SharedPreferences settings = getSharedPreferences("DIST", MODE_PRIVATE);
                SharedPreferences.Editor editor = settings.edit();
                editor.putInt("DISTANCE_FILTER", showDist);
                editor.apply();

                updateLatestLocation(mLocation, mPlacesModelList, showDist, useOrientation);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

        });
    }

    public void loadAllPlaces(final String categoryId) {

        DatabaseReference myRefParent = firebaseDatabase.getReference("places");
        myRefParent.keepSynced(true);
        myRefParent.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                mPlacesModelList.clear();
                mStringFilterList.clear();
                LayoutInflater layoutInflator = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                infl.removeAllViews();
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    String parent = postSnapshot.getKey();
                    final PlacesModel placesModel = postSnapshot.getValue(PlacesModel.class);
                    placesModel.setPlaceId(parent);

                    float dist;

                    if (!placesModel.getLatlong().equals("0") && mLocation != null) {
                        String[] newLoc = placesModel.getLatlong().split(",");
                        double mLatitude = Double.parseDouble(newLoc[0].trim());
                        double mLongitude = Double.parseDouble(newLoc[1].trim());

                        placesModel.setElevation(Config.getElevation(mLatitude, mLongitude) + 10);
                        Location markerLoc = new Location("Marker");
                        markerLoc.setLatitude(mLatitude);
                        markerLoc.setLongitude(mLongitude);
                        if (mLocation == null) {
                            dist = 0;
                        } else {
                            dist = mLocation.distanceTo(markerLoc);
                            // Config.distanceFrom(currentLocation.getLatitude(), currentLocation.getLongitude(), mLatitude, mLongitude);
                        }

                    } else {
                        dist = 0;
                    }

                    placesModel.setDistance(dist);

                    View v = layoutInflator.inflate(R.layout.ar_view, null);
                    TextView t = (TextView) v.findViewById(R.id.t);
                    TextView d = (TextView) v.findViewById(R.id.d);
                    t.setText(placesModel.getPlaceName());
                    d.setText(Config.cDist(placesModel.getDistance()));


                    v.setLayoutParams(new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT));

                    t.setTextSize(setSize(placesModel.getDistance()));
                    d.setTextSize(setSize(placesModel.getDistance()) - 2);

                    v.setVisibility(View.GONE);
                    v.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent go = new Intent(ARActivity.this, DescriptionActivity.class);
                            go.putExtra("address", placesModel.getAddress());
                            go.putExtra("category", placesModel.getCategory());
                            go.putExtra("description", placesModel.getDescription());
                            go.putExtra("distance", placesModel.getDistance());
                            go.putExtra("facilities", placesModel.getFacilities());
                            go.putExtra("imageThumbnail", placesModel.getImageThumbnail());
                            go.putExtra("info", placesModel.getInfo());
                            go.putExtra("latlong", placesModel.getLatlong());
                            go.putExtra("placeId", placesModel.getPlaceId());
                            go.putExtra("placeName", placesModel.getPlaceName());
                            go.putExtra("mCurrentLocation", mLocation.getLatitude() + "," + mLocation.getLongitude());
                            startActivity(go);
                        }
                    });

                    if ((placesModel.getDistance() / 1000f) < showDist) {
                        if (categoryId.equalsIgnoreCase("all_place")) {
                            infl.addView(v);
                            mPlacesModelList.add(placesModel);
                            mStringFilterList.add(placesModel);
                        } else {
                            if (placesModel.getCategory().contains(categoryId)) {
                                infl.addView(v);
                                mPlacesModelList.add(placesModel);
                                mStringFilterList.add(placesModel);
                            }
                        }
                    }
                }


                if (arOverlayView != null) {
                    arOverlayView.updateCurrentLocation(mLocation);
                    arOverlayView.updateFilter(infl, mPlacesModelList, showDist, useOrientation);
                }

                if (mDrawView != null) {
                    mDrawView.updateCurrentLocation(mLocation);
                    mDrawView.updateFilter(infl, mPlacesModelList, showDist, useOrientation);
                }
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {
                Log.w(Constants.TAG_PARENT, ":" + firebaseError.getMessage());
            }
        });
    }

    int setSize(float dist) {
        int size;
        if (dist > 50000) {
            size = 12;
        } else if (dist <= 50000 && dist > 10000) {
            size = 14;
        } else if (dist <= 10000 && dist > 5000) {
            size = 16;
        } else if (dist <= 5000 && dist > 1000) {
            size = 18;
        } else if (dist <= 1000 && dist > 500) {
            size = 20;
        } else if (dist <= 500) {
            size = 22;
        } else if (dist <= 100) {
            size = 60;
        } else {
            size = 5;
        }
        return size;
    }

    @Override
    public void onResume() {
        super.onResume();
        requestLocationPermission();
        requestCameraPermission();
        registerSensors();
        initAROverlayView();
    }

    @Override
    public void onPause() {
        releaseCamera();
        super.onPause();
    }

    @Override
    protected void onStop() {
        sensorManager.unregisterListener(this);
        super.onStop();
    }

    public void requestCameraPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                this.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            this.requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSIONS_CODE);
        } else {
            initARCameraView();
        }
    }

    public void requestLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, RP_ACCESS_LOCATION);
        } else {
            initLocationService();
        }
    }

    public void initAROverlayView() {
        if (arOverlayView.getParent() != null) {
            ((ViewGroup) arOverlayView.getParent()).removeView(arOverlayView);
        }
        cameraContainerLayout.addView(arOverlayView);
    }

    public void initARCameraView() {
        reloadSurfaceView();

        if (arCamera == null) {
            arCamera = new ARCamera(this, surfaceView);
        }
        if (arCamera.getParent() != null) {
            ((ViewGroup) arCamera.getParent()).removeView(arCamera);
        }
        cameraContainerLayout.addView(arCamera);
        arCamera.setKeepScreenOn(true);
        initCamera();
    }

    private void initCamera() {
        int numCams = Camera.getNumberOfCameras();
        if (numCams > 0) {
            try {
                camera = Camera.open();
                camera.startPreview();
                arCamera.setCamera(camera);
            } catch (RuntimeException ex) {
                Toast.makeText(this, R.string.camera_not_found, Toast.LENGTH_LONG).show();
            }
        }
    }

    private void reloadSurfaceView() {
        if (surfaceView.getParent() != null) {
            ((ViewGroup) surfaceView.getParent()).removeView(surfaceView);
        }

        cameraContainerLayout.addView(surfaceView);
    }

    private void releaseCamera() {
        if (camera != null) {
            camera.setPreviewCallback(null);
            camera.stopPreview();
            arCamera.setCamera(null);
            camera.release();
            camera = null;
        }
    }

    private void registerSensors() {

        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(checkSensor()),
                SensorManager.SENSOR_DELAY_GAME);

        sensorManager.registerListener(this, compass,
                SensorManager.SENSOR_DELAY_GAME);

    }

    int checkSensor() {
        PackageManager packageManager = getPackageManager();
        boolean gyroExists = packageManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_GYROSCOPE);
        int sens;
        if (gyroExists) {
            sens = Sensor.TYPE_ROTATION_VECTOR;
        } else {
            sens = Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR;
        }
        return sens;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == checkSensor()) {
            float[] rotationMatrixFromVector = new float[16];
            float[] projectionMatrix = new float[16];
            float[] rotatedProjectionMatrix = new float[16];

            SensorManager.getRotationMatrixFromVector(rotationMatrixFromVector, event.values);

            if (arCamera != null) {
                projectionMatrix = arCamera.getProjectionMatrix();
            }

            Matrix.multiplyMM(rotatedProjectionMatrix, 0, projectionMatrix, 0, rotationMatrixFromVector, 0);
            this.arOverlayView.updateRotatedProjectionMatrix(rotatedProjectionMatrix);
        }

        if (event.sensor.getType() == Sensor.TYPE_ORIENTATION) {
            if (mDrawView != null) {
                mDrawView.setOffset(event.values[0]);
                mDrawView.invalidate();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        //do nothing
    }

    private void initLocationService() {
        mLocationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        isGPSEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        isNetworkEnabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (!isGPSEnabled && !isNetworkEnabled) {
            Config.showSettingsAlert(ARActivity.this);
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
                        if (mLocation != null) {
                            onLocationChanged(mLocation);
                        }
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
                            if (mLocation != null) {
                                onLocationChanged(mLocation);
                            }
                        }
                    }
                }
            }
        }

    }

    private void updateLatestLocation(Location l, List<PlacesModel> mPlacesModelList, int showDist, boolean useOrientation) {

        if (arOverlayView != null) {
            arOverlayView.updateCurrentLocation(l);
            arOverlayView.updateFilter(infl, mPlacesModelList, showDist, useOrientation);
        }

        if (mDrawView != null) {
            mDrawView.updateCurrentLocation(mLocation);
            mDrawView.updateFilter(infl, mPlacesModelList, showDist, useOrientation);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        mLocation = location;
        updateLatestLocation(mLocation, mPlacesModelList, showDist, useOrientation);
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_search_ar, menu);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        final SearchView searchView = (SearchView) menu.findItem(R.id.action_filter_search)
                .getActionView();
        View v = searchView.findViewById(android.support.v7.appcompat.R.id.search_plate);
        v.setBackgroundColor(ContextCompat.getColor(ARActivity.this, R.color.transparent_color));
        searchView.setQueryHint(getString(R.string.hint_search));
        searchView.setSearchableInfo(searchManager
                .getSearchableInfo(ARActivity.this.getComponentName()));
        searchView.setIconifiedByDefault(true);

        SearchView.OnQueryTextListener textChangeListener = new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String query) {
                query = query.toLowerCase();
                callSearch(query);
                return true;
            }

            @Override
            public boolean onQueryTextSubmit(String query) {
                query = query.toLowerCase();
                callSearch(query);
                searchView.clearFocus();
                return true;
            }

            public void callSearch(String query) {
                mPlacesModelList.clear();
                infl.removeAllViews();
                LayoutInflater layoutInflator = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                List<PlacesModel> filterList = new ArrayList<PlacesModel>();
                for (int i = 0; i < mStringFilterList.size(); i++) {
                    if ((mStringFilterList.get(i).getPlaceName().toLowerCase())
                            .contains(query.toLowerCase())) {

                        final PlacesModel newFilter = new PlacesModel();
                        newFilter.setDistance(mStringFilterList.get(i).getDistance());
                        newFilter.setViewType(mStringFilterList.get(i).getViewType());
                        newFilter.setPlaceId(mStringFilterList.get(i).getPlaceId());
                        newFilter.setAddress(mStringFilterList.get(i).getAddress());
                        newFilter.setCategory(mStringFilterList.get(i).getCategory());
                        newFilter.setDescription(mStringFilterList.get(i).getDescription());
                        newFilter.setFacilities(mStringFilterList.get(i).getFacilities());
                        newFilter.setImageThumbnail(mStringFilterList.get(i).getImageThumbnail());
                        newFilter.setLatlong(mStringFilterList.get(i).getLatlong());
                        newFilter.setInfo(mStringFilterList.get(i).getInfo());
                        newFilter.setPlaceName(mStringFilterList.get(i).getPlaceName());


                        View v = layoutInflator.inflate(R.layout.ar_view, null);
                        TextView t = (TextView) v.findViewById(R.id.t);
                        TextView d = (TextView) v.findViewById(R.id.d);
                        t.setText(newFilter.getPlaceName());
                        d.setText(Config.cDist(newFilter.getDistance()));
                        v.setLayoutParams(new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT));

                        t.setTextSize(setSize(newFilter.getDistance()));
                        d.setTextSize(setSize(newFilter.getDistance()) - 2);

                        v.setVisibility(View.GONE);
                        v.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent go = new Intent(ARActivity.this, DescriptionActivity.class);
                                go.putExtra("address", newFilter.getAddress());
                                go.putExtra("category", newFilter.getCategory());
                                go.putExtra("description", newFilter.getDescription());
                                go.putExtra("distance", newFilter.getDistance());
                                go.putExtra("facilities", newFilter.getFacilities());
                                go.putExtra("imageThumbnail", newFilter.getImageThumbnail());
                                go.putExtra("info", newFilter.getInfo());
                                go.putExtra("latlong", newFilter.getLatlong());
                                go.putExtra("placeId", newFilter.getPlaceId());
                                go.putExtra("placeName", newFilter.getPlaceName());
                                go.putExtra("mCurrentLocation", mLocation.getLatitude() + "," + mLocation.getLongitude());
                                startActivity(go);
                            }
                        });

                        if ((newFilter.getDistance() / 1000f) < showDist) {
                            if (getIntent().getStringExtra("categoryId").equalsIgnoreCase("all_place")) {
                                infl.addView(v);
                            } else {
                                if (newFilter.getCategory().contains(getIntent().getStringExtra("categoryId"))) {
                                    infl.addView(v);
                                }
                            }
                        }

                        filterList.add(newFilter);
                    }
                }

                mPlacesModelList.addAll(filterList);

                updateLatestLocation(mLocation, mPlacesModelList, showDist, useOrientation);

            }
        };
        searchView.setOnQueryTextListener(textChangeListener);
        return super.onPrepareOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.context_menu:
                if (mFragmentManager.findFragmentByTag(ContextMenuDialogFragment.TAG) == null) {
                    mMenuDialogFragment.show(mFragmentManager, ContextMenuDialogFragment.TAG);
                }
                return true;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMenuItemClick(View clickedView, int position) {
        if (position == 1) {
            useOrientation = true;
            updateLatestLocation(mLocation, mPlacesModelList, showDist, useOrientation);
        } else if (position == 2) {
            useOrientation = false;
            updateLatestLocation(mLocation, mPlacesModelList, showDist, useOrientation);
        }
    }
}
