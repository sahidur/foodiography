package me.sahidur.foodiography.main.users;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.util.Log;
import android.view.MenuItem;
import android.view.Window;
import android.widget.RelativeLayout;

import me.sahidur.foodiography.R;
import me.sahidur.foodiography.main.utils.Constants;
import me.sahidur.foodiography.main.utils.ScaleImageView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class ImageShowActivity extends AppCompatActivity {
    @Bind(R.id.toolbar)
    Toolbar mToolbar;

    @Bind(R.id.image)
    ScaleImageView vScaleImage;

    @Bind(R.id.buttonDelete)
    FloatingActionButton vBtnDelete;

    @Bind(R.id.coordinator)
    RelativeLayout vCoordinatorLayout;

    FirebaseDatabase mFDatabase= FirebaseDatabase.getInstance();
    FirebaseStorage mFStorage = FirebaseStorage.getInstance();
    private ProgressDialog dProgress;

    private InterstitialAd mInterstitialAd;

    private final String TAG = "ImageShowActivity:";

    boolean enable_ads_interstitial;

    void loadRemoteConfig(){
        DatabaseReference configRef = mFDatabase.getReference("config").child("ads_config").child(Constants.ENABLE_ADS_INTERSTITIAL);
        configRef.keepSynced(true);
        configRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                enable_ads_interstitial = dataSnapshot.getValue(Boolean.class);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(Constants.TAG_PARENT, TAG + ":"+databaseError.getMessage());
            }
        });
    }

    public void loadInterstitial(boolean isEnable) {
        if(isEnable){
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
            Transition transition;
            transition = TransitionInflater.from(this).inflateTransition(R.transition.slide_from_bottom);
            getWindow().setEnterTransition(transition);

            getWindow().setExitTransition(transition);
        }
        setContentView(R.layout.shared_activity_image_show);

        loadRemoteConfig();

        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(getIntent().getStringExtra("fileName"));

        String urlImg = getIntent().getStringExtra("urlImg");

        Glide.with(getApplicationContext())
                .load(urlImg)
                .asBitmap()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(new BitmapImageViewTarget(vScaleImage) {
                    @Override
                    protected void setResource(Bitmap resource) {
                        vScaleImage.setImageBitmap(resource);
                    }
                });

        loadInterstitial(enable_ads_interstitial);
    }

    @OnClick(R.id.buttonDelete)
    void deleteFoto() {

        final DatabaseReference myRef = mFDatabase.getReference("gallery")
                .child(getIntent().getStringExtra("userId"))
                .child(getIntent().getStringExtra("placeId"))
                .child(getIntent().getStringExtra("imageId"));

        mFStorage.setMaxOperationRetryTimeMillis(5000);

        StorageReference storageRef = mFStorage.getReferenceFromUrl("gs://"+Constants.FIREBASE_PROJECT_ID+".appspot.com/gallery/" + getIntent().getStringExtra("placeId"));

        final StorageReference desertRef = storageRef.child(getIntent().getStringExtra("fileName"));

        dProgress = new ProgressDialog(ImageShowActivity.this);
        dProgress.setMessage(getString(R.string.txt_deleting));
        dProgress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dProgress.setCancelable(false);
        dProgress.show();

        desertRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                myRef.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        dProgress.hide();
                        finish();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                dProgress.hide();
                Snackbar snackbar = Snackbar
                        .make(vCoordinatorLayout, "Currently can't delete image, try again!", Snackbar.LENGTH_LONG);
                snackbar.show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        supportFinishAfterTransition();
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
}