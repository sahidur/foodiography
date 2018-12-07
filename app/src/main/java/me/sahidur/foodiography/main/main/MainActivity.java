package me.sahidur.foodiography.main.main;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import me.sahidur.foodiography.R;
import me.sahidur.foodiography.main.about.AboutActivity;
import me.sahidur.foodiography.main.ar.rotation.ARActivity;
import me.sahidur.foodiography.main.categories.CategoryFragment;
import me.sahidur.foodiography.main.maps.online.MapsOnlineFragment;
import me.sahidur.foodiography.main.places.AllPlacesFragment;
import me.sahidur.foodiography.main.users.UserGalleryFragment;
import me.sahidur.foodiography.main.users.UserRateFragment;
import me.sahidur.foodiography.main.food.FoodActivity;
import me.sahidur.foodiography.main.utils.AppController;
import me.sahidur.foodiography.main.utils.Config;
import me.sahidur.foodiography.main.utils.Constants;
import me.sahidur.foodiography.main.pedometer.*;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import butterknife.Bind;
import butterknife.ButterKnife;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;



public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, LocationListener {

    private static final String TAG = "FoodActivity:";

    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 0; // 5 meters
    private static final long MIN_TIME_BW_UPDATES = 10000; // 1 minute
    private static final int RP_ACCESS_LOCATION = 1;
    private static final String SELECTED_ITEM_ID = "SELECTED_ITEM_ID";
    private final Handler mDrawerHandler = new Handler();

    boolean enable_ads_banner;

    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.nav_view)
    NavigationView navigationView;
    @Bind(R.id.drawer_layout)
    DrawerLayout drawer;
    @Bind(R.id.adView)
    AdView mAdView;

    View hView;
    TextView uDisplayName;
    ImageView uPhoto;
    Fragment navFragment = null;

    private LocationManager mLocationManager;
    private boolean isGPSEnabled = false;
    private boolean isNetworkEnabled = false;
    private Location mLocation;
    private String mCurrentLocation = "0.0,0.0";
    private int mPrevSelectedId;
    private int mSelectedId;
    private FirebaseAuth mFAuth = FirebaseAuth.getInstance();
    private FirebaseDatabase mFDatabase = FirebaseDatabase.getInstance();
    private FirebaseUser mFUser;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    void loadConfig() {
        DatabaseReference configRef = mFDatabase.getReference("config").child("ads_config").child(Constants.ENABLE_ADS_BANNER);
        configRef.keepSynced(true);
        configRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    enable_ads_banner = dataSnapshot.getValue(Boolean.class);
                }else {
                    enable_ads_banner = false;
                }

                loadAds(enable_ads_banner);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Config.showToast(drawer, "Database Error!");
            }
        });
    }

    void loadAds(boolean enableThis) {
        if (enableThis) {

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
        } else {
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

        setContentView(R.layout.main_activity);

        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        loadConfig();

        getLocation();

        hView = navigationView.getHeaderView(0);
        uDisplayName = (TextView) hView.findViewById(R.id.uDisplayName);
        uPhoto = (ImageView) hView.findViewById(R.id.imageView);

        SignFirst();

        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(this,
                drawer, toolbar, R.string.navigation_drawer_open,
                R.string.navigation_drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                super.onDrawerSlide(drawerView, 0);
            }

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, 0);
            }
        };
        drawer.setDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        mSelectedId = navigationView.getMenu().getItem(prefs.getInt("default_view", 0)).getItemId();
        mSelectedId = savedInstanceState == null ? mSelectedId : savedInstanceState.getInt(SELECTED_ITEM_ID);
        mPrevSelectedId = mSelectedId;
        navigationView.getMenu().findItem(mSelectedId).setChecked(true);

        if (savedInstanceState == null) {
            mDrawerHandler.removeCallbacksAndMessages(null);
            mDrawerHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    navigate(mSelectedId);
                }
            }, getResources().getInteger(R.integer.anim_duration_long));

            boolean openDrawer = prefs.getBoolean("open_drawer", false);

            if (openDrawer)
                drawer.openDrawer(GravityCompat.START);
            else
                drawer.closeDrawers();
        }

        if (savedInstanceState == null) {
            MenuItem item = navigationView.getMenu().findItem(R.id.nav_allplace).setChecked(true);
            onNavigationItemSelected(item);
        }
    }

    void SignFirst() {

        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                mFUser = firebaseAuth.getCurrentUser();
                if (mFUser != null) {
                    String url, displayName;

                    if (mFUser.isAnonymous()) {
                        url = "https://firebasestorage.googleapis.com/v0/b/"+Constants.FIREBASE_PROJECT_ID+".appspot.com/o/anonymous.png?alt=media";
                        displayName = "Guest";
                    } else {
                        url = String.valueOf(mFUser.getPhotoUrl());
                        displayName = mFUser.getDisplayName();

                        DatabaseReference mDatabase = mFDatabase.getReference("users");

                        SimpleDateFormat formatD = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
                        formatD.setTimeZone(TimeZone.getTimeZone("UTC"));
                        String timeStamp = formatD.format(new Date());
                        PostUser post = new PostUser(displayName,
                                String.valueOf(url), timeStamp, mFAuth.getCurrentUser().getEmail(), getIntent().getStringExtra("prov"));
                        Map<String, Object> postValues = post.toMap();
                        Map<String, Object> childUpdates = new HashMap<>();
                        childUpdates.put(mFUser.getUid(), postValues);
                        mDatabase.updateChildren(childUpdates);
                    }

                    uDisplayName.setText(displayName);
                    Glide.with(getApplicationContext())
                            .load(url)
                            .asBitmap()
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .centerCrop()
                            .into(new BitmapImageViewTarget(uPhoto) {
                                @Override
                                protected void setResource(Bitmap resource) {
                                    RoundedBitmapDrawable rounded =
                                            RoundedBitmapDrawableFactory.create(MainActivity.this.getResources(), resource);
                                    rounded.setCircular(true);
                                    uPhoto.setImageDrawable(rounded);
                                }
                            });
                } else {
                    Log.d(Constants.TAG_PARENT, TAG + "onAuthStateChanged:signed_out");
                    MainActivity.this.finish();
                }
            }
        };
    }

    public void getLocation() {
        mLocationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        isGPSEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        isNetworkEnabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (!isGPSEnabled && !isNetworkEnabled) {
            Config.showSettingsAlert(MainActivity.this);
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
                        if(mLocation!=null){
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
                            if(mLocation!=null){
                                onLocationChanged(mLocation);
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        double lat = location.getLatitude();
        double lng = location.getLongitude();
        mCurrentLocation = lat + "," + lng;
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
        Config.showToast(drawer, getString(R.string.enable_provider) + provider);
    }

    @Override
    public void onProviderDisabled(String provider) {
        Config.showToast(drawer, getString(R.string.disable_provider) + provider);
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

    private void navigate(final int itemId) {

        final Bundle bundle = new Bundle();
        bundle.putString("mCurrentLocation", mCurrentLocation);
        switch (itemId) {
            case R.id.nav_allplace:
                mPrevSelectedId = itemId;
                setTitle(getString(R.string.nav_title_all_place));
                navFragment = new AllPlacesFragment();
                navFragment.setArguments(bundle);
                break;
            case R.id.nav_categories:
                mPrevSelectedId = itemId;
                setTitle(getString(R.string.nav_title_categories));
                navFragment = new CategoryFragment();
                navFragment.setArguments(bundle);
                break;
            case R.id.nav_map:
                mPrevSelectedId = itemId;
                if (Config.checkConnection(MainActivity.this)) {
                    setTitle(getString(R.string.nav_title_online_map));
                    navFragment = new MapsOnlineFragment();
                    navFragment.setArguments(bundle);
                } else {
                    Snackbar snackbar = Snackbar
                            .make(drawer, "Your internet is not connected!", Snackbar.LENGTH_INDEFINITE);
                    snackbar.setAction("FORCE", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            setTitle(getString(R.string.nav_title_online_map));
                            navFragment = new MapsOnlineFragment();
                            navFragment.setArguments(bundle);
                        }
                    });
                    snackbar.show();
                }
                break;
            case R.id.nav_ar:
                mPrevSelectedId = itemId;
                Intent ar = new Intent(MainActivity.this, ARActivity.class);
                ar.putExtra("categoryId", "all_place");
                ar.putExtra("categoryName", "AR Map");
                startActivity(ar);
                navigationView.getMenu().findItem(mPrevSelectedId).setChecked(false);
                break;
            case R.id.nav_review:
                mPrevSelectedId = itemId;
                Intent pedo = new Intent(MainActivity.this, Pedometer.class);
                pedo.putExtra("categoryId", "all_place");
                pedo.putExtra("categoryName", "Counter");
                startActivity(pedo);
                navigationView.getMenu().findItem(mPrevSelectedId).setChecked(false);
                break;
            case R.id.nav_gallery:
                mPrevSelectedId = itemId;
                Intent food = new Intent(MainActivity.this, FoodActivity.class);
                food.putExtra("categoryId", "all_place");
                food.putExtra("categoryName", "Food Chart");
                startActivity(food);
                navigationView.getMenu().findItem(mPrevSelectedId).setChecked(false);
                break;
            case R.id.nav_logout:
                if (mAuthStateListener != null) {
                    if (mFUser != null) {

                        if (mFUser.isAnonymous()) {
                            mFUser.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    FirebaseAuth.getInstance().signOut();
                                    mFAuth.removeAuthStateListener(mAuthStateListener);
                                }
                            });
                            MainActivity.this.finish();
                        } else {
                            FirebaseAuth.getInstance().signOut();
                            mFAuth.removeAuthStateListener(mAuthStateListener);
                            MainActivity.this.finish();
                        }

                    }
                }
                break;
            case R.id.nav_about:
                mPrevSelectedId = itemId;
                Intent intent = new Intent(this, AboutActivity.class);
                startActivity(intent);
                navigationView.getMenu().findItem(mPrevSelectedId).setChecked(false);
                break;
        }

        if (navFragment != null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
            try {
                transaction.replace(R.id.container_body, navFragment).commit();
            } catch (IllegalStateException ignored) {
                Log.e(Constants.TAG_PARENT, TAG + ignored.getMessage());
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        AppController.getApplication(this).registerOttoBus(this);
        getLocation();
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (mFUser != null) {
                if (mFUser.isAnonymous()) {
                    mFUser.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            FirebaseAuth.getInstance().signOut();
                            mFAuth.removeAuthStateListener(mAuthStateListener);
                        }
                    });
                }
            }
            MainActivity.this.finish();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mFAuth.addAuthStateListener(mAuthStateListener);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        mSelectedId = item.getItemId();
        mDrawerHandler.removeCallbacksAndMessages(null);
        mDrawerHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                navigate(mSelectedId);
            }
        }, getResources().getInteger(R.integer.anim_duration_long));
        drawer.closeDrawers();
        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(SELECTED_ITEM_ID, mSelectedId);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mFUser != null) {
            if (mFUser.isAnonymous()) {
                mFUser.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        FirebaseAuth.getInstance().signOut();
                        mFAuth.removeAuthStateListener(mAuthStateListener);
                    }
                });
            }
        }
    }

    @IgnoreExtraProperties
    private class PostUser {
        String displayName;
        String photoUrl;
        String lastAccess;
        String email;
        String providers;

        PostUser() {
            // Default constructor required for calls to DataSnapshot.getValue(Post.class)
        }

        PostUser(String display_name, String photo_url, String last_access, String email, String provider) {
            this.displayName = display_name;
            this.photoUrl = photo_url;
            this.lastAccess = last_access;
            this.email = email;
            this.providers = provider;
        }

        @Exclude
        Map<String, Object> toMap() {
            HashMap<String, Object> result = new HashMap<>();
            result.put("displayName", displayName);
            result.put("photoUrl", photoUrl);
            result.put("email", email);
            result.put("provider", providers);
            result.put("lastAccess", lastAccess);
            return result;
        }
    }
}
